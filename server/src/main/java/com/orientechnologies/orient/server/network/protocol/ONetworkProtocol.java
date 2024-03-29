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
package com.orientechnologies.orient.server.network.protocol;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

import com.orientechnologies.common.concur.OTimeoutException;
import com.orientechnologies.common.log.OLogManager;
import com.orientechnologies.common.thread.OSoftThread;
import com.orientechnologies.orient.core.config.OContextConfiguration;
import com.orientechnologies.orient.enterprise.channel.OChannel;
import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.distributed.ODistributedServerManager;

public abstract class ONetworkProtocol extends OSoftThread {
  private static final int MAX_RETRIES = 20;
  protected OServer        server;

  public ONetworkProtocol(ThreadGroup group, String name) {
    super(group, name);
  }

  public abstract void config(OServer iServer, Socket iSocket, OContextConfiguration iConfiguration, List<?> statelessCommands,
      List<?> statefulCommands) throws IOException;

  public abstract String getType();

  public abstract int getVersion();

  public abstract OChannel getChannel();

  public String getListeningAddress() {
    final OChannel c = getChannel();
    if (c != null)
      return c.socket.getLocalAddress().getHostAddress();
    return null;
  }

  public OServer getServer() {
    return server;
  }

  public void waitNodeIsOnline() throws OTimeoutException {
    // WAIT THE NODE IS ONLINE AGAIN
    final ODistributedServerManager mgr = server.getDistributedManager();
    if (mgr != null && mgr.isEnabled() && mgr.isOffline()) {
      for (int retry = 0; retry < MAX_RETRIES; ++retry) {
        if (mgr != null && mgr.isOffline()) {
          // NODE NOT ONLINE YET, REFUSE THE CONNECTION
          OLogManager.instance().info(this, "Node is not online yet (status=%s), blocking the command until it's online %d/%d",
              mgr.getStatus(), retry + 1, MAX_RETRIES);
          pauseCurrentThread(300);
        } else
          // OK, RETURN
          return;
      }

      // TIMEOUT
      throw new OTimeoutException("Cannot execute operation while the node is not online (status=" + mgr.getStatus() + ")");
    }
  }
}
