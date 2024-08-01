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
import static org.junit.Assert.assertThrows;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;
import org.junit.Test;

/**
 * Tests for {@link TypeToken}.
 *
 * @author Jesse Wilson
 */
// Suppress because these classes are only needed for this test, but must be top-level classes
// to not have an enclosing type
@SuppressWarnings("MultipleTopLevelClasses")
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

    WildcardType wildcardType = (WildcardType) ((ParameterizedType) b).getActualTypeArguments()[0];
    TypeToken<?> wildcardTypeToken = TypeToken.get(wildcardType);
    IllegalArgumentException e =
        assertThrows(IllegalArgumentException.class, () -> wildcardTypeToken.isAssignableFrom(b));
    assertThat(e)
        .hasMessageThat()
        .isEqualTo(
            "Unsupported type, expected one of: java.lang.Class,"
                + " java.lang.reflect.ParameterizedType, java.lang.reflect.GenericArrayType, but"
                + " got: com.google.gson.internal.$Gson$Types$WildcardTypeImpl, for type token: "
                + wildcardTypeToken);
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

    TypeToken<?> expectedIntArray = new TypeToken<int[]>() {};
    assertThat(TypeToken.getArray(int.class)).isEqualTo(expectedIntArray);

    assertThrows(NullPointerException.class, () -> TypeToken.getArray(null));
  }

  static class NestedGeneric<T> {}

  @Test
  public void testParameterizedFactory() {
    TypeToken<?> expectedListOfString = new TypeToken<List<String>>() {};
    assertThat(TypeToken.getParameterized(List.class, String.class))
        .isEqualTo(expectedListOfString);

    TypeToken<?> expectedMapOfStringToString = new TypeToken<Map<String, String>>() {};
    assertThat(TypeToken.getParameterized(Map.class, String.class, String.class))
        .isEqualTo(expectedMapOfStringToString);

    TypeToken<?> expectedListOfListOfListOfString = new TypeToken<List<List<List<String>>>>() {};
    Type listOfString = TypeToken.getParameterized(List.class, String.class).getType();
    Type listOfListOfString = TypeToken.getParameterized(List.class, listOfString).getType();
    assertThat(TypeToken.getParameterized(List.class, listOfListOfString))
        .isEqualTo(expectedListOfListOfListOfString);

    TypeToken<?> expectedWithExactArg = new TypeToken<GenericWithBound<Number>>() {};
    assertThat(TypeToken.getParameterized(GenericWithBound.class, Number.class))
        .isEqualTo(expectedWithExactArg);

    TypeToken<?> expectedWithSubclassArg = new TypeToken<GenericWithBound<Integer>>() {};
    assertThat(TypeToken.getParameterized(GenericWithBound.class, Integer.class))
        .isEqualTo(expectedWithSubclassArg);

    TypeToken<?> expectedSatisfyingTwoBounds =
        new TypeToken<GenericWithMultiBound<ClassSatisfyingBounds>>() {};
    assertThat(TypeToken.getParameterized(GenericWithMultiBound.class, ClassSatisfyingBounds.class))
        .isEqualTo(expectedSatisfyingTwoBounds);

    TypeToken<?> nestedTypeToken = TypeToken.getParameterized(NestedGeneric.class, Integer.class);
    ParameterizedType nestedParameterizedType = (ParameterizedType) nestedTypeToken.getType();
    // TODO: This seems to differ from how Java reflection behaves; when using
    // TypeToken<NestedGeneric<Integer>>, then NestedGeneric<Integer> does have an owner type
    assertThat(nestedParameterizedType.getOwnerType()).isNull();
    assertThat(nestedParameterizedType.getRawType()).isEqualTo(NestedGeneric.class);
    assertThat(nestedParameterizedType.getActualTypeArguments())
        .asList()
        .containsExactly(Integer.class);

    class LocalGenericClass<T> {}
    TypeToken<?> expectedLocalType = new TypeToken<LocalGenericClass<Integer>>() {};
    assertThat(TypeToken.getParameterized(LocalGenericClass.class, Integer.class))
        .isEqualTo(expectedLocalType);

    // For legacy reasons, if requesting parameterized type for non-generic class, create a
    // `TypeToken(Class)`
    assertThat(TypeToken.getParameterized(String.class)).isEqualTo(TypeToken.get(String.class));
  }

  @Test
  public void testParameterizedFactory_Invalid() {
    assertThrows(NullPointerException.class, () -> TypeToken.getParameterized(null, new Type[0]));
    assertThrows(
        NullPointerException.class,
        () -> TypeToken.getParameterized(List.class, new Type[] {null}));

    GenericArrayType arrayType = (GenericArrayType) TypeToken.getArray(String.class).getType();
    IllegalArgumentException e =
        assertThrows(
            IllegalArgumentException.class,
            () -> TypeToken.getParameterized(arrayType, new Type[0]));
    assertThat(e)
        .hasMessageThat()
        .isEqualTo("rawType must be of type Class, but was java.lang.String[]");

    e =
        assertThrows(
            IllegalArgumentException.class,
            () -> TypeToken.getParameterized(String.class, Number.class));
    assertThat(e)
        .hasMessageThat()
        .isEqualTo("java.lang.String requires 0 type arguments, but got 1");

    e =
        assertThrows(
            IllegalArgumentException.class,
            () -> TypeToken.getParameterized(List.class, new Type[0]));
    assertThat(e).hasMessageThat().isEqualTo("java.util.List requires 1 type arguments, but got 0");

    e =
        assertThrows(
            IllegalArgumentException.class,
            () -> TypeToken.getParameterized(List.class, String.class, String.class));
    assertThat(e).hasMessageThat().isEqualTo("java.util.List requires 1 type arguments, but got 2");

    // Primitive types must not be used as type argument
    e =
        assertThrows(
            IllegalArgumentException.class,
            () -> TypeToken.getParameterized(List.class, int.class));
    assertThat(e)
        .hasMessageThat()
        .isEqualTo(
            "Type argument int does not satisfy bounds for type variable E declared by "
                + List.class);

    e =
        assertThrows(
            IllegalArgumentException.class,
            () -> TypeToken.getParameterized(GenericWithBound.class, String.class));
    assertThat(e)
        .hasMessageThat()
        .isEqualTo(
            "Type argument class java.lang.String does not satisfy bounds"
                + " for type variable T declared by "
                + GenericWithBound.class);

    e =
        assertThrows(
            IllegalArgumentException.class,
            () -> TypeToken.getParameterized(GenericWithBound.class, Object.class));
    assertThat(e)
        .hasMessageThat()
        .isEqualTo(
            "Type argument class java.lang.Object does not satisfy bounds"
                + " for type variable T declared by "
                + GenericWithBound.class);

    e =
        assertThrows(
            IllegalArgumentException.class,
            () -> TypeToken.getParameterized(GenericWithMultiBound.class, Number.class));
    assertThat(e)
        .hasMessageThat()
        .isEqualTo(
            "Type argument class java.lang.Number does not satisfy bounds"
                + " for type variable T declared by "
                + GenericWithMultiBound.class);

    e =
        assertThrows(
            IllegalArgumentException.class,
            () -> TypeToken.getParameterized(GenericWithMultiBound.class, CharSequence.class));
    assertThat(e)
        .hasMessageThat()
        .isEqualTo(
            "Type argument interface java.lang.CharSequence does not satisfy bounds"
                + " for type variable T declared by "
                + GenericWithMultiBound.class);

    e =
        assertThrows(
            IllegalArgumentException.class,
            () -> TypeToken.getParameterized(GenericWithMultiBound.class, Object.class));
    assertThat(e)
        .hasMessageThat()
        .isEqualTo(
            "Type argument class java.lang.Object does not satisfy bounds"
                + " for type variable T declared by "
                + GenericWithMultiBound.class);

    class Outer {
      @SuppressWarnings("ClassCanBeStatic")
      class NonStaticInner<T> {}
    }

    e =
        assertThrows(
            IllegalArgumentException.class,
            () -> TypeToken.getParameterized(Outer.NonStaticInner.class, Object.class));
    assertThat(e)
        .hasMessageThat()
        .isEqualTo(
            "Raw type "
                + Outer.NonStaticInner.class.getName()
                + " is not supported because it requires specifying an owner type");
  }

  private static class CustomTypeToken extends TypeToken<String> {}

  @Test
  public void testTypeTokenNonAnonymousSubclass() {
    TypeToken<?> typeToken = new CustomTypeToken();
    assertThat(typeToken.getRawType()).isEqualTo(String.class);
    assertThat(typeToken.getType()).isEqualTo(String.class);
  }

  /**
   * User must only create direct subclasses of TypeToken, but not subclasses of subclasses (...) of
   * TypeToken.
   */
  @Test
  public void testTypeTokenSubSubClass() {
    class SubTypeToken<T> extends TypeToken<String> {}
    class SubSubTypeToken1<T> extends SubTypeToken<T> {}
    class SubSubTypeToken2 extends SubTypeToken<Integer> {}

    IllegalStateException e =
        assertThrows(IllegalStateException.class, () -> new SubTypeToken<Integer>() {});
    assertThat(e).hasMessageThat().isEqualTo("Must only create direct subclasses of TypeToken");

    e = assertThrows(IllegalStateException.class, () -> new SubSubTypeToken1<Integer>());
    assertThat(e).hasMessageThat().isEqualTo("Must only create direct subclasses of TypeToken");

    e = assertThrows(IllegalStateException.class, () -> new SubSubTypeToken2());
    assertThat(e).hasMessageThat().isEqualTo("Must only create direct subclasses of TypeToken");
  }

  private static <M> void createTypeTokenTypeVariable() {
    var unused = new TypeToken<M>() {};
  }

  /**
   * TypeToken type argument must not contain a type variable because, due to type erasure, at
   * runtime only the bound of the type variable is available which is likely not what the user
   * wanted.
   *
   * <p>Note that type variables are allowed for the {@code TypeToken} factory methods calling
   * {@code TypeToken(Type)} because for them the return type is {@code TypeToken<?>} which does not
   * give a false sense of type-safety.
   */
  @Test
  public void testTypeTokenTypeVariable() throws Exception {
    // Put the test code inside generic class to be able to access `T`
    class Enclosing<T> {
      @SuppressWarnings("ClassCanBeStatic")
      class Inner {}

      void test() {
        String expectedMessage =
            "TypeToken type argument must not contain a type variable;"
                + " captured type variable T declared by "
                + Enclosing.class
                + "\n"
                + "See https://github.com/google/gson/blob/main/Troubleshooting.md#typetoken-type-variable";
        IllegalArgumentException e =
            assertThrows(IllegalArgumentException.class, () -> new TypeToken<T>() {});
        assertThat(e).hasMessageThat().isEqualTo(expectedMessage);

        e = assertThrows(IllegalArgumentException.class, () -> new TypeToken<List<List<T>>>() {});
        assertThat(e).hasMessageThat().isEqualTo(expectedMessage);

        e =
            assertThrows(
                IllegalArgumentException.class, () -> new TypeToken<List<? extends List<T>>>() {});
        assertThat(e).hasMessageThat().isEqualTo(expectedMessage);

        e =
            assertThrows(
                IllegalArgumentException.class, () -> new TypeToken<List<? super List<T>>>() {});
        assertThat(e).hasMessageThat().isEqualTo(expectedMessage);

        e = assertThrows(IllegalArgumentException.class, () -> new TypeToken<List<T>[]>() {});
        assertThat(e).hasMessageThat().isEqualTo(expectedMessage);

        e =
            assertThrows(
                IllegalArgumentException.class, () -> new TypeToken<Enclosing<T>.Inner>() {});
        assertThat(e).hasMessageThat().isEqualTo(expectedMessage);

        String systemProperty = "gson.allowCapturingTypeVariables";
        try {
          // Any value other than 'true' should be ignored
          System.setProperty(systemProperty, "some-value");

          e = assertThrows(IllegalArgumentException.class, () -> new TypeToken<T>() {});
          assertThat(e).hasMessageThat().isEqualTo(expectedMessage);
        } finally {
          System.clearProperty(systemProperty);
        }

        try {
          System.setProperty(systemProperty, "true");

          TypeToken<?> typeToken = new TypeToken<T>() {};
          assertThat(typeToken.getType()).isEqualTo(Enclosing.class.getTypeParameters()[0]);
        } finally {
          System.clearProperty(systemProperty);
        }
      }

      <M> void testMethodTypeVariable() throws Exception {
        Method testMethod = Enclosing.class.getDeclaredMethod("testMethodTypeVariable");
        IllegalArgumentException e =
            assertThrows(IllegalArgumentException.class, () -> new TypeToken<M>() {});
        assertThat(e)
            .hasMessageThat()
            .isAnyOf(
                "TypeToken type argument must not contain a type variable;"
                    + " captured type variable M declared by "
                    + testMethod
                    + "\n"
                    + "See https://github.com/google/gson/blob/main/Troubleshooting.md#typetoken-type-variable",
                // Note: When running this test in Eclipse IDE or with certain Java versions it
                // seems to capture `null` instead of the type variable, see
                // https://github.com/eclipse-jdt/eclipse.jdt.core/issues/975
                "TypeToken captured `null` as type argument; probably a compiler / runtime bug");
      }
    }

    new Enclosing<>().test();
    new Enclosing<>().testMethodTypeVariable();

    Method testMethod = TypeTokenTest.class.getDeclaredMethod("createTypeTokenTypeVariable");
    IllegalArgumentException e =
        assertThrows(IllegalArgumentException.class, () -> createTypeTokenTypeVariable());
    assertThat(e)
        .hasMessageThat()
        .isEqualTo(
            "TypeToken type argument must not contain a type variable;"
                + " captured type variable M declared by "
                + testMethod
                + "\n"
                + "See https://github.com/google/gson/blob/main/Troubleshooting.md#typetoken-type-variable");

    // Using type variable as argument for factory methods should be allowed; this is not a
    // type-safety problem because the user would have to perform unsafe casts
    TypeVariable<?> typeVar = Enclosing.class.getTypeParameters()[0];
    TypeToken<?> typeToken = TypeToken.get(typeVar);
    assertThat(typeToken.getType()).isEqualTo(typeVar);

    TypeToken<?> parameterizedTypeToken = TypeToken.getParameterized(List.class, typeVar);
    ParameterizedType parameterizedType = (ParameterizedType) parameterizedTypeToken.getType();
    assertThat(parameterizedType.getRawType()).isEqualTo(List.class);
    assertThat(parameterizedType.getActualTypeArguments()).asList().containsExactly(typeVar);
  }

  @SuppressWarnings("rawtypes")
  @Test
  public void testTypeTokenRaw() {
    IllegalStateException e = assertThrows(IllegalStateException.class, () -> new TypeToken() {});
    assertThat(e)
        .hasMessageThat()
        .isEqualTo(
            "TypeToken must be created with a type argument: new TypeToken<...>() {}; When using"
                + " code shrinkers (ProGuard, R8, ...) make sure that generic signatures are"
                + " preserved.\n"
                + "See https://github.com/google/gson/blob/main/Troubleshooting.md#type-token-raw");
  }
}

// Have to declare these classes here as top-level classes because otherwise tests for
// TypeToken.getParameterized fail due to owner type mismatch
class GenericWithBound<T extends Number> {}

class GenericWithMultiBound<T extends Number & CharSequence> {}

@SuppressWarnings("serial")
abstract class ClassSatisfyingBounds extends Number implements CharSequence {}
