/*
 * Copyright (C) 2014 Google Inc.
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

package com.google.gson.internal;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import org.junit.Test;

public final class GsonTypesTest {

  @Test
  public void testNewParameterizedTypeWithoutOwner() throws Exception {
    // List<A>. List is a top-level class
    Type type = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, A.class);
    assertThat(getFirstTypeArgument(type)).isEqualTo(A.class);

    // A<B>. A is a static inner class.
    type = $Gson$Types.newParameterizedTypeWithOwner(null, A.class, B.class);
    assertThat(getFirstTypeArgument(type)).isEqualTo(B.class);

    final class D {
    }
    try {
      // D<A> is not allowed since D is not a static inner class
      $Gson$Types.newParameterizedTypeWithOwner(null, D.class, A.class);
      fail();
    } catch (IllegalArgumentException expected) {}

    // A<D> is allowed.
    type = $Gson$Types.newParameterizedTypeWithOwner(null, A.class, D.class);
    assertThat(getFirstTypeArgument(type)).isEqualTo(D.class);
  }

  @Test
  public void testGetFirstTypeArgument() throws Exception {
    assertThat(getFirstTypeArgument(A.class)).isNull();

    Type type = $Gson$Types.newParameterizedTypeWithOwner(null, A.class, B.class, C.class);
    assertThat(getFirstTypeArgument(type)).isEqualTo(B.class);
  }

  private static final class A {
  }
  private static final class B {
  }
  private static final class C {
  }

  /**
   * Given a parameterized type A&lt;B,C&gt;, returns B. If the specified type is not
   * a generic type, returns null.
   */
  public static Type getFirstTypeArgument(Type type) throws Exception {
    if (!(type instanceof ParameterizedType)) return null;
    ParameterizedType ptype = (ParameterizedType) type;
    Type[] actualTypeArguments = ptype.getActualTypeArguments();
    if (actualTypeArguments.length == 0) return null;
    return $Gson$Types.canonicalize(actualTypeArguments[0]);
  }
}
