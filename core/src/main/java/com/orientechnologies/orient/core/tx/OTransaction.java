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
package com.orientechnologies.orient.core.tx;

import java.util.List;

import com.orientechnologies.orient.core.db.ODatabaseComplex.OPERATION_MODE;
import com.orientechnologies.orient.core.db.record.ODatabaseRecordTx;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.db.record.ORecordOperation;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.record.ORecordInternal;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.storage.ORecordCallback;
import com.orientechnologies.orient.core.version.ORecordVersion;

public interface OTransaction {
  public enum TXTYPE {
    NOTX, OPTIMISTIC, PESSIMISTIC
  }

  public enum TXSTATUS {
    INVALID, BEGUN, COMMITTING, ROLLBACKING
  }

  public void begin();

  public void commit();

  public void rollback();

  public ODatabaseRecordTx getDatabase();

  public void clearRecordEntries();

  public ORecordInternal<?> loadRecord(ORID iRid, ORecordInternal<?> iRecord, String iFetchPlan, boolean ignoreCache,
      boolean loadTombstone);

  public boolean updateReplica(ORecordInternal<?> iRecord);

  public void saveRecord(ORecordInternal<?> iContent, String iClusterName, OPERATION_MODE iMode, boolean iForceCreate,
      ORecordCallback<? extends Number> iRecordCreatedCallback, ORecordCallback<ORecordVersion> iRecordUpdatedCallback);

  public void deleteRecord(ORecordInternal<?> iRecord, OPERATION_MODE iMode);

  public int getId();

  public TXSTATUS getStatus();

  public Iterable<? extends ORecordOperation> getCurrentRecordEntries();

  public Iterable<? extends ORecordOperation> getAllRecordEntries();

  public List<ORecordOperation> getRecordEntriesByClass(String iClassName);

  public List<ORecordOperation> getNewRecordEntriesByClusterIds(int[] iIds);

  public ORecordInternal<?> getRecord(ORID iRid);

  public ORecordOperation getRecordEntry(ORID rid);

  public List<String> getInvolvedIndexes();

  public ODocument getIndexChanges();

  public void addIndexEntry(OIndex<?> delegate, final String iIndexName, final OTransactionIndexChanges.OPERATION iStatus,
      final Object iKey, final OIdentifiable iValue);

  public void clearIndexEntries();

  public OTransactionIndexChanges getIndexChanges(String iName);

  /**
   * Tells if the transaction is active.
   * 
   * @return
   */
  public boolean isActive();

  public boolean isUsingLog();

  public void setUsingLog(boolean useLog);

  public void close();

  /**
   * When commit in transaction is performed all new records will change their identity, but index values will contain stale links,
   * to fix them given method will be called for each entry. This update local transaction maps too.
   * 
   * @param oldRid
   *          Record identity before commit.
   * @param newRid
   *          Record identity after commit.
   */
  public void updateIdentityAfterCommit(final ORID oldRid, final ORID newRid);
}
