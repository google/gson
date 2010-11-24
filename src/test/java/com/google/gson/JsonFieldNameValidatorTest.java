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

package com.google.gson;

import junit.framework.TestCase;

/**
 * Unit tests for the {@link JsonFieldNameValidator} class.
 *
 * @author Joel Leitch
 */
public class JsonFieldNameValidatorTest extends TestCase {

  private JsonFieldNameValidator validator;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    validator = new JsonFieldNameValidator();
  }

  public void testValidFieldBeginsWithDollarSign() throws Exception {
    String fieldName = "$abc";
    assertEquals(fieldName, validator.validate(fieldName));
  }

  public void testValidFieldBeginsWithUnderscore() throws Exception {
    String fieldName = "_abc";
    assertEquals(fieldName, validator.validate(fieldName));
  }

  public void testValidFieldBeginsWithLetter() throws Exception {
    String fieldName = "abc";
    assertEquals(fieldName, validator.validate(fieldName));
  }

  public void testValidFieldMixingLetter() throws Exception {
    String fieldName = "$abc_12v$34";
    assertEquals(fieldName, validator.validate(fieldName));
  }

  public void testInvalidFieldStartingWithNumbers() throws Exception {
    try {
      validator.validate("1abc");
      fail("Json field name can not start with a number");
    } catch (IllegalArgumentException expected) { }
  }

  public void testInvalidFieldStartingTwoDollarSigns() throws Exception {
    try {
      validator.validate("$$abc");
      fail("Json field name can not start with a double dollar sign");
    } catch (IllegalArgumentException expected) { }
  }

  public void testInvalidFieldStartingTwoUnderscores() throws Exception {
    try {
      validator.validate("__abc");
      fail("Json field name can not start with a double underscore");
    } catch (IllegalArgumentException expected) { }
  }

  public void testInvalidFieldStartingDollarUnderscore() throws Exception {
    try {
      validator.validate("$_abc");
      fail("Json field name can not start with two non-alphabet characters");
    } catch (IllegalArgumentException expected) { }
  }

  public void testJavaAndJsKeywordAsFieldName() throws Exception {
    String fieldName = "break";
    assertEquals(fieldName, validator.validate(fieldName));
  }

  public void testInvalidCharacters() throws Exception {
    try {
      validator.validate("abc.123");
      fail("Json field name can not contain a period character");
    } catch (IllegalArgumentException expected) { }
  }
  
  public void testDashesInFieldName() throws Exception {
    String fieldName = "test-field-name";
    assertEquals(fieldName, validator.validate(fieldName));
  }
  
  public void testSpacesInFieldName() throws Exception {
    String fieldName = "test field name";
    assertEquals(fieldName, validator.validate(fieldName));
  }
  
  public void testSpacesInBeginningOfName() throws Exception {
    try {
      validator.validate(" testFieldName");
      fail("Json field name can not contain a period character");
    } catch (IllegalArgumentException expected) { }
  }
}
