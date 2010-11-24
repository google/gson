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


/**
 * This acts as a "Null Object" pattern for the {@link ExclusionStrategy}.
 * Passing an instance of this class into the {@link ObjectNavigator} will
 * make the {@link ObjectNavigator} parse/visit every field of the object
 * being navigated.
 *
 * @author Joel Leitch
 */
final class NullExclusionStrategy implements ExclusionStrategy {

  public boolean shouldSkipField(FieldAttributes f) {
    return false;
  }

  public boolean shouldSkipClass(Class<?> clazz) {
    return false;
  }
}
