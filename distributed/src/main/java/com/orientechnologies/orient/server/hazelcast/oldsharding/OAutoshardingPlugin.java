package com.orientechnologies.orient.server.hazelcast.oldsharding;

import java.util.HashSet;
import java.util.Set;

import com.orientechnologies.common.parser.OSystemVariableResolver;
import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.core.db.ODatabaseComplex;
import com.orientechnologies.orient.core.db.ODatabaseLifecycleListener;
import com.orientechnologies.orient.core.metadata.OMetadataDefault;
import com.orientechnologies.orient.core.metadata.function.OFunction;
import com.orientechnologies.orient.core.metadata.security.ORole;
import com.orientechnologies.orient.core.metadata.security.OSecurityShared;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.storage.OStorage;
import com.orientechnologies.orient.core.storage.OStorageEmbedded;
import com.orientechnologies.orient.core.type.tree.provider.OMVRBTreeRIDProvider;
import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.config.OServerParameterConfiguration;
import com.orientechnologies.orient.server.hazelcast.oldsharding.distributed.ODHTConfiguration;
import com.orientechnologies.orient.server.hazelcast.oldsharding.hazelcast.ServerInstance;
import com.orientechnologies.orient.server.plugin.OServerPluginAbstract;

/**
 * Distributed plugin implementation that supports autosharding
 * 
 * @author <a href="mailto:enisher@gmail.com">Artem Orobets</a>
 * @since 8/27/12
 */
public class OAutoshardingPlugin extends OServerPluginAbstract implements ODatabaseLifecycleListener {

  private boolean          enabled = true;
  private ServerInstance   serverInstance;
  private DHTConfiguration dhtConfiguration;
  private OServer          server;

  @Override
  public String getName() {
    return "autosharding";
  }

  @Override
  public void config(final OServer iServer, OServerParameterConfiguration[] iParams) {
    server = iServer;
    server.setVariable("OAutoshardingPlugin", this);

    String configFile = "/hazelcast.xml";
    for (OServerParameterConfiguration param : iParams) {
      if (param.name.equalsIgnoreCase("enabled")) {
        if (!Boolean.parseBoolean(param.value)) {
          enabled = false;
          return;
        }
      } else if (param.name.equalsIgnoreCase("configuration.hazelcast")) {
        configFile = OSystemVariableResolver.resolveSystemVariables(param.value);
      }
    }

    dhtConfiguration = new DHTConfiguration(server);

    serverInstance = new ServerInstance(server, configFile);
    serverInstance.setDHTConfiguration(dhtConfiguration);
  }

  @Override
  public void startup() {
    if (!enabled)
      return;

    serverInstance.init();

    super.startup();
    Orient.instance().addDbLifecycleListener(this);
  }

  @Override
  public void onCreate(final ODatabase iDatabase) {
    onOpen(iDatabase);
  }

  @Override
  public void onOpen(final ODatabase iDatabase) {
    if (iDatabase instanceof ODatabaseComplex<?>) {
      iDatabase.replaceStorage(new OAutoshardedStorageImpl(serverInstance, (OStorageEmbedded) iDatabase.getStorage(),
          dhtConfiguration));
    }
  }

  @Override
  public void onClose(ODatabase iDatabase) {
  }

  public static class DHTConfiguration implements ODHTConfiguration {

    private final HashSet<String> undistributableClusters;
    private final OServer         server;

    public DHTConfiguration(final OServer iServer) {
      server = iServer;
      undistributableClusters = new HashSet<String>();

      undistributableClusters.add(OStorage.CLUSTER_DEFAULT_NAME.toLowerCase());
      undistributableClusters.add(OMetadataDefault.CLUSTER_INTERNAL_NAME.toLowerCase());
      undistributableClusters.add(OMetadataDefault.CLUSTER_INDEX_NAME.toLowerCase());
      undistributableClusters.add(OMetadataDefault.CLUSTER_MANUAL_INDEX_NAME.toLowerCase());
      undistributableClusters.add(ORole.CLASS_NAME.toLowerCase());
      undistributableClusters.add(OUser.CLASS_NAME.toLowerCase());
      undistributableClusters.add(OMVRBTreeRIDProvider.PERSISTENT_CLASS_NAME.toLowerCase());
      undistributableClusters.add(OSecurityShared.RESTRICTED_CLASSNAME.toLowerCase());
      undistributableClusters.add(OSecurityShared.IDENTITY_CLASSNAME.toLowerCase());
      undistributableClusters.add(OFunction.CLASS_NAME.toLowerCase());
    }

    @Override
    public Set<String> getDistributedStorageNames() {
      return server.getAvailableStorageNames().keySet();
    }

    @Override
    public Set<String> getUndistributableClusters() {
      return undistributableClusters;
    }
  }

}
