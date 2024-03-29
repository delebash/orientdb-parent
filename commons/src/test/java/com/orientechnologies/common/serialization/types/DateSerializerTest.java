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

package com.orientechnologies.common.serialization.types;

import java.util.Calendar;
import java.util.Date;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.orientechnologies.common.directmemory.ODirectMemoryPointer;

/**
 * @author ibershadskiy <a href="mailto:ibersh20@gmail.com">Ilya Bershadskiy</a>
 * @since 20.01.12
 */
@Test
public class DateSerializerTest {
  private final static int FIELD_SIZE = 8;
  private final Date       OBJECT     = new Date();
  private ODateSerializer  dateSerializer;
  private final byte[]     stream     = new byte[FIELD_SIZE];

  @BeforeClass
  public void beforeClass() {
    dateSerializer = new ODateSerializer();
  }

  public void testFieldSize() {
    Assert.assertEquals(dateSerializer.getObjectSize(OBJECT), FIELD_SIZE);
  }

  public void testSerialize() {
    dateSerializer.serialize(OBJECT, stream, 0);
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(OBJECT);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    Assert.assertEquals(dateSerializer.deserialize(stream, 0), calendar.getTime());
  }

  public void testSerializeNative() {
    dateSerializer.serializeNative(OBJECT, stream, 0);
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(OBJECT);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    Assert.assertEquals(dateSerializer.deserializeNative(stream, 0), calendar.getTime());
  }

  public void testNativeDirectMemoryCompatibility() {
    dateSerializer.serializeNative(OBJECT, stream, 0);
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(OBJECT);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);

    ODirectMemoryPointer pointer = new ODirectMemoryPointer(stream);
    try {
      Assert.assertEquals(dateSerializer.deserializeFromDirectMemory(pointer, 0), calendar.getTime());
    } finally {
      pointer.free();
    }
  }
}
