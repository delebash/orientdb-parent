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
package com.orientechnologies.orient.core.record;

import java.io.Serializable;

import com.orientechnologies.orient.core.db.record.ODatabaseRecord;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.db.record.ORecordElement;
import com.orientechnologies.orient.core.exception.ORecordNotFoundException;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.tx.OTransactionOptimistic;
import com.orientechnologies.orient.core.version.ORecordVersion;

/**
 * Generic record representation. The object can be reused across multiple calls to the database by using the {@link #reset()}
 * method.
 */
public interface ORecord<T> extends ORecordElement, OIdentifiable, Serializable {
  /**
   * Removes all the dependencies with other records. All the relationships remain in form of RecordID. If some links contain dirty
   * records, the detach cannot be complete and this method returns false.
   * 
   * @return True if the document has been successfully detached, otherwise false.
   */
  public boolean detach();

  /**
   * Resets the record to be reused. The record is fresh like just created. Use this method to recycle records avoiding the creation
   * of them stressing the JVM Garbage Collector.
   * 
   * @return The Object instance itself giving a "fluent interface". Useful to call multiple methods in chain.
   */
  public <RET extends ORecord<T>> RET reset();

  /**
   * Unloads current record. All information are lost but the record identity. At the next access the record will be auto-reloaded.
   * Useful to free memory or to avoid to keep an old version of it.
   * 
   * @return The Object instance itself giving a "fluent interface". Useful to call multiple methods in chain.
   */
  public <RET extends ORecord<T>> RET unload();

  /**
   * All the fields are deleted but the record identity is maintained. Use this to remove all the document's fields.
   * 
   * @return The Object instance itself giving a "fluent interface". Useful to call multiple methods in chain.
   */
  public <RET extends ORecord<T>> RET clear();

  /**
   * Creates a copy of the record. All the record contents are copied.
   * 
   * @return The Object instance itself giving a "fluent interface". Useful to call multiple methods in chain.
   */
  public <RET extends ORecord<T>> RET copy();

  /**
   * Returns the record identity as &lt;cluster-id&gt;:&lt;cluster-position&gt;
   */
  public ORID getIdentity();

  /**
   * Returns the data segment where the record will be created at first.
   * 
   * @return Data segment name
   */
  public String getDataSegmentName();

  /**
   * Sets the data segment name where to save the record the first time it's created.
   * 
   * @param iName
   *          Data segment name
   * @return The Object instance itself giving a "fluent interface". Useful to call multiple methods in chain.
   */
  public <RET extends ORecord<T>> RET setDataSegmentName(String iName);

  /**
   * Returns the current version number of the record. When the record is created has version = 0. At every change the storage
   * increment the version number. Version number is used by Optimistic transactions to check if the record is changed in the
   * meanwhile of the transaction. In distributed environment you should prefer {@link #getRecordVersion()} instead of this method.
   * 
   * @see OTransactionOptimistic
   * @return The version number. 0 if it's a brand new record.
   */
  public int getVersion();

  /**
   * The same as {@link #getVersion()} but returns {@link ORecordVersion} interface that can contain additional information about
   * current version. In distributed environment you should prefer this method instead of {@link #getVersion()}.
   * 
   * @return version of record
   * @see ORecordVersion
   */
  public ORecordVersion getRecordVersion();

  /**
   * Returns the database where the record belongs.
   * 
   * @return
   */
  public ODatabaseRecord getDatabase();

  /**
   * Checks if the record is dirty, namely if it was changed in memory.
   * 
   * @return True if dirty, otherwise false
   */
  public boolean isDirty();

  /**
   * Checks if the record is pinned.
   * 
   * @return True if pinned, otherwise false
   */
  public Boolean isPinned();

  /**
   * Suggests to the engine to keep the record in cache. Use it for the most read records.
   * 
   * @see ORecord#unpin()
   * @return The Object instance itself giving a "fluent interface". Useful to call multiple methods in chain.
   */
  public <RET extends ORecord<T>> RET pin();

  /**
   * Suggests to the engine to not keep the record in cache.
   * 
   * @see ORecord#pin()
   * @return The Object instance itself giving a "fluent interface". Useful to call multiple methods in chain.
   */
  public <RET extends ORecord<T>> RET unpin();

  /**
   * Loads the record content in memory. If the record is in cache will be returned a new instance, so pay attention to use the
   * returned. If the record is dirty, then it returns to the original content. If the record does not exist a
   * ORecordNotFoundException exception is thrown.
   * 
   * @return The record loaded or itself if the record has been reloaded from the storage. Useful to call methods in chain.
   */
  public <RET extends ORecord<T>> RET load() throws ORecordNotFoundException;

