/*
 * Copyright (C) 2024 Google Inc.
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

@SuppressWarnings("requires-automatic") // for automatic module names for 'junit' and 'truth'
module com.google.gson.jpms_test {
  requires com.google.gson;

  // Test dependencies
  requires junit;
  requires truth; // has no proper module name yet, see https://github.com/google/truth/issues/605

  opens com.google.gson.jpms_test to
      junit;
  opens com.google.gson.jpms_test.opened to
      junit,
      com.google.gson;
}
