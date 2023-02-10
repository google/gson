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

package com.google.gson.reflect;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;
import org.junit.Test;

/**
 * @author Jesse Wilson
 */
public final class TypeTokenTest {
  // These fields are accessed using reflection by the tests below
  List<Integer> listOfInteger = null;
  List<Number> listOfNumber = null;
  List<String> listOfString = null;
  List<?> listOfUnknown = null;
  List<Set<String>> listOfSetOfString = null;
  List<Set<?>> listOfSetOfUnknown = null;

  @SuppressWarnings({"deprecation"})
  @Test
  public void testIsAssignableFromRawTypes() {
    assertThat(TypeToken.get(Object.class).isAssignableFrom(String.class)).isTrue();
    assertThat(TypeToken.get(String.class).isAssignableFrom(Object.class)).isFalse();
    assertThat(TypeToken.get(RandomAccess.class).isAssignableFrom(ArrayList.class)).isTrue();
    assertThat(TypeToken.get(ArrayList.class).isAssignableFrom(RandomAccess.class)).isFalse();
  }

  @SuppressWarnings({"deprecation"})
  @Test
  public void testIsAssignableFromWithTypeParameters() throws Exception {
    Type a = getClass().getDeclaredField("listOfInteger").getGenericType();
    Type b = getClass().getDeclaredField("listOfNumber").getGenericType();
    assertThat(TypeToken.get(a).isAssignableFrom(a)).isTrue();
    assertThat(TypeToken.get(b).isAssignableFrom(b)).isTrue();

    // listOfInteger = listOfNumber; // doesn't compile; must be false
    assertThat(TypeToken.get(a).isAssignableFrom(b)).isFalse();
    // listOfNumber = listOfInteger; // doesn't compile; must be false
    assertThat(TypeToken.get(b).isAssignableFrom(a)).isFalse();
  }

  @SuppressWarnings({"deprecation"})
  @Test
  public void testIsAssignableFromWithBasicWildcards() throws Exception {
    Type a = getClass().getDeclaredField("listOfString").getGenericType();
    Type b = getClass().getDeclaredField("listOfUnknown").getGenericType();
    assertThat(TypeToken.get(a).isAssignableFrom(a)).isTrue();
    assertThat(TypeToken.get(b).isAssignableFrom(b)).isTrue();

    // listOfString = listOfUnknown  // doesn't compile; must be false
    assertThat(TypeToken.get(a).isAssignableFrom(b)).isFalse();
    listOfUnknown = listOfString; // compiles; must be true
    // The following assertion is too difficult to support reliably, so disabling
    // assertThat(TypeToken.get(b).isAssignableFrom(a)).isTrue();
  }

  @SuppressWarnings({"deprecation"})
  @Test
  public void testIsAssignableFromWithNestedWildcards() throws Exception {
    Type a = getClass().getDeclaredField("listOfSetOfString").getGenericType();
    Type b = getClass().getDeclaredField("listOfSetOfUnknown").getGenericType();
    assertThat(TypeToken.get(a).isAssignableFrom(a)).isTrue();
    assertThat(TypeToken.get(b).isAssignableFrom(b)).isTrue();

    // listOfSetOfString = listOfSetOfUnknown; // doesn't compile; must be false
    assertThat(TypeToken.get(a).isAssignableFrom(b)).isFalse();
    // listOfSetOfUnknown = listOfSetOfString; // doesn't compile; must be false
    assertThat(TypeToken.get(b).isAssignableFrom(a)).isFalse();
  }

  @Test
  public void testArrayFactory() {
    TypeToken<?> expectedStringArray = new TypeToken<String[]>() {};
    assertThat(TypeToken.getArray(String.class)).isEqualTo(expectedStringArray);

    TypeToken<?> expectedListOfStringArray = new TypeToken<List<String>[]>() {};
    Type listOfString = new TypeToken<List<String>>() {}.getType();
    assertThat(TypeToken.getArray(listOfString)).isEqualTo(expectedListOfStringArray);

    try {
      TypeToken.getArray(null);
      fail();
    } catch (NullPointerException e) {
    }
  }

  @Test
  public void testParameterizedFactory() {
    TypeToken<?> expectedListOfString = new TypeToken<List<String>>() {};
    assertThat(TypeToken.getParameterized(List.class, String.class)).isEqualTo(expectedListOfString);

    TypeToken<?> expectedMapOfStringToString = new TypeToken<Map<String, String>>() {};
    assertThat(TypeToken.getParameterized(Map.class, String.class, String.class)).isEqualTo(expectedMapOfStringToString);

    TypeToken<?> expectedListOfListOfListOfString = new TypeToken<List<List<List<String>>>>() {};
    Type listOfString = TypeToken.getParameterized(List.class, String.class).getType();
    Type listOfListOfString = TypeToken.getParameterized(List.class, listOfString).getType();
    assertThat(TypeToken.getParameterized(List.class, listOfListOfString)).isEqualTo(expectedListOfListOfListOfString);

    TypeToken<?> expectedWithExactArg = new TypeToken<GenericWithBound<Number>>() {};
    assertThat(TypeToken.getParameterized(GenericWithBound.class, Number.class)).isEqualTo(expectedWithExactArg);

    TypeToken<?> expectedWithSubclassArg = new TypeToken<GenericWithBound<Integer>>() {};
    assertThat(TypeToken.getParameterized(GenericWithBound.class, Integer.class)).isEqualTo(expectedWithSubclassArg);

    TypeToken<?> expectedSatisfyingTwoBounds = new TypeToken<GenericWithMultiBound<ClassSatisfyingBounds>>() {};
    assertThat(TypeToken.getParameterized(GenericWithMultiBound.class, ClassSatisfyingBounds.class)).isEqualTo(expectedSatisfyingTwoBounds);
  }

