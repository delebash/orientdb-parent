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
package com.orientechnologies.orient.core.index;

import java.util.Collections;
import java.util.List;

import com.orientechnologies.orient.core.collate.ODefaultCollate;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.db.record.ORecordElement;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * Index implementation bound to one schema class property.
 * 
 */
public class OPropertyIndexDefinition extends OAbstractIndexDefinition {
  protected String className;
  protected String field;
  protected OType  keyType;

  public OPropertyIndexDefinition(final String iClassName, final String iField, final OType iType) {
    className = iClassName;
    field = iField;
    keyType = iType;
  }

  /**
   * Constructor used for index unmarshalling.
   */
  public OPropertyIndexDefinition() {
  }

  public String getClassName() {
    return className;
  }

  public List<String> getFields() {
    return Collections.singletonList(field);
  }

  public List<String> getFieldsToIndex() {
    return Collections.singletonList(field);
  }

  public Object getDocumentValueToIndex(final ODocument iDocument) {
    if (OType.LINK.equals(keyType)) {
      final OIdentifiable identifiable = iDocument.field(field, OType.LINK);
      if (identifiable != null)
        return createValue(identifiable.getIdentity());
      else
        return null;
    }
    return createValue(iDocument.field(field));
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    final OPropertyIndexDefinition that = (OPropertyIndexDefinition) o;

    if (!className.equals(that.className))
      return false;
    if (!field.equals(that.field))
      return false;
    if (keyType != that.keyType)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = className.hashCode();
    result = 31 * result + field.hashCode();
    result = 31 * result + keyType.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "OPropertyIndexDefinition{" + "className='" + className + '\'' + ", field='" + field + '\'' + ", keyType=" + keyType
        + '}';
  }

  public Object createValue(final List<?> params) {
    return OType.convert(params.get(0), keyType.getDefaultJavaType());
  }

  /**
   * {@inheritDoc}
   */
  public Object createValue(final Object... params) {
    return OType.convert(params[0], keyType.getDefaultJavaType());
  }

  public int getParamCount() {
    return 1;
  }

  public OType[] getTypes() {
    return new OType[] { keyType };
  }

  @Override
  protected final void fromStream() {
    serializeFromStream();
  }

  @Override
  public final ODocument toStream() {
    document.setInternalStatus(ORecordElement.STATUS.UNMARSHALLING);

    try {
      serializeToStream();
    } finally {
      document.setInternalStatus(ORecordElement.STATUS.LOADED);
    }

    return document;
  }

  protected void serializeToStream() {
    document.field("className", className);
    document.field("field", field);
    document.field("keyType", keyType.toString());
    document.field("collate", collate.getName());
  }

  protected void serializeFromStream() {
    className = document.field("className");
    field = document.field("field");

    final String keyTypeStr = document.field("keyType");
    keyType = OType.valueOf(keyTypeStr);

    setCollate((String) document.field("collate"));
  }

  /**
   * {@inheritDoc}
   * 
   * @param indexName
   * @param indexType
   */
  public String toCreateIndexDDL(final String indexName, final String indexType) {
    return createIndexDDLWithFieldType(indexName, indexType).toString();
  }

  protected StringBuilder createIndexDDLWithFieldType(String indexName, String indexType) {
    final StringBuilder ddl = createIndexDDLWithoutFieldType(indexName, indexType);
    ddl.append(' ').append(keyType.name());
    return ddl;
  }

  protected StringBuilder createIndexDDLWithoutFieldType(final String indexName, final String indexType) {
    final StringBuilder ddl = new StringBuilder("create index ");

    final String shortName = className + "." + field;
    if (indexName.equalsIgnoreCase(shortName)) {
      ddl.append(shortName).append(" ");
    } else {
      ddl.append(indexName).append(" on ");
      ddl.append(className).append(" ( ").append(field);
      if (!collate.getName().equals(ODefaultCollate.NAME))
        ddl.append(" COLLATE ").append(collate.getName());
      ddl.append(" ) ");
    }
    ddl.append(indexType);

    return ddl;
  }

  @Override
  public boolean isAutomatic() {
    return true;
  }
}
