/*
 * Copyright 1999-2012 Luca Garulli (l.garulli--at--orientechnologies.com)
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

package com.orientechnologies.orient.core.id;

import java.io.DataInput;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;

import com.orientechnologies.common.serialization.types.OLongSerializer;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;

/**
 * Factory is used to create new instances of {@link OClusterPosition} class.
 * 
 * @author Andrey Lomakin
 * @since 12.11.12
 */

public abstract class OClusterPositionFactory {
  public static final OClusterPositionFactory INSTANCE;

  static {
    if (OGlobalConfiguration.USE_NODE_ID_CLUSTER_POSITION.getValueAsBoolean())
      INSTANCE = new OClusterPositionFactoryNodeId();
    else
      INSTANCE = new OClusterPositionFactoryLong();
  }

  public abstract OClusterPosition generateUniqueClusterPosition();

  public abstract OClusterPosition valueOf(long value);

  public abstract OClusterPosition valueOf(String value);

  public abstract OClusterPosition fromStream(byte[] content, int start);

  /**
   * @return Size of {@link OClusterPosition} instance in serialized form.
   * @see com.orientechnologies.orient.core.id.OClusterPosition#toStream()
   */
  public abstract int getSerializedSize();

  public OClusterPosition fromStream(byte[] content) {
    return fromStream(content, 0);
  }

  public OClusterPosition fromStream(InputStream in) throws IOException {
    int bytesToRead;
    int contentLength = 0;

    final int clusterSize = OClusterPositionFactory.INSTANCE.getSerializedSize();
    final byte[] clusterContent = new byte[clusterSize];

    do {
      bytesToRead = in.read(clusterContent, contentLength, clusterSize - contentLength);
      if (bytesToRead < 0)
        break;

      contentLength += bytesToRead;
    } while (contentLength < clusterSize);

    return fromStream(clusterContent);
  }

  public OClusterPosition fromStream(ObjectInput in) throws IOException {
    int bytesToRead;
    int contentLength = 0;

    final int clusterSize = OClusterPositionFactory.INSTANCE.getSerializedSize();
    final byte[] clusterContent = new byte[clusterSize];

    do {
      bytesToRead = in.read(clusterContent, contentLength, clusterSize - contentLength);
      if (bytesToRead < 0)
        break;

      contentLength += bytesToRead;
    } while (contentLength < clusterSize);

    return fromStream(clusterContent);
  }

  public OClusterPosition fromStream(DataInput in) throws IOException {
    final int clusterSize = OClusterPositionFactory.INSTANCE.getSerializedSize();
    final byte[] clusterContent = new byte[clusterSize];

    in.readFully(clusterContent);

    return fromStream(clusterContent);
  }

  public abstract OClusterPosition getMaxValue();

  public static final class OClusterPositionFactoryLong extends OClusterPositionFactory {
    @Override
    public OClusterPosition generateUniqueClusterPosition() {
      throw new UnsupportedOperationException();
    }

    @Override
    public OClusterPosition valueOf(long value) {
      return new OClusterPositionLong(value);
    }

    @Override
    public OClusterPosition valueOf(String value) {
      return new OClusterPositionLong(Long.valueOf(value));
    }

    @Override
    public OClusterPosition fromStream(byte[] content, int start) {
      return new OClusterPositionLong(OLongSerializer.INSTANCE.deserialize(content, start));
    }

    @Override
    public int getSerializedSize() {
      return OLongSerializer.LONG_SIZE;
    }

    @Override
    public OClusterPosition getMaxValue() {
      return new OClusterPositionLong(Long.MAX_VALUE);
    }
  }

  public static final class OClusterPositionFactoryNodeId extends OClusterPositionFactory {
    @Override
    public OClusterPosition generateUniqueClusterPosition() {
      return new OClusterPositionNodeId(ONodeId.generateUniqueId());
    }

    @Override
    public OClusterPosition valueOf(long value) {
      return new OClusterPositionNodeId(ONodeId.valueOf(value));
    }

    @Override
    public OClusterPosition valueOf(String value) {
      return new OClusterPositionNodeId(ONodeId.parseString(value));
    }

    @Override
    public OClusterPosition fromStream(byte[] content, int start) {
      return new OClusterPositionNodeId(ONodeId.fromStream(content, start));
    }

    @Override
    public int getSerializedSize() {
      return ONodeId.SERIALIZED_SIZE;
    }

    @Override
    public OClusterPosition getMaxValue() {
      return new OClusterPositionNodeId(ONodeId.MAX_VALUE);
    }
  }

}