  @Test
  public void testParameterizedFactory_Invalid() {
    try {
      TypeToken.getParameterized(null, new Type[0]);
      fail();
    } catch (NullPointerException e) {
    }

    GenericArrayType arrayType = (GenericArrayType) TypeToken.getArray(String.class).getType();
    try {
      TypeToken.getParameterized(arrayType, new Type[0]);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat().isEqualTo("rawType must be of type Class, but was java.lang.String[]");
    }

    try {
      TypeToken.getParameterized(String.class, String.class);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat().isEqualTo("java.lang.String requires 0 type arguments, but got 1");
    }

    try {
      TypeToken.getParameterized(List.class, new Type[0]);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat().isEqualTo("java.util.List requires 1 type arguments, but got 0");
    }

    try {
      TypeToken.getParameterized(List.class, String.class, String.class);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat().isEqualTo("java.util.List requires 1 type arguments, but got 2");
    }

    try {
      TypeToken.getParameterized(GenericWithBound.class, String.class);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat().isEqualTo("Type argument class java.lang.String does not satisfy bounds "
          + "for type variable T declared by " + GenericWithBound.class);
    }

    try {
      TypeToken.getParameterized(GenericWithBound.class, Object.class);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat().isEqualTo("Type argument class java.lang.Object does not satisfy bounds "
          + "for type variable T declared by " + GenericWithBound.class);
    }

    try {
      TypeToken.getParameterized(GenericWithMultiBound.class, Number.class);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat().isEqualTo("Type argument class java.lang.Number does not satisfy bounds "
          + "for type variable T declared by " + GenericWithMultiBound.class);
    }

    try {
      TypeToken.getParameterized(GenericWithMultiBound.class, CharSequence.class);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat().isEqualTo("Type argument interface java.lang.CharSequence does not satisfy bounds "
          + "for type variable T declared by " + GenericWithMultiBound.class);
    }

    try {
      TypeToken.getParameterized(GenericWithMultiBound.class, Object.class);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat().isEqualTo("Type argument class java.lang.Object does not satisfy bounds "
          + "for type variable T declared by " + GenericWithMultiBound.class);
    }
  }

  private static class CustomTypeToken extends TypeToken<String> {
  }

  @Test
  public void testTypeTokenNonAnonymousSubclass() {
    TypeToken<?> typeToken = new CustomTypeToken();
    assertThat(typeToken.getRawType()).isEqualTo(String.class);
    assertThat(typeToken.getType()).isEqualTo(String.class);
  }

  /**
   * User must only create direct subclasses of TypeToken, but not subclasses
   * of subclasses (...) of TypeToken.
   */
  @Test
  public void testTypeTokenSubSubClass() {
    class SubTypeToken<T> extends TypeToken<String> {}
    class SubSubTypeToken1<T> extends SubTypeToken<T> {}
    class SubSubTypeToken2 extends SubTypeToken<Integer> {}

    try {
      new SubTypeToken<Integer>() {};
      fail();
    } catch (IllegalStateException expected) {
      assertThat(expected.getMessage()).isEqualTo("Must only create direct subclasses of TypeToken");
    }

    try {
      new SubSubTypeToken1<Integer>();
      fail();
    } catch (IllegalStateException expected) {
      assertThat(expected.getMessage()).isEqualTo("Must only create direct subclasses of TypeToken");
    }

    try {
      new SubSubTypeToken2();
      fail();
    } catch (IllegalStateException expected) {
      assertThat(expected.getMessage()).isEqualTo("Must only create direct subclasses of TypeToken");
    }
  }

  @SuppressWarnings("rawtypes")
  @Test
  public void testTypeTokenRaw() {
    try {
      new TypeToken() {};
      fail();
    } catch (IllegalStateException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("TypeToken must be created with a type argument: new TypeToken<...>() {}; "
          + "When using code shrinkers (ProGuard, R8, ...) make sure that generic signatures are preserved.");
    }
  }
}

// Have to declare these classes here as top-level classes because otherwise tests for
// TypeToken.getParameterized fail due to owner type mismatch
class GenericWithBound<T extends Number> {
}
class GenericWithMultiBound<T extends Number & CharSequence> {
}
@SuppressWarnings("serial")
abstract class ClassSatisfyingBounds extends Number implements CharSequence {
}
