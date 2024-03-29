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
package com.orientechnologies.orient.core.metadata.security;

import java.util.Set;

import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.record.ODatabaseRecord;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.exception.OConfigurationException;
import com.orientechnologies.orient.core.exception.OSecurityException;
import com.orientechnologies.orient.core.hook.ODocumentHookAbstract;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OClassImpl;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * Checks the access against restricted resources. Restricted resources are those documents of classes that implement ORestricted
 * abstract class.
 * 
 * @author Luca Garulli
 */
public class ORestrictedAccessHook extends ODocumentHookAbstract {
  public ORestrictedAccessHook() {
  }

  public DISTRIBUTED_EXECUTION_MODE getDistributedExecutionMode() {
    return DISTRIBUTED_EXECUTION_MODE.BOTH;
  }

  @Override
  public RESULT onRecordBeforeCreate(final ODocument iDocument) {
    final OClass cls = iDocument.getSchemaClass();
    if (cls != null && cls.isSubClassOf(OSecurityShared.RESTRICTED_CLASSNAME)) {
      String fieldNames = ((OClassImpl) cls).getCustom(OSecurityShared.ONCREATE_FIELD);
      if (fieldNames == null)
        fieldNames = OSecurityShared.ALLOW_ALL_FIELD;
      final String[] fields = fieldNames.split(",");
      String identityType = ((OClassImpl) cls).getCustom(OSecurityShared.ONCREATE_IDENTITY_TYPE);
      if (identityType == null)
        identityType = "user";

      final ODatabaseRecord db = ODatabaseRecordThreadLocal.INSTANCE.get();

      ODocument identity = null;
      if (identityType.equals("user"))
        identity = db.getUser().getDocument();
      else if (identityType.equals("role")) {
        final Set<ORole> roles = db.getUser().getRoles();
        if (!roles.isEmpty())
          identity = roles.iterator().next().getDocument();
      } else
        throw new OConfigurationException("Wrong custom field '" + OSecurityShared.ONCREATE_IDENTITY_TYPE + "' in class '"
            + cls.getName() + "' with value '" + identityType + "'. Supported ones are: 'user', 'role'");

      if (identity != null) {
        for (String f : fields)
          db.getMetadata().getSecurity().allowIdentity(iDocument, f, identity);
        return RESULT.RECORD_CHANGED;
      }
    }
    return RESULT.RECORD_NOT_CHANGED;
  }

  @Override
  public RESULT onRecordBeforeRead(final ODocument iDocument) {
    return isAllowed(iDocument, OSecurityShared.ALLOW_READ_FIELD, false) ? RESULT.RECORD_NOT_CHANGED : RESULT.SKIP;
  }

  @Override
  public RESULT onRecordBeforeUpdate(final ODocument iDocument) {
    if (!isAllowed(iDocument, OSecurityShared.ALLOW_UPDATE_FIELD, true))
      throw new OSecurityException("Cannot update record " + iDocument.getIdentity() + ": the resource has restricted access");
    return RESULT.RECORD_NOT_CHANGED;
  }

  @Override
  public RESULT onRecordBeforeDelete(final ODocument iDocument) {
    if (!isAllowed(iDocument, OSecurityShared.ALLOW_DELETE_FIELD, true))
      throw new OSecurityException("Cannot delete record " + iDocument.getIdentity() + ": the resource has restricted access");
    return RESULT.RECORD_NOT_CHANGED;
  }

  @SuppressWarnings("unchecked")
  protected boolean isAllowed(final ODocument iDocument, final String iAllowOperation, final boolean iReadOriginal) {
    final OClass cls = iDocument.getSchemaClass();
    if (cls != null && cls.isSubClassOf(OSecurityShared.RESTRICTED_CLASSNAME)) {

      final ODatabaseRecord db = ODatabaseRecordThreadLocal.INSTANCE.get();

      if (db.getUser() == null)
        return true;

      if (db.getUser().isRuleDefined(ODatabaseSecurityResources.BYPASS_RESTRICTED))
        if (db.getUser().checkIfAllowed(ODatabaseSecurityResources.BYPASS_RESTRICTED, ORole.PERMISSION_READ) != null)
          // BYPASS RECORD LEVEL SECURITY: ONLY "ADMIN" ROLE CAN BY DEFAULT
          return true;

      final ODocument doc;
      if (iReadOriginal)
        // RELOAD TO AVOID HACKING OF "_ALLOW" FIELDS
        doc = (ODocument) db.load(iDocument.getIdentity());
      else
        doc = iDocument;

      return db
          .getMetadata()
          .getSecurity()
          .isAllowed((Set<OIdentifiable>) doc.field(OSecurityShared.ALLOW_ALL_FIELD),
              (Set<OIdentifiable>) doc.field(iAllowOperation));
    }

    return true;
  }
}
