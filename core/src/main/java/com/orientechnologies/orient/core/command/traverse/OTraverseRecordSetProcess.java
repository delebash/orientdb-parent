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

import java.util.Collection;
import java.util.Iterator;

import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.core.record.impl.ODocument;

public class OTraverseRecordSetProcess extends OTraverseAbstractProcess<Iterator<OIdentifiable>> {
  protected OIdentifiable record;
  protected int           index = -1;

  public OTraverseRecordSetProcess(final OTraverse iCommand, final Iterator<OIdentifiable> iTarget) {
    super(iCommand, iTarget);
  }

  @SuppressWarnings("unchecked")
  public OIdentifiable process() {
    while (target.hasNext()) {
      record = target.next();
      index++;

      final ORecord<?> rec = record.getRecord();
      if (rec instanceof ODocument) {
        ODocument doc = (ODocument) rec;
        if (!doc.getIdentity().isPersistent() && doc.fields() == 1) {
          // EXTRACT THE FIELD CONTEXT
          Object fieldvalue = doc.field(doc.fieldNames()[0]);
          if (fieldvalue instanceof Collection<?>) {
            final OTraverseRecordSetProcess subProcess = new OTraverseRecordSetProcess(command,
                ((Collection<OIdentifiable>) fieldvalue).iterator());
            final OIdentifiable subValue = subProcess.process();
            if (subValue != null)
              return subValue;

          } else if (fieldvalue instanceof ODocument) {
            final OTraverseRecordProcess subProcess = new OTraverseRecordProcess(command, (ODocument) rec);
            final OIdentifiable subValue = subProcess.process();
            if (subValue != null)
              return subValue;
          }

        } else {
          final OTraverseRecordProcess subProcess = new OTraverseRecordProcess(command, (ODocument) rec);

          final OIdentifiable subValue = subProcess.process();
          if (subValue != null)
            return subValue;
        }
      }
    }

    return drop();
  }

  @Override
  public String getStatus() {
    return null;
  }

  @Override
  public String toString() {
    return target != null ? target.toString() : "-";
  }
}