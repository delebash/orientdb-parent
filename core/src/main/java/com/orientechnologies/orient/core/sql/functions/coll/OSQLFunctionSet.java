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
package com.orientechnologies.orient.core.sql.functions.coll;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.orientechnologies.common.collection.OMultiValue;
import com.orientechnologies.orient.core.command.OCommandContext;
import com.orientechnologies.orient.core.db.record.OIdentifiable;

/**
 * This operator add an item in a set. The set doesn't accept duplicates, so adding multiple times the same value has no effect: the
 * value is contained only once.
 * 
 * @author Luca Garulli (l.garulli--at--orientechnologies.com)
 * 
 */
public class OSQLFunctionSet extends OSQLFunctionMultiValueAbstract<Set<Object>> {
  public static final String NAME = "set";

  public OSQLFunctionSet() {
    super(NAME, 1, -1);
  }

  public Object execute(final OIdentifiable iCurrentRecord, Object iCurrentResult, final Object[] iParameters,
      OCommandContext iContext) {
    if (iParameters.length > 1)
      // IN LINE MODE
      context = new HashSet<Object>();

    for (Object value : iParameters) {
      if (value != null) {
        if (iParameters.length == 1 && context == null)
          // AGGREGATION MODE (STATEFULL)
          context = new HashSet<Object>();

        OMultiValue.add(context, value);
      }
    }

    return prepareResult(context);
  }

  public String getSyntax() {
    return "Syntax error: set(<value>*)";
  }

  public boolean aggregateResults(final Object[] configuredParameters) {
    return configuredParameters.length == 1;
  }

  @Override
  public Set<Object> getResult() {
    final Set<Object> res = context;
    context = null;
    return prepareResult(res);
  }

  protected Set<Object> prepareResult(Set<Object> res) {
    if (returnDistributedResult()) {
      final Map<String, Object> doc = new HashMap<String, Object>();
      doc.put("node", getDistributedStorageId());
      doc.put("context", context);
      return Collections.<Object> singleton(doc);
    } else {
      return res;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object mergeDistributedResult(List<Object> resultsToMerge) {
    final Map<Long, Collection<Object>> chunks = new HashMap<Long, Collection<Object>>();
    for (Object iParameter : resultsToMerge) {
      final Map<String, Object> container = (Map<String, Object>) ((Collection<?>) iParameter).iterator().next();
      chunks.put((Long) container.get("node"), (Collection<Object>) container.get("context"));
    }
    final Collection<Object> result = new HashSet<Object>();
    for (Collection<Object> chunk : chunks.values()) {
      result.addAll(chunk);
    }
    return result;
  }
}
