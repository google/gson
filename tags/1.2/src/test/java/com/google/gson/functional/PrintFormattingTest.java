/*
 * Copyright (C) 2008 Google Inc.
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

package com.google.gson.functional;

import com.google.gson.Gson;
import com.google.gson.common.TestTypes.BagOfPrimitives;
import com.google.gson.common.TestTypes.ClassWithTransientFields;
import com.google.gson.common.TestTypes.Nested;
import com.google.gson.common.TestTypes.PrimitiveArray;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Functional tests for print formatting.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class PrintFormattingTest extends TestCase {

  @SuppressWarnings("unchecked")
  public void testCompactFormattingLeavesNoWhiteSpace() {
    Gson gson = new Gson();
    List list = new ArrayList();
    list.add(new BagOfPrimitives());
    list.add(new Nested());
    list.add(new PrimitiveArray());
    list.add(new ClassWithTransientFields());

    String json = gson.toJson(list);
    assertContainsNoWhiteSpace(json);
  }

  private static void assertContainsNoWhiteSpace(String str) {
    for (char c : str.toCharArray()) {
      assertFalse(Character.isWhitespace(c));
    }
  }
}
