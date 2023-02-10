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

import static com.google.common.truth.Truth.assertThat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.common.TestTypes.BagOfPrimitives;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for security-related aspects of Gson
 * 
 * @author Inderjeet Singh
 */
public class SecurityTest {
  /**
   * Keep this in sync with Gson.JSON_NON_EXECUTABLE_PREFIX
   */
  private static final String JSON_NON_EXECUTABLE_PREFIX = ")]}'\n";

  private GsonBuilder gsonBuilder;

  @Before
  public void setUp() throws Exception {
    gsonBuilder = new GsonBuilder();
  }

  @Test
  public void testNonExecutableJsonSerialization() {
    Gson gson = gsonBuilder.generateNonExecutableJson().create();
    String json = gson.toJson(new BagOfPrimitives());
    assertThat(json.startsWith(JSON_NON_EXECUTABLE_PREFIX)).isTrue();
  }
  
  @Test
  public void testNonExecutableJsonDeserialization() {
    String json = JSON_NON_EXECUTABLE_PREFIX + "{longValue:1}";
    Gson gson = gsonBuilder.create();
    BagOfPrimitives target = gson.fromJson(json, BagOfPrimitives.class);
    assertThat(target.longValue).isEqualTo(1);
  }
  
  @Test
  public void testJsonWithNonExectuableTokenSerialization() {
    Gson gson = gsonBuilder.generateNonExecutableJson().create();
    String json = gson.toJson(JSON_NON_EXECUTABLE_PREFIX);
    assertThat(json).contains(")]}'\n");
  }
  
  /**
   *  Gson should be able to deserialize a stream with non-exectuable token even if it is created
   *  without {@link GsonBuilder#generateNonExecutableJson()}.
   */
  @Test
  public void testJsonWithNonExectuableTokenWithRegularGsonDeserialization() {
    Gson gson = gsonBuilder.create();
    String json = JSON_NON_EXECUTABLE_PREFIX + "{stringValue:')]}\\u0027\\n'}";
    BagOfPrimitives target = gson.fromJson(json, BagOfPrimitives.class);
    assertThat(target.stringValue).isEqualTo(")]}'\n");
  }  
  
  /**
   *  Gson should be able to deserialize a stream with non-exectuable token if it is created
   *  with {@link GsonBuilder#generateNonExecutableJson()}.
   */
  @Test
  public void testJsonWithNonExectuableTokenWithConfiguredGsonDeserialization() {
    // Gson should be able to deserialize a stream with non-exectuable token even if it is created 
    Gson gson = gsonBuilder.generateNonExecutableJson().create();
    String json = JSON_NON_EXECUTABLE_PREFIX + "{intValue:2,stringValue:')]}\\u0027\\n'}";
    BagOfPrimitives target = gson.fromJson(json, BagOfPrimitives.class);
    assertThat(target.stringValue).isEqualTo(")]}'\n");
    assertThat(target.intValue).isEqualTo(2);
  }  
}
