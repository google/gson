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

package com.google.gson.internal.reflect;

import static com.google.common.truth.Truth.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;
import java.util.Objects;
import org.junit.Test;

public class Java17ReflectionHelperTest {
  @Test
  public void testJava17Record() throws ClassNotFoundException {
    Class<?> unixDomainPrincipalClass = Class.forName("jdk.net.UnixDomainPrincipal");
    // UnixDomainPrincipal is a record
    assertThat(ReflectionHelper.isRecord(unixDomainPrincipalClass)).isTrue();
    // with 2 components
    assertThat(ReflectionHelper.getRecordComponentNames(unixDomainPrincipalClass)).isEqualTo(new String[] {"user", "group"});
    // Check canonical constructor
    Constructor<?> constructor =
        ReflectionHelper.getCanonicalRecordConstructor(unixDomainPrincipalClass);
    assertThat(constructor).isNotNull();
    assertThat(constructor.getParameterTypes()).isEqualTo(new Class<?>[] {UserPrincipal.class, GroupPrincipal.class});
  }

  @Test
  public void testJava17RecordAccessors() throws ReflectiveOperationException {
    // Create an instance of UnixDomainPrincipal, using our custom implementation of UserPrincipal,
    // and GroupPrincipal. Then attempt to access each component of the record using our accessor
    // methods.
    Class<?> unixDomainPrincipalClass = Class.forName("jdk.net.UnixDomainPrincipal");
    Object unixDomainPrincipal =
        ReflectionHelper.getCanonicalRecordConstructor(unixDomainPrincipalClass)
            .newInstance(new PrincipalImpl("user"), new PrincipalImpl("group"));

    String[] componentNames = ReflectionHelper.getRecordComponentNames(unixDomainPrincipalClass);
    assertThat(componentNames.length > 0).isTrue();

    for (String componentName : componentNames) {
      Field componentField = unixDomainPrincipalClass.getDeclaredField(componentName);
      Method accessor = ReflectionHelper.getAccessor(unixDomainPrincipalClass, componentField);
      Object principal = accessor.invoke(unixDomainPrincipal);

      assertThat(principal).isEqualTo(new PrincipalImpl(componentName));
    }
  }

  /** Implementation of {@link UserPrincipal} and {@link GroupPrincipal} just for record tests. */
  public static class PrincipalImpl implements UserPrincipal, GroupPrincipal {
    private final String name;

    public PrincipalImpl(String name) {
      this.name = name;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof PrincipalImpl) {
        return Objects.equals(name, ((PrincipalImpl) o).name);
      }
      return false;
    }

    @Override
    public int hashCode() {
      return Objects.hash(name);
    }
  }
}
