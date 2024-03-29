/*
 * Copyright 2010-2012 Luca Garulli (l.garulli--at--orientechnologies.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orientechnologies.orient.server.distributed;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.orientechnologies.common.log.OLogManager;
import com.orientechnologies.common.parser.OSystemVariableResolver;
import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.core.db.ODatabaseComplex;
import com.orientechnologies.orient.core.db.ODatabaseLifecycleListener;
import com.orientechnologies.orient.core.exception.OConfigurationException;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.storage.OStorageEmbedded;
import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.config.OServerParameterConfiguration;
import com.orientechnologies.orient.server.distributed.ODistributedServerLog.DIRECTION;
import com.orientechnologies.orient.server.distributed.conflict.OReplicationConflictResolver;
import com.orientechnologies.orient.server.plugin.OServerPluginAbstract;

/**
 * Abstract plugin to manage the distributed environment.
 * 
 * @author Luca Garulli (l.garulli--at--orientechnologies.com)
 * 
 */
public abstract class ODistributedAbstractPlugin extends OServerPluginAbstract implements ODistributedServerManager,
    ODatabaseLifecycleListener {
  public static final String                              REPLICATOR_USER             = "replicator";
  protected static final String                           MASTER_AUTO                 = "$auto";

  protected static final String                           PAR_DEF_DISTRIB_DB_CONFIG   = "configuration.db.default";
  protected static final String                           FILE_DISTRIBUTED_DB_CONFIG  = "distributed-config.json";

  protected OServer                                       serverInstance;
  protected Map<String, ODocument>                        cachedDatabaseConfiguration = new HashMap<String, ODocument>();

  protected boolean                                       enabled                     = true;
  protected String                                        nodeName                    = null;
  protected Class<? extends OReplicationConflictResolver> confictResolverClass;
  protected File                                          defaultDatabaseConfigFile;
  protected Map<String, ODistributedPartitioningStrategy> strategies                  = new HashMap<String, ODistributedPartitioningStrategy>();

  @SuppressWarnings("unchecked")
  @Override
  public void config(OServer oServer, OServerParameterConfiguration[] iParams) {
    serverInstance = oServer;
    oServer.setVariable("ODistributedAbstractPlugin", this);

    for (OServerParameterConfiguration param : iParams) {
      if (param.name.equalsIgnoreCase("enabled")) {
        if (!Boolean.parseBoolean(OSystemVariableResolver.resolveSystemVariables(param.value))) {
          // DISABLE IT
          enabled = false;
          return;
        }
      } else if (param.name.equalsIgnoreCase("nodeName"))
        nodeName = param.value;
      else if (param.name.startsWith(PAR_DEF_DISTRIB_DB_CONFIG)) {
        defaultDatabaseConfigFile = new File(OSystemVariableResolver.resolveSystemVariables(param.value));
        if (!defaultDatabaseConfigFile.exists())
          throw new OConfigurationException("Cannot find distributed database config file: " + defaultDatabaseConfigFile);
      } else if (param.name.equalsIgnoreCase("conflict.resolver.impl"))
        try {
          confictResolverClass = (Class<? extends OReplicationConflictResolver>) Class.forName(param.value);
        } catch (ClassNotFoundException e) {
          OLogManager.instance().error(this, "Cannot find the conflict resolver implementation '%s'", e, param.value);
        }
      else if (param.name.startsWith("sharding.strategy.")) {
        try {
          strategies.put(param.name.substring("sharding.strategy.".length()),
              (ODistributedPartitioningStrategy) Class.forName(param.value).newInstance());
        } catch (Exception e) {
          OLogManager.instance().error(this, "Cannot create sharding strategy instance '%s'", e, param.value);

          e.printStackTrace();
        }
      }
    }

    if (serverInstance.getUser(REPLICATOR_USER) == null)
      // CREATE THE REPLICATOR USER
      try {
        serverInstance.addUser(REPLICATOR_USER, null, "database.passthrough");
        serverInstance.saveConfiguration();
      } catch (IOException e) {
        throw new OConfigurationException("Error on creating 'replicator' user", e);
      }
  }

  @Override
  public void startup() {
    if (!enabled)
      return;

    Orient.instance().addDbLifecycleListener(this);
  }

  @Override
  public void shutdown() {
    if (!enabled)
      return;

    Orient.instance().removeDbLifecycleListener(this);
  }

  /**
   * Auto register myself as hook.
   */
  @Override
  public void onOpen(final ODatabase iDatabase) {
    final String dbDirectory = serverInstance.getDatabaseDirectory();
    if (!iDatabase.getURL().substring(iDatabase.getURL().indexOf(":") + 1).startsWith(dbDirectory))
      // NOT OWN DB, SKIPT IT
      return;

    synchronized (cachedDatabaseConfiguration) {
      final ODistributedConfiguration cfg = getDatabaseConfiguration(iDatabase.getName());
      if (cfg == null)
        return;

      if (cfg.isReplicationActive(null)) {
        if (iDatabase instanceof ODatabaseComplex<?> && !(iDatabase.getStorage() instanceof ODistributedStorage))
          ((ODatabaseComplex<?>) iDatabase).replaceStorage(new ODistributedStorage(serverInstance,
              (OStorageEmbedded) ((ODatabaseComplex<?>) iDatabase).getStorage()));
      }
    }
  }

  /**
   * Remove myself as hook.
   */
  @Override
  public void onClose(final ODatabase iDatabase) {
  }

  @Override
  public void sendShutdown() {
    super.sendShutdown();
  }

  @Override
  public String getName() {
    return "cluster";
  }

  public String getLocalNodeId() {
    return nodeName;
  }

  public ODistributedPartitioningStrategy getReplicationStrategy(String iStrategy) {
    if (iStrategy.startsWith("$"))
      iStrategy = iStrategy.substring(1);

    final ODistributedPartitioningStrategy strategy = strategies.get(iStrategy);
    if (strategy == null)
      throw new ODistributedException("Configured strategy '" + iStrategy + "' is not configured");

    return strategy;

  }

  public ODistributedPartitioningStrategy getPartitioningStrategy(final String iStrategyName) {
    return strategies.get(iStrategyName);
  }

  protected ODocument loadDatabaseConfiguration(final String iDatabaseName, final File file) {
    if (!file.exists() || file.length() == 0)
      return null;

    ODistributedServerLog.info(this, getLocalNodeName(), null, DIRECTION.NONE, "loaded database configuration from disk: %s", file);

    FileInputStream f = null;
    try {
      f = new FileInputStream(file);
      final byte[] buffer = new byte[(int) file.length()];
      f.read(buffer);

      final ODocument doc = (ODocument) new ODocument().fromJSON(new String(buffer), "noMap");
      updateCachedDatabaseConfiguration(iDatabaseName, doc);
      return doc;

    } catch (Exception e) {
    } finally {
      if (f != null)
        try {
          f.close();
        } catch (IOException e) {
        }
    }
    return null;
  }

  public void updateCachedDatabaseConfiguration(final String iDatabaseName, final ODocument cfg) {
    synchronized (cachedDatabaseConfiguration) {
      cachedDatabaseConfiguration.put(iDatabaseName, cfg);

      OLogManager.instance().info(this, "updated distributed configuration for database: %s:\n----------\n%s\n----------",
          iDatabaseName, cfg.toJSON("prettyPrint"));
    }
  }

  public ODistributedConfiguration getDatabaseConfiguration(final String iDatabaseName) {
    synchronized (cachedDatabaseConfiguration) {
      ODocument cfg = cachedDatabaseConfiguration.get(iDatabaseName);
      if (cfg == null) {
        cfg = cachedDatabaseConfiguration.get("*");
        if (cfg == null) {
          cfg = loadDatabaseConfiguration(iDatabaseName, defaultDatabaseConfigFile);
          if (cfg == null)
            throw new OConfigurationException("Cannot load default distributed database config file: " + defaultDatabaseConfigFile);
        }
      }
      return new ODistributedConfiguration(cfg);
    }
  }

  protected void saveDatabaseConfiguration(final String iDatabaseName, final ODocument cfg) {
    synchronized (cachedDatabaseConfiguration) {
      final ODocument oldCfg = cachedDatabaseConfiguration.get(iDatabaseName);
      if (oldCfg != null && Arrays.equals(oldCfg.toStream(), cfg.toStream()))
        // NO CHANGE, SKIP IT
        return;
    }

    // INCREMENT VERSION
    Integer oldVersion = cfg.field("version");
    if (oldVersion == null)
      oldVersion = 0;
    cfg.field("version", oldVersion.intValue() + 1);

    updateCachedDatabaseConfiguration(iDatabaseName, cfg);

    FileOutputStream f = null;
    try {
      File file = getDistributedConfigFile(iDatabaseName);

      OLogManager.instance().config(this, "Saving distributed configuration file for database '%s' in: %s", iDatabaseName, file);

      f = new FileOutputStream(file);
      f.write(cfg.toJSON().getBytes());
    } catch (Exception e) {
      OLogManager.instance().error(this, "Error on saving distributed configuration file", e);

    } finally {
      if (f != null)
        try {
          f.close();
        } catch (IOException e) {
        }
    }
  }

  public File getDistributedConfigFile(final String iDatabaseName) {
    return new File(serverInstance.getDatabaseDirectory() + iDatabaseName + "/" + FILE_DISTRIBUTED_DB_CONFIG);
  }

  public OServer getServerInstance() {
    return serverInstance;
  }
}
