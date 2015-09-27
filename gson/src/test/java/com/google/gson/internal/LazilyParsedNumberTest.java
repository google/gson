/*
 * Copyright (C) 2015 Google Inc.
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
package com.google.gson.internal;

import junit.framework.TestCase;

public class LazilyParsedNumberTest extends TestCase {
  public void testHashCode() {
    LazilyParsedNumber n1 = new LazilyParsedNumber("1");
    LazilyParsedNumber n1Another = new LazilyParsedNumber("1");
    assertEquals(n1.hashCode(), n1Another.hashCode());
  }

  public void testEquals() {
    LazilyParsedNumber n1 = new LazilyParsedNumber("1");
    LazilyParsedNumber n1Another = new LazilyParsedNumber("1");
    assertTrue(n1.equals(n1Another));
  }
}
