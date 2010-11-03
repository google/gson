/*
 * Copyright (C) 2010 Google Inc.
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
package com.google.gson.wsf.inject;

import junit.framework.TestCase;

/**
 * Unit test for {@link CallPathProvider}
 *
 * @author Inderjeet Singh
 */
public class CallPathProviderTest extends TestCase {

  public void testVersionIsSkipped() {
    CallPathProvider provider = new CallPathProvider("/1.0/rest/service1");
    assertEquals("/rest/service1", provider.get().get());
  }

  public void testVersionNotPresent() {
    CallPathProvider provider = new CallPathProvider("/rest/service1");
    assertEquals("/rest/service1", provider.get().get());
  }
}
