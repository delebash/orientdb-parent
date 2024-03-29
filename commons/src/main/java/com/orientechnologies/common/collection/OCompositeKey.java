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
package com.orientechnologies.common.collection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.orientechnologies.common.comparator.ODefaultComparator;

/**
 * Container for the list of heterogeneous values that are going to be stored in {@link OMVRBTree} as composite keys.
 * 
 * Such keys is allowed to use only in {@link OMVRBTree#subMap(Object, boolean, Object, boolean)},
 * {@link OMVRBTree#tailMap(Object, boolean)} and {@link OMVRBTree#headMap(Object, boolean)} methods.
 * 
 * @see OMVRBTree.PartialSearchMode
 * @author Andrey lomakin, Artem Orobets
 */
public class OCompositeKey implements Comparable<OCompositeKey>, Serializable {
  private static final long        serialVersionUID = 1L;
  /**
   * List of heterogeneous values that are going to be stored in {@link OMVRBTree}.
   */
  private final List<Object>       keys;

  private final Comparator<Object> comparator;

  public OCompositeKey(final List<?> keys) {
    this.keys = new ArrayList<Object>(keys.size());
    this.comparator = ODefaultComparator.INSTANCE;

    for (final Object key : keys)
      addKey(key);
  }

  public OCompositeKey(final Object... keys) {
    this.keys = new ArrayList<Object>(keys.length);
    this.comparator = ODefaultComparator.INSTANCE;
    for (final Object key : keys)
      addKey(key);
  }

  public OCompositeKey() {
    this.keys = new ArrayList<Object>();
    this.comparator = ODefaultComparator.INSTANCE;
  }

  /**
   * Clears the keys array for reuse of the object
   */
  public void reset() {
    if (this.keys != null)
      this.keys.clear();
  }

  /**
   * @return List of heterogeneous values that are going to be stored in {@link OMVRBTree}.
   */
  public List<Object> getKeys() {
    return Collections.unmodifiableList(keys);
  }

  /**
   * Add new key value to the list of already registered values.
   * 
   * If passed in value is {@link OCompositeKey} itself then its values will be copied in current index. But key itself will not be
   * added.
   * 
   * @param key
   *          Key to add.
   */
  public void addKey(final Object key) {
    if (key instanceof OCompositeKey) {
      final OCompositeKey compositeKey = (OCompositeKey) key;
      for (final Object inKey : compositeKey.keys) {
        addKey(inKey);
      }
    } else {
      keys.add(key);
    }
  }

  /**
   * Performs partial comparison of two composite keys.
   * 
   * Two objects will be equal if the common subset of their keys is equal. For example if first object contains two keys and second
   * contains four keys then only first two keys will be compared.
   * 
   * @param otherKey
   *          Key to compare.
   * 
   * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified
   *         object.
   */
  public int compareTo(final OCompositeKey otherKey) {
    final Iterator<Object> inIter = keys.iterator();
    final Iterator<Object> outIter = otherKey.keys.iterator();

    while (inIter.hasNext() && outIter.hasNext()) {
      final Object inKey = inIter.next();
      final Object outKey = outIter.next();

      if (outKey instanceof OAlwaysGreaterKey)
        return -1;

      if (outKey instanceof OAlwaysLessKey)
        return 1;

      final int result = comparator.compare(inKey, outKey);
      if (result != 0)
        return result;
    }

    return 0;
  }

  /**
   * {@inheritDoc }
   */
  @Override
  public boolean equals(final Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    final OCompositeKey that = (OCompositeKey) o;

    return keys.equals(that.keys);
  }

  /**
   * {@inheritDoc }
   */
  @Override
  public int hashCode() {
    return keys.hashCode();
  }

  /**
   * {@inheritDoc }
   */
  @Override
  public String toString() {
    return "OCompositeKey{" + "keys=" + keys + '}';
  }
}
