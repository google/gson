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
package com.google.gson.webservice.definition;

import com.google.gson.rest.definition.ID;

import junit.framework.TestCase;

/**
 * Unit test for {@link CallPath}
 *
 * @author inder
 */
public class CallPathTest extends TestCase {

  public void testVersionIsSkipped() {
    CallPath path = new CallPath("/1.0/rest/service1");
    assertEquals("/rest/service1", path.get());
    assertEquals(1D, path.getVersion());
    assertEquals(ID.INVALID_ID, path.getResourceId());
  }

  public void testVersionNotPresent() {
    CallPath path = new CallPath("/rest/service1");
    assertEquals("/rest/service1", path.get());
    assertEquals(CallPath.IGNORE_VERSION, path.getVersion());
    assertEquals(ID.INVALID_ID, path.getResourceId());
  }
  
  public void testResourceIdPresent() {
    CallPath path = new CallPath("/rest/service/3");
    assertEquals("/rest/service", path.get());
    assertEquals(3L, path.getResourceId());
  }

  public void testResourceIdWithEndSlashPresent() {
    CallPath path = new CallPath("/rest/service/3/");
    assertEquals("/rest/service", path.get());
    assertEquals(3L, path.getResourceId());
  }

  public void testVersionAndResourceIdPresent() {
    CallPath path = new CallPath("/3.1/rest/service53/323222");
    assertEquals(3.1D, path.getVersion());
    assertEquals("/rest/service53", path.get());
    assertEquals(323222L, path.getResourceId());
  }

  public void testNullPath() {
    CallPath path = new CallPath(null);
    assertEquals(CallPath.IGNORE_VERSION, path.getVersion());
    assertEquals(ID.INVALID_ID, path.getResourceId());
    assertNull(path.get());
  }

  public void testEmptyPath() {
    CallPath path = new CallPath("");
    assertEquals(CallPath.IGNORE_VERSION, path.getVersion());
    assertEquals(ID.INVALID_ID, path.getResourceId());
    assertEquals("", path.get());
  }

  public void testWhiteSpacePath() {
    CallPath path = new CallPath("\r\n");
    assertEquals(CallPath.IGNORE_VERSION, path.getVersion());
    assertEquals(ID.INVALID_ID, path.getResourceId());
    assertEquals("\r\n", path.get());
  }
}
