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
package com.orientechnologies.orient.core.db;

import com.orientechnologies.orient.core.record.ORecord;

/**
 * Default strategy that always uses the default data-segment with id = 0
 * 
 * @author Luca Garulli (l.garulli--at--orientechnologies.com)
 * 
 */
public class ODefaultDataSegmentStrategy implements ODataSegmentStrategy {

  public int assignDataSegmentId(final ODatabase iDatabase, final ORecord<?> iRecord) {
    // GET THE DATASEGMENT SPECIFIED IN THE RECORD IF ANY
    final String dsName = iRecord.getDataSegmentName();
    if (dsName != null)
      return iDatabase.getDataSegmentIdByName(dsName);

    // GET THE DATA SEGMENT CONFIGURED IN THE CLUSTER IF ANY
    final int clusterId = iRecord.getIdentity().getClusterId();
    if (clusterId >= 0)
      return iDatabase.getStorage().getClusterById(clusterId).getDataSegmentId();

    // RETURN 0 AS DEFAULT ONE
    return 0;
  }

}
