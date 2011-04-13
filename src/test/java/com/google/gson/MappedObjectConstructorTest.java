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

import junit.framework.TestCase;

import java.lang.reflect.Type;

/**
 * Unit tests for the {@link MappedObjectConstructor} class.
 *
 * @author Joel Leitch
 */
public class MappedObjectConstructorTest extends TestCase {
  private ParameterizedTypeHandlerMap<InstanceCreator<?>> creatorMap;
  private MappedObjectConstructor constructor;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    creatorMap = new ParameterizedTypeHandlerMap<InstanceCreator<?>>();
    constructor = new MappedObjectConstructor(creatorMap);
  }

  public void testInstanceCreatorTakesTopPrecedence() throws Exception {
    creatorMap.register(ObjectWithDefaultConstructor.class, new MyInstanceCreator());
    ObjectWithDefaultConstructor obj =
        constructor.construct(ObjectWithDefaultConstructor.class);
    assertEquals("instanceCreator", obj.stringValue);
    assertEquals(10, obj.intValue);
  }

  public void testNoInstanceCreatorInvokesDefaultConstructor() throws Exception {
    ObjectWithDefaultConstructor expected = new ObjectWithDefaultConstructor();
    ObjectWithDefaultConstructor obj =
        constructor.construct(ObjectWithDefaultConstructor.class);
    assertEquals(expected.stringValue, obj.stringValue);
    assertEquals(expected.intValue, obj.intValue);
  }

  public void testNoDefaultConstructor() throws Exception {
    ObjectNoDefaultConstructor obj = constructor.construct(ObjectNoDefaultConstructor.class);
    assertNull(obj.stringValue);
    assertEquals(0, obj.intValue);
  }

  private static class MyInstanceCreator
      implements InstanceCreator<ObjectWithDefaultConstructor> {
    public ObjectWithDefaultConstructor createInstance(Type type) {
      return new ObjectWithDefaultConstructor("instanceCreator", 10);
    }
  }

  private static class ObjectWithDefaultConstructor {
    public final String stringValue;
    public final int intValue;

    private ObjectWithDefaultConstructor() {
      this("default", 5);
    }

    public ObjectWithDefaultConstructor(String stringValue, int intValue) {
      this.stringValue = stringValue;
      this.intValue = intValue;
    }
  }

  private static class ObjectNoDefaultConstructor extends ObjectWithDefaultConstructor {
    @SuppressWarnings("unused")
    public ObjectNoDefaultConstructor(String stringValue, int intValue) {
      super(stringValue, intValue);
    }
  }
}
