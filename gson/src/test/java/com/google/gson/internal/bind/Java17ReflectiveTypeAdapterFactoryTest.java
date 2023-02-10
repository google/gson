/*
 * Copyright (C) 2022 Google Inc.
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

package com.google.gson.internal.bind;

import static com.google.common.truth.Truth.assertThat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.reflect.Java17ReflectionHelperTest;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;
import java.security.Principal;
import org.junit.Before;
import org.junit.Test;

public class Java17ReflectiveTypeAdapterFactoryTest {

  // The class jdk.net.UnixDomainPrincipal is one of the few Record types that are included in the JDK.
  // We use this to test serialization and deserialization of Record classes, so we do not need to
  // have record support at the language level for these tests. This class was added in JDK 16.
  Class<?> unixDomainPrincipalClass;

  @Before
  public void setUp() throws Exception {
    unixDomainPrincipalClass = Class.forName("jdk.net.UnixDomainPrincipal");
  }

  // Class for which the normal reflection based adapter is used
  private static class DummyClass {
    @SuppressWarnings("unused")
    public String s;
  }

  @Test
  public void testCustomAdapterForRecords() {
    Gson gson = new Gson();
    TypeAdapter<?> recordAdapter = gson.getAdapter(unixDomainPrincipalClass);
    TypeAdapter<?> defaultReflectionAdapter = gson.getAdapter(DummyClass.class);
    assertThat(defaultReflectionAdapter.getClass()).isNotEqualTo(recordAdapter.getClass());
  }

  @Test
  public void testSerializeRecords() throws ReflectiveOperationException {
    Gson gson =
        new GsonBuilder()
            .registerTypeAdapter(UserPrincipal.class, new PrincipalTypeAdapter<>())
            .registerTypeAdapter(GroupPrincipal.class, new PrincipalTypeAdapter<>())
            .create();

    UserPrincipal userPrincipal = gson.fromJson("\"user\"", UserPrincipal.class);
    GroupPrincipal groupPrincipal = gson.fromJson("\"group\"", GroupPrincipal.class);
    Object recordInstance =
        unixDomainPrincipalClass
            .getDeclaredConstructor(UserPrincipal.class, GroupPrincipal.class)
            .newInstance(userPrincipal, groupPrincipal);
    String serialized = gson.toJson(recordInstance);
    Object deserializedRecordInstance = gson.fromJson(serialized, unixDomainPrincipalClass);

    assertThat(deserializedRecordInstance).isEqualTo(recordInstance);
    assertThat(serialized).isEqualTo("{\"user\":\"user\",\"group\":\"group\"}");
  }

  private static class PrincipalTypeAdapter<T extends Principal> extends TypeAdapter<T> {
    @Override
    public void write(JsonWriter out, T principal) throws IOException {
      out.value(principal.getName());
    }

    @Override
    public T read(JsonReader in) throws IOException {
      final String name = in.nextString();
      // This type adapter is only used for Group and User Principal, both of which are implemented by PrincipalImpl.
      @SuppressWarnings("unchecked")
      T principal = (T) new Java17ReflectionHelperTest.PrincipalImpl(name);
      return principal;
    }
  }
}
