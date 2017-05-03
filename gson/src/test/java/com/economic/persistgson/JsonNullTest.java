/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.economic.persistgson;

import com.economic.persistgson.common.MoreAsserts;
import junit.framework.TestCase;

/**
 * @author Jesse Wilson
 */
public final class JsonNullTest extends TestCase {

  @SuppressWarnings("deprecation")
  public void testEqualsAndHashcode() {
    MoreAsserts.assertEqualsAndHashCode(new com.economic.persistgson.JsonNull(), new com.economic.persistgson.JsonNull());
    MoreAsserts.assertEqualsAndHashCode(new com.economic.persistgson.JsonNull(), com.economic.persistgson.JsonNull.INSTANCE);
    MoreAsserts.assertEqualsAndHashCode(com.economic.persistgson.JsonNull.INSTANCE, com.economic.persistgson.JsonNull.INSTANCE);
  }

  public void testDeepCopy() {
    @SuppressWarnings("deprecation")
    com.economic.persistgson.JsonNull a = new com.economic.persistgson.JsonNull();
    assertSame(com.economic.persistgson.JsonNull.INSTANCE, a.deepCopy());
    assertSame(com.economic.persistgson.JsonNull.INSTANCE, com.economic.persistgson.JsonNull.INSTANCE.deepCopy());
  }
}
