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
package com.orientechnologies.common.concur.lock;

import java.util.concurrent.locks.ReadWriteLock;

/**
 * Lock that uses the write lock of a reader writer lock object.
 * 
 * @author Luca Garulli (l.garulli--at--orientechnologies.com)
 * 
 */
public class OSharedLock extends OAbstractLock {
  private final ReadWriteLock lock;

  public OSharedLock(final ReadWriteLock iLock) {
    lock = iLock;
  }

  public void lock() {
    lock.readLock().lock();
  }

  public void unlock() {
    lock.readLock().unlock();
  }
}
