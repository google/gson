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
package com.google.gson;

/**
 * This class holds a number value that is lazily converted to a specific number type
 *
 * @author Inderjeet Singh
 */
@SuppressWarnings("serial")
final class LazilyParsedNumber extends Number {
  private final String value;

  LazilyParsedNumber(String value) {
    this.value = value;
  }

  @Override
  public int intValue() {
    return Integer.parseInt(value);
  }

  @Override
  public long longValue() {
    return Long.parseLong(value);
  }

  @Override
  public float floatValue() {
    return Float.parseFloat(value);
  }

  @Override
  public double doubleValue() {
    return Double.parseDouble(value);
  }

  @Override
  public String toString() {
    return value;
  }
}