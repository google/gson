/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.functional;

import static com.google.gson.FieldNamingPolicy.IDENTITY;
import static com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_DASHES;
import static com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES;
import static com.google.gson.FieldNamingPolicy.UPPER_CAMEL_CASE;
import static com.google.gson.FieldNamingPolicy.UPPER_CAMEL_CASE_WITH_SPACES;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import junit.framework.TestCase;

public final class FieldNamingTest extends TestCase {
  public void testIdentity() {
    Gson gson = new GsonBuilder().setFieldNamingPolicy(IDENTITY).create();
    assertEquals("{'lowerCamel':1,'UpperCamel':2,'_lowerCamelLeadingUnderscore':3," +
        "'_UpperCamelLeadingUnderscore':4,'lower_words':5,'UPPER_WORDS':6," +
        "'annotatedName':7}",
        gson.toJson(new TestNames()).replace('\"', '\''));
  }

  public void testUpperCamelCase() {
    Gson gson = new GsonBuilder().setFieldNamingPolicy(UPPER_CAMEL_CASE).create();
    assertEquals("{'LowerCamel':1,'UpperCamel':2,'_LowerCamelLeadingUnderscore':3," +
        "'_UpperCamelLeadingUnderscore':4,'Lower_words':5,'UPPER_WORDS':6," +
        "'annotatedName':7}",
        gson.toJson(new TestNames()).replace('\"', '\''));
  }

  public void testUpperCamelCaseWithSpaces() {
    Gson gson = new GsonBuilder().setFieldNamingPolicy(UPPER_CAMEL_CASE_WITH_SPACES).create();
    assertEquals("{'Lower Camel':1,'Upper Camel':2,'_Lower Camel Leading Underscore':3," +
        "'_ Upper Camel Leading Underscore':4,'Lower_words':5,'U P P E R_ W O R D S':6," +
        "'annotatedName':7}",
        gson.toJson(new TestNames()).replace('\"', '\''));
  }

  public void testLowerCaseWithUnderscores() {
    Gson gson = new GsonBuilder().setFieldNamingPolicy(LOWER_CASE_WITH_UNDERSCORES).create();
    assertEquals("{'lower_camel':1,'upper_camel':2,'_lower_camel_leading_underscore':3," +
        "'__upper_camel_leading_underscore':4,'lower_words':5,'u_p_p_e_r__w_o_r_d_s':6," +
        "'annotatedName':7}",
        gson.toJson(new TestNames()).replace('\"', '\''));
  }

  public void testLowerCaseWithDashes() {
    Gson gson = new GsonBuilder().setFieldNamingPolicy(LOWER_CASE_WITH_DASHES).create();
    assertEquals("{'lower-camel':1,'upper-camel':2,'_lower-camel-leading-underscore':3," +
        "'_-upper-camel-leading-underscore':4,'lower_words':5,'u-p-p-e-r_-w-o-r-d-s':6," +
        "'annotatedName':7}",
        gson.toJson(new TestNames()).replace('\"', '\''));
  }

  @SuppressWarnings("unused") // fields are used reflectively
  private static class TestNames {
    int lowerCamel = 1;
    int UpperCamel = 2;
    int _lowerCamelLeadingUnderscore = 3;
    int _UpperCamelLeadingUnderscore = 4;
    int lower_words = 5;
    int UPPER_WORDS = 6;
    @SerializedName("annotatedName") int annotated = 7;
  }
}
