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
import static org.junit.Assert.assertThrows;

import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.common.TestTypes.ClassOverridingEquals;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

/**
 * Functional tests related to circular reference detection and error reporting.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class CircularReferenceTest {
  private Gson gson;

  @Before
  public void setUp() throws Exception {
    gson = new Gson();
  }

  @Test
  public void testCircularSerialization() {
    ContainsReferenceToSelfType a = new ContainsReferenceToSelfType();
    ContainsReferenceToSelfType b = new ContainsReferenceToSelfType();
    a.children.add(b);
    b.children.add(a);
    // Circular types should not get printed
    assertThrowsStackOverflow(() -> gson.toJson(a));
  }

  @Test
  public void testSelfReferenceIgnoredInSerialization() {
    ClassOverridingEquals objA = new ClassOverridingEquals();
    objA.ref = objA;

    String json = gson.toJson(objA);
    assertThat(json).doesNotContain("ref"); // self-reference is ignored
  }

  @Test
  public void testSelfReferenceArrayFieldSerialization() {
    ClassWithSelfReferenceArray objA = new ClassWithSelfReferenceArray();
    objA.children = new ClassWithSelfReferenceArray[] {objA};

    // Circular reference to self can not be serialized
    assertThrowsStackOverflow(() -> gson.toJson(objA));
  }

  @Test
  public void testSelfReferenceCustomHandlerSerialization() {
    ClassWithSelfReference obj = new ClassWithSelfReference();
    obj.child = obj;
    Gson gson =
        new GsonBuilder()
            .registerTypeAdapter(
                ClassWithSelfReference.class,
                new JsonSerializer<ClassWithSelfReference>() {
                  @Override
                  public JsonElement serialize(
                      ClassWithSelfReference src,
                      Type typeOfSrc,
                      JsonSerializationContext context) {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("property", "value");
                    obj.add("child", context.serialize(src.child));
                    return obj;
                  }
                })
            .create();

    // Circular reference to self can not be serialized
    assertThrowsStackOverflow(() -> gson.toJson(obj));
  }

  /** Asserts that a {@link StackOverflowError} is thrown. */
  private static void assertThrowsStackOverflow(ThrowingRunnable runnable) {
    // Obtain the root cause because the StackOverflowError might occur in JDK code, and that might
    // wrap it in another exception class, for example InternalError
    Throwable t = assertThrows(Throwable.class, runnable);
    assertThat(Throwables.getRootCause(t)).isInstanceOf(StackOverflowError.class);
  }

  @Test
  public void testDirectedAcyclicGraphSerialization() {
    ContainsReferenceToSelfType a = new ContainsReferenceToSelfType();
    ContainsReferenceToSelfType b = new ContainsReferenceToSelfType();
    ContainsReferenceToSelfType c = new ContainsReferenceToSelfType();
    a.children.add(b);
    a.children.add(c);
    b.children.add(c);
    assertThat(gson.toJson(a)).isNotNull();
  }

  @Test
  public void testDirectedAcyclicGraphDeserialization() {
    String json = "{\"children\":[{\"children\":[{\"children\":[]}]},{\"children\":[]}]}";
    ContainsReferenceToSelfType target = gson.fromJson(json, ContainsReferenceToSelfType.class);
    assertThat(target).isNotNull();
    assertThat(target.children).hasSize(2);
  }

  private static class ContainsReferenceToSelfType {
    Collection<ContainsReferenceToSelfType> children = new ArrayList<>();
  }

  private static class ClassWithSelfReference {
    ClassWithSelfReference child;
  }

  private static class ClassWithSelfReferenceArray {
    @SuppressWarnings("unused")
    ClassWithSelfReferenceArray[] children;
  }
}
