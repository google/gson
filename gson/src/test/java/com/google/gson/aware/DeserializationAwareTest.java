/*
 * Copyright (C) 2016 Google Inc.
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

package com.google.gson.aware;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import junit.framework.TestCase;

/**
 * Unit tests for DeserializationAware class.
 *
 * @author Manish Goyal
 */
public class DeserializationAwareTest extends TestCase {
  private Gson gson;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gson = new GsonBuilder().create();
  }

  public void testOnDeserializationCompleteIsCalledAfterDeserializationIsComplete() {
    DeserializationAwareClass object = gson.fromJson("{a:3}", DeserializationAwareClass.class);
    assertTrue(object.isAPositiveNumber);
  }

  private static class DeserializationAwareClass implements DeserializationAware {
    @Expose
    private int a;

    private boolean isAPositiveNumber;

    @Override
    public void onDeserializationComplete() {
      isAPositiveNumber = a > 0;
    }
  }
}