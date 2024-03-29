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
package com.orientechnologies.orient.server.distributed.task;

import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.version.ORecordVersion;

/**
 * Distributed create record task used for synchronization.
 * 
 * @author Luca Garulli (l.garulli--at--orientechnologies.com)
 * 
 */
public abstract class OAbstractRecordReplicatedTask extends OAbstractReplicatedTask {
  protected ORecordId      rid;
  protected ORecordVersion version;

  public OAbstractRecordReplicatedTask() {
  }

  public OAbstractRecordReplicatedTask(final ORecordId iRid, final ORecordVersion iVersion) {
    this.rid = iRid;
    this.version = iVersion;
  }

  @Override
  public String toString() {
    return super.toString() + "(" + rid + " v." + version + ")";
  }

  public ORecordId getRid() {
    return rid;
  }

  public void setRid(ORecordId rid) {
    this.rid = rid;
  }

  public ORecordVersion getVersion() {
    return version;
  }

  public void setVersion(ORecordVersion version) {
    this.version = version;
  }

  @Override
  public String getPayload() {
    return "rid=" + rid + " v=" + version;
  }
}