  /**
   * Loads the record content in memory. No cache is used. If the record is dirty, then it returns to the original content. If the
   * record does not exist a ORecordNotFoundException exception is thrown.
   * 
   * @return The Object instance itself giving a "fluent interface". Useful to call multiple methods in chain.
   */
  public <RET extends ORecord<T>> RET reload() throws ORecordNotFoundException;

  /**
   * Saves in-memory changes to the database. Behavior depends by the current running transaction if any. If no transaction is
   * running then changes apply immediately. If an Optimistic transaction is running then the record will be changed at commit time.
   * The current transaction will continue to see the record as modified, while others not. If a Pessimistic transaction is running,
   * then an exclusive lock is acquired against the record. Current transaction will continue to see the record as modified, while
   * others cannot access to it since it's locked.
   * 
   * @return The Object instance itself giving a "fluent interface". Useful to call multiple methods in chain.
   */
  public <RET extends ORecord<T>> RET save();

  /**
   * Saves in-memory changes to the database defining a specific cluster where to save it. Behavior depends by the current running
   * transaction if any. If no transaction is running then changes apply immediately. If an Optimistic transaction is running then
   * the record will be changed at commit time. The current transaction will continue to see the record as modified, while others
   * not. If a Pessimistic transaction is running, then an exclusive lock is acquired against the record. Current transaction will
   * continue to see the record as modified, while others cannot access to it since it's locked.
   * 
   * @return The Object instance itself giving a "fluent interface". Useful to call multiple methods in chain.
   */
  public <RET extends ORecord<T>> RET save(String iCluster);

  public <RET extends ORecord<T>> RET save(boolean forceCreate);

  public <RET extends ORecord<T>> RET save(String iCluster, boolean forceCreate);

  /**
   * Deletes the record from the database. Behavior depends by the current running transaction if any. If no transaction is running
   * then the record is deleted immediately. If an Optimistic transaction is running then the record will be deleted at commit time.
   * The current transaction will continue to see the record as deleted, while others not. If a Pessimistic transaction is running,
   * then an exclusive lock is acquired against the record. Current transaction will continue to see the record as deleted, while
   * others cannot access to it since it's locked.
   * 
   * @return The Object instance itself giving a "fluent interface". Useful to call multiple methods in chain.
   */
  public <RET extends ORecord<T>> RET delete();

  /**
   * Fills the record parsing the content in JSON format.
   * 
   * @param iJson
   *          Object content in JSON format
   * @return The Object instance itself giving a "fluent interface". Useful to call multiple methods in chain.
   */
  public <RET extends ORecord<T>> RET fromJSON(String iJson);

  /**
   * Exports the record in JSON format.
   * 
   * @return Object content in JSON format
   */
  public String toJSON();

  /**
   * Exports the record in JSON format specifying additional formatting settings.
   * 
   * @param iFormat
   *          Format settings separated by comma. Available settings are:
   *          <ul>
   *          <li><b>rid</b>: exports the record's id as property "@rid"</li>
   *          <li><b>version</b>: exports the record's version as property "@version"</li>
   *          <li><b>class</b>: exports the record's class as property "@class"</li>
   *          <li><b>attribSameRow</b>: exports all the record attributes in the same row</li>
   *          <li><b>indent:&lt;level&gt;</b>: Indents the output if the &lt;level&gt; specified. Default is 0</li>
   *          </ul>
   *          Example: "rid,version,class,indent:6" exports record id, version and class properties along with record properties
   *          using an indenting level equals of 6.
   * @return Object content in JSON format
   */
  public String toJSON(String iFormat);

  /**
   * Returns the size in bytes of the record. The size can be computed only for not new records.
   * 
   * @return the size in bytes
   */
  public int getSize();

  /**
   * Adds identity change listener, which is called when record identity is changed. Identity is changed if new record is saved or
   * if transaction is committed and new record created inside of transaction.
   * 
   * @param identityChangeListener
   *          Listener instance.
   */
  public void addIdentityChangeListener(OIdentityChangeListener identityChangeListener);

  /**
   * Removes identity change listener, which is called when record identity is changed. Identity is changed if new record is saved
   * or if transaction is committed and new record created inside of transaction.
   * 
   * @param identityChangeListener
   *          Listener instance.
   */
  public void removeIdentityChangeListener(OIdentityChangeListener identityChangeListener);
}
