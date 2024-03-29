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

import com.orientechnologies.common.exception.OException;
import com.orientechnologies.orient.server.distributed.task.OAbstractRemoteTask;

/**
 * Exception thrown when the operation has already been executed.
 * 
 * @author Luca Garulli (l.garulli--at--orientechnologies.com)
 * 
 */
public class OSkippedOperationException extends OException {
  private static final long         serialVersionUID = 1L;
  private final OAbstractRemoteTask task;

  public OSkippedOperationException(OAbstractRemoteTask iTask) {
    task = iTask;
  }

  public OAbstractRemoteTask getTask() {
    return task;
  }

  @Override
  public String toString() {
    return "OSkippedOperationException task=" + task;
  }
}
