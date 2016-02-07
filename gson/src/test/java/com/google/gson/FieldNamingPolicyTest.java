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

package com.google.gson;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

/**
 * Unit test for the {@link FieldNamingPolicy} class
 * 
 * @author David Betancourt
 *
 */
@RunWith(JUnitParamsRunner.class)
public class FieldNamingPolicyTest {

  private Object[] getUpperCaseFirstLetterParams() {
    return new Object[] { getUpperCaseFirstLetterParamArgs("someFieldName", "SomeFieldName"),
        getUpperCaseFirstLetterParamArgs("SomeFieldName", "SomeFieldName"),
        getUpperCaseFirstLetterParamArgs("_someFieldName", "_SomeFieldName"),
        getUpperCaseFirstLetterParamArgs("_SomeFieldName", "_SomeFieldName"),
        getUpperCaseFirstLetterParamArgs("_999someFieldName", "_999SomeFieldName"),
        getUpperCaseFirstLetterParamArgs("_999SomeFieldName", "_999SomeFieldName"),
        getUpperCaseFirstLetterParamArgs("x", "X"),
        getUpperCaseFirstLetterParamArgs("X", "X"),
        getUpperCaseFirstLetterParamArgs("555", "555"),
        getUpperCaseFirstLetterParamArgs("_", "_") };

  }

  private Object[] getUpperCaseFirstLetterParamArgs(String fieldName, String expectedFieldName) {
    return new Object[] { fieldName, expectedFieldName };
  }

  @Test
  @Parameters(method = "getUpperCaseFirstLetterParams")
  public void testUpperCaseFieldLetter(String fieldName, String expectedFieldName) {
    String actualFieldName = FieldNamingPolicy.upperCaseFirstLetter(fieldName);
    assertEquals("Verify '" + fieldName + "' was uppercased properly", expectedFieldName,
        actualFieldName);
  }
}
