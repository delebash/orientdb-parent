/*
 * Copyright 2013 Orient Technologies.
 * Copyright 2013 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orientechnologies.orient.core.sql.method.misc;

import com.orientechnologies.orient.core.command.OCommandContext;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.record.impl.ODocumentHelper;

/**
 * Works against multi value objects like collections, maps and arrays.
 * 
 * @author Luca Garulli
 */
public class OSQLMethodMultiValue extends OAbstractSQLMethod {

  public static final String NAME = "multivalue";

  public OSQLMethodMultiValue() {
    super(NAME, 0, 0);
  }

  @Override
  public Object execute(final OIdentifiable iCurrentRecord, final OCommandContext iContext, final Object ioResult,
      final Object[] iMethodParams) {

    return ODocumentHelper.getFieldValue(ioResult, iMethodParams[0].toString(), iContext);
  }
}
