/*
 * Copyright 2010-2013 Luca Garulli (l.garulli--at--orientechnologies.com)
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
package com.orientechnologies.orient.server.hazelcast;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.concurrent.atomic.AtomicLong;

import com.orientechnologies.orient.server.distributed.ODistributedRequest;
import com.orientechnologies.orient.server.distributed.task.OAbstractRemoteTask;

/**
 * Hazelcast implementation of distributed peer.
 * 
 * @author Luca Garulli (l.garulli--at--orientechnologies.com)
 * 
 */
public class OHazelcastDistributedRequest implements ODistributedRequest, Externalizable {
  private static AtomicLong   serialId = new AtomicLong();

  private long                id;
  private EXECUTION_MODE      executionMode;
  private String              senderNodeName;
  private String              databaseName;
  private String              clusterName;
  private long                senderThreadId;
  private OAbstractRemoteTask task;

  /**
   * Constructor used by serializer.
   */
  public OHazelcastDistributedRequest() {
  }

  public OHazelcastDistributedRequest(final String senderNodeName, final String databaseName, final String clusterName,
      final OAbstractRemoteTask payload, EXECUTION_MODE iExecutionMode) {
    this.senderNodeName = senderNodeName;
    this.databaseName = databaseName;
    this.clusterName = clusterName;
    this.senderThreadId = Thread.currentThread().getId();
    this.task = payload;
    this.executionMode = iExecutionMode;
    id = serialId.incrementAndGet();
  }

  @Override
  public void undo() {
    task.undo();
  }

  public long getId() {
    return id;
  }

  @Override
  public String getDatabaseName() {
    return databaseName;
  }

  @Override
  public String getClusterName() {
    return clusterName;
  }

  @Override
  public OAbstractRemoteTask getTask() {
    return task;
  }

  @Override
  public OHazelcastDistributedRequest setDatabaseName(final String databaseName) {
    this.databaseName = databaseName;
    return this;
  }

  @Override
  public OHazelcastDistributedRequest setClusterName(final String clusterName) {
    this.clusterName = clusterName;
    return this;
  }

  @Override
  public OHazelcastDistributedRequest setTask(final OAbstractRemoteTask payload) {
    this.task = payload;
    return this;
  }

  public String getSenderNodeName() {
    return senderNodeName;
  }

  public OHazelcastDistributedRequest setSenderNodeName(final String senderNodeName) {
    this.senderNodeName = senderNodeName;
    return this;
  }

  @Override
  public EXECUTION_MODE getExecutionMode() {
    return executionMode;
  }

  public OHazelcastDistributedRequest setExecutionMode(final EXECUTION_MODE executionMode) {
    this.executionMode = executionMode;
    return this;
  }

  @Override
  public void writeExternal(final ObjectOutput out) throws IOException {
    out.writeLong(id);
    out.writeUTF(senderNodeName);
    out.writeLong(senderThreadId);
    out.writeUTF(databaseName);
    out.writeUTF(clusterName != null ? clusterName : "");
    out.writeObject(task);
  }

  @Override
  public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
    id = in.readLong();
    senderNodeName = in.readUTF();
    senderThreadId = in.readLong();
    databaseName = in.readUTF();
    clusterName = in.readUTF();
    if (clusterName.length() == 0)
      clusterName = null;
    task = (OAbstractRemoteTask) in.readObject();
  }

  @Override
  public String toString() {
    final StringBuilder buffer = new StringBuilder();
    buffer.append("id=");
    buffer.append(id);
    if (task != null) {
      buffer.append(" task=");
      buffer.append(task.toString());
    }
    return buffer.toString();
  }
}
