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
package com.orientechnologies.orient.core.db;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.orientechnologies.common.concur.lock.OAdaptiveLock;
import com.orientechnologies.common.concur.lock.OLockException;
import com.orientechnologies.common.concur.resource.OResourcePool;
import com.orientechnologies.common.concur.resource.OResourcePoolListener;
import com.orientechnologies.common.io.OIOUtils;
import com.orientechnologies.common.log.OLogManager;
import com.orientechnologies.orient.core.OOrientListener;
import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.storage.OStorage;

public abstract class ODatabasePoolAbstract<DB extends ODatabase> extends OAdaptiveLock implements
    OResourcePoolListener<String, DB>, OOrientListener {

  private final HashMap<String, OResourcePool<String, DB>> pools = new HashMap<String, OResourcePool<String, DB>>();
  private int                                              maxSize;
  private int                                              timeout;
  protected Object                                         owner;
  private Timer                                            evictionTask;
  private Evictor                                          evictor;

  public ODatabasePoolAbstract(final Object iOwner, final int iMinSize, final int iMaxSize) {
    this(iOwner, iMinSize, iMaxSize, OGlobalConfiguration.CLIENT_CONNECT_POOL_WAIT_TIMEOUT.getValueAsInteger(),
        OGlobalConfiguration.DB_POOL_IDLE_TIMEOUT.getValueAsLong(), OGlobalConfiguration.DB_POOL_IDLE_CHECK_DELAY.getValueAsLong());
  }

  public ODatabasePoolAbstract(final Object iOwner, final int iMinSize, final int iMaxSize, final long idleTimeout,
      final long timeBetweenEvictionRunsMillis) {
    this(iOwner, iMinSize, iMaxSize, OGlobalConfiguration.CLIENT_CONNECT_POOL_WAIT_TIMEOUT.getValueAsInteger(), idleTimeout,
        timeBetweenEvictionRunsMillis);
  }

  public ODatabasePoolAbstract(final Object iOwner, final int iMinSize, final int iMaxSize, final int iTimeout,
      final long idleTimeoutMillis, final long timeBetweenEvictionRunsMillis) {
    super(OGlobalConfiguration.ENVIRONMENT_CONCURRENT.getValueAsBoolean(), OGlobalConfiguration.STORAGE_LOCK_TIMEOUT
        .getValueAsInteger(), true);

    maxSize = iMaxSize;
    timeout = iTimeout;
    owner = iOwner;
    Orient.instance().registerListener(this);

    if (idleTimeoutMillis > 0 && timeBetweenEvictionRunsMillis > 0) {
      this.evictionTask = new Timer();
      this.evictor = new Evictor(idleTimeoutMillis);
      this.evictionTask.schedule(evictor, timeBetweenEvictionRunsMillis, timeBetweenEvictionRunsMillis);
    }
  }

  public DB acquire(final String iURL, final String iUserName, final String iUserPassword) throws OLockException {
    return acquire(iURL, iUserName, iUserPassword, null);
  }

  public DB acquire(final String iURL, final String iUserName, final String iUserPassword, final Map<String, Object> iOptionalParams)
      throws OLockException {
    final String dbPooledName = OIOUtils.getUnixFileName(iUserName + "@" + iURL);

    lock();
    try {

      OResourcePool<String, DB> pool = pools.get(dbPooledName);
      if (pool == null)
        // CREATE A NEW ONE
        pool = new OResourcePool<String, DB>(maxSize, this);

      final DB db = pool.getResource(iURL, timeout, iUserName, iUserPassword, iOptionalParams);

      // PUT IN THE POOL MAP ONLY IF AUTHENTICATION SUCCEED
      pools.put(dbPooledName, pool);
      return db;

    } finally {
      unlock();
    }
  }

  public void release(final DB iDatabase) {
    final String dbPooledName = iDatabase instanceof ODatabaseComplex ? ((ODatabaseComplex<?>) iDatabase).getUser().getName() + "@"
        + iDatabase.getURL() : iDatabase.getURL();

    lock();
    try {

      final OResourcePool<String, DB> pool = pools.get(dbPooledName);
      if (pool == null)
        throw new OLockException("Cannot release a database URL not acquired before. URL: " + iDatabase.getName());

      pool.returnResource(iDatabase);
      this.notifyEvictor(dbPooledName, iDatabase);

    } finally {
      unlock();
    }
  }

  public DB reuseResource(final String iKey, final DB iValue) {
    return iValue;
  }

  public Map<String, OResourcePool<String, DB>> getPools() {
    lock();
    try {

      return Collections.unmodifiableMap(pools);

    } finally {
      unlock();
    }
  }

  /**
   * Closes all the databases.
   */
  public void close() {
    lock();
    try {

      if (this.evictionTask != null) {
        this.evictionTask.cancel();
      }

      for (Entry<String, OResourcePool<String, DB>> pool : pools.entrySet()) {
        for (DB db : pool.getValue().getResources()) {
          pool.getValue().close();
          try {
            OLogManager.instance().debug(this, "Closing pooled database '%s'...", db.getName());
            ((ODatabasePooled) db).forceClose();
            OLogManager.instance().debug(this, "OK", db.getName());
          } catch (Exception e) {
            OLogManager.instance().debug(this, "Error: %d", e.toString());
          }
        }
      }

    } finally {
      unlock();
    }
  }

  public void remove(final String iName, final String iUser) {
    remove(iUser + "@" + iName);
  }

  public void remove(final String iPoolName) {
    lock();
    try {

      final OResourcePool<String, DB> pool = pools.get(iPoolName);

      if (pool != null) {
        for (DB db : pool.getResources()) {
          if (db.getStorage().getStatus() == OStorage.STATUS.OPEN)
            try {
              OLogManager.instance().debug(this, "Closing pooled database '%s'...", db.getName());
              ((ODatabasePooled) db).forceClose();
              OLogManager.instance().debug(this, "OK", db.getName());
            } catch (Exception e) {
              OLogManager.instance().debug(this, "Error: %d", e.toString());
            }

        }
        pool.close();
        pools.remove(iPoolName);
      }

    } finally {
      unlock();
    }
  }

  public int getMaxSize() {
    return maxSize;
  }

  public void onStorageRegistered(final OStorage iStorage) {
  }

  /**
   * Removes from memory the pool associated to the closed storage. This avoids pool open against closed storages.
   */
  public void onStorageUnregistered(final OStorage iStorage) {
    final String storageURL = iStorage.getURL();

    lock();
    try {
      Set<String> poolToClose = null;

      for (Entry<String, OResourcePool<String, DB>> e : pools.entrySet()) {
        final int pos = e.getKey().indexOf("@");
        final String dbName = e.getKey().substring(pos + 1);
        if (storageURL.equals(dbName)) {
          if (poolToClose == null)
            poolToClose = new HashSet<String>();

          poolToClose.add(e.getKey());
        }
      }

      if (poolToClose != null)
        for (String pool : poolToClose)
          remove(pool);

    } finally {
      unlock();
    }
  }

  private void notifyEvictor(final String poolName, final DB iDatabase) {
    if (this.evictor != null) {
      this.evictor.updateIdleTime(poolName, iDatabase);
    }
  }

  /**
   * The idle object evictor {@link TimerTask}.
   */
  class Evictor extends TimerTask {

    private HashMap<String, Map<DB, Long>> evictionMap = new HashMap<String, Map<DB, Long>>();
    private long                           minIdleTime;

    public Evictor(long minIdleTime) {
      this.minIdleTime = minIdleTime;
    }

    /**
     * Run pool maintenance. Evict objects qualifying for eviction
     */
    @Override
    public void run() {
      OLogManager.instance().debug(this, "Running Connection Pool Evictor Service...");
      lock();
      try {
        for (Entry<String, Map<DB, Long>> pool : this.evictionMap.entrySet()) {
          Map<DB, Long> poolDbs = pool.getValue();
          Iterator<Entry<DB, Long>> iterator = poolDbs.entrySet().iterator();
          while (iterator.hasNext()) {
            Entry<DB, Long> db = iterator.next();
            if (System.currentTimeMillis() - db.getValue() >= this.minIdleTime) {

              OResourcePool<String, DB> oResourcePool = pools.get(pool.getKey());
              if (oResourcePool != null) {
                OLogManager.instance().debug(this, "Closing idle pooled database '%s'...", db.getKey().getName());
                ((ODatabasePooled) db.getKey()).forceClose();
                oResourcePool.remove(db.getKey());
                iterator.remove();
              }

            }
          }

        }
      } finally {
        unlock();
      }
    }

    public void updateIdleTime(final String poolName, final DB iDatabase) {
      Map<DB, Long> pool = this.evictionMap.get(poolName);
      if (pool == null) {
        pool = new HashMap<DB, Long>();
        this.evictionMap.put(poolName, pool);
      }

      pool.put(iDatabase, System.currentTimeMillis());

    }
  }
}
