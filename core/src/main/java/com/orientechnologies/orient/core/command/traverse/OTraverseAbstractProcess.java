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
package com.orientechnologies.orient.core.command.traverse;

import com.orientechnologies.orient.core.command.OCommandProcess;
import com.orientechnologies.orient.core.db.record.OIdentifiable;

public abstract class OTraverseAbstractProcess<T> extends OCommandProcess<OTraverse, T, OIdentifiable> {
  public OTraverseAbstractProcess(final OTraverse iCommand, final T iTarget) {
    super(iCommand, iTarget);
    command.getContext().push(this);
  }

  public abstract String getStatus();

  public OIdentifiable drop() {
    command.getContext().pop();
    return null;
  }
}