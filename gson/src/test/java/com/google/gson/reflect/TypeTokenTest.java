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

import static org.junit.Assert.assertArrayEquals;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;

import com.google.gson.internal.$Gson$Types;

import junit.framework.TestCase;

/**
 * @author Jesse Wilson
 */
@SuppressWarnings({"deprecation"})
public final class TypeTokenTest extends TestCase {

  List<Integer> listOfInteger = null;
  List<Number> listOfNumber = null;
  List<String> listOfString = null;
  List<?> listOfUnknown = null;
  List<Set<String>> listOfSetOfString = null;
  List<Set<?>> listOfSetOfUnknown = null;

  public void testIsAssignableFromRawTypes() {
    assertTrue(TypeToken.get(Object.class).isAssignableFrom(String.class));
    assertFalse(TypeToken.get(String.class).isAssignableFrom(Object.class));
    assertTrue(TypeToken.get(RandomAccess.class).isAssignableFrom(ArrayList.class));
    assertFalse(TypeToken.get(ArrayList.class).isAssignableFrom(RandomAccess.class));
  }

  public void testIsAssignableFromWithTypeParameters() throws Exception {
    Type a = getClass().getDeclaredField("listOfInteger").getGenericType();
    Type b = getClass().getDeclaredField("listOfNumber").getGenericType();
    assertTrue(TypeToken.get(a).isAssignableFrom(a));
    assertTrue(TypeToken.get(b).isAssignableFrom(b));

    // listOfInteger = listOfNumber; // doesn't compile; must be false
    assertFalse(TypeToken.get(a).isAssignableFrom(b));
    // listOfNumber = listOfInteger; // doesn't compile; must be false
    assertFalse(TypeToken.get(b).isAssignableFrom(a));
  }

  public void testIsAssignableFromWithBasicWildcards() throws Exception {
    Type a = getClass().getDeclaredField("listOfString").getGenericType();
    Type b = getClass().getDeclaredField("listOfUnknown").getGenericType();
    assertTrue(TypeToken.get(a).isAssignableFrom(a));
    assertTrue(TypeToken.get(b).isAssignableFrom(b));

    // listOfString = listOfUnknown  // doesn't compile; must be false
    assertFalse(TypeToken.get(a).isAssignableFrom(b));
    listOfUnknown = listOfString; // compiles; must be true
    // The following assertion is too difficult to support reliably, so disabling
    // assertTrue(TypeToken.get(b).isAssignableFrom(a));
  }

  public void testIsAssignableFromWithNestedWildcards() throws Exception {
    Type a = getClass().getDeclaredField("listOfSetOfString").getGenericType();
    Type b = getClass().getDeclaredField("listOfSetOfUnknown").getGenericType();
    assertTrue(TypeToken.get(a).isAssignableFrom(a));
    assertTrue(TypeToken.get(b).isAssignableFrom(b));

    // listOfSetOfString = listOfSetOfUnknown; // doesn't compile; must be false
    assertFalse(TypeToken.get(a).isAssignableFrom(b));
    // listOfSetOfUnknown = listOfSetOfString; // doesn't compile; must be false
    assertFalse(TypeToken.get(b).isAssignableFrom(a));
  }

  public void testArrayFactory() {
    TypeToken<?> expectedStringArray = new TypeToken<String[]>() {};
    assertEquals(expectedStringArray, TypeToken.getArray(String.class));

    TypeToken<?> expectedListOfStringArray = new TypeToken<List<String>[]>() {};
    Type listOfString = new TypeToken<List<String>>() {}.getType();
    assertEquals(expectedListOfStringArray, TypeToken.getArray(listOfString));
  }

  public void testParameterizedFactory() {
    TypeToken<?> expectedListOfString = new TypeToken<List<String>>() {};
    assertEquals(expectedListOfString, TypeToken.getParameterized(List.class, String.class));

    TypeToken<?> expectedMapOfStringToString = new TypeToken<Map<String, String>>() {};
    assertEquals(expectedMapOfStringToString, TypeToken.getParameterized(Map.class, String.class, String.class));

    TypeToken<?> expectedListOfListOfListOfString = new TypeToken<List<List<List<String>>>>() {};
    Type listOfString = TypeToken.getParameterized(List.class, String.class).getType();
    Type listOfListOfString = TypeToken.getParameterized(List.class, listOfString).getType();
    assertEquals(expectedListOfListOfListOfString, TypeToken.getParameterized(List.class, listOfListOfString));
  }

  interface CustomMap<K, V> extends Map<K, V> {}
  interface CustomMapNoParams extends Map<Double, Float> {}
  interface CustomMapOneParam<K> extends Map<K, Short> {}
  public void testGetTypeArguments() {
    {
      Type[] typeArguments = new TypeToken<Map<String, Integer>>() {}.getTypeArguments(Map.class);
      assertArrayEquals(new Type[] {String.class, Integer.class}, typeArguments);
    }

    {
      Type[] typeArguments = new TypeToken<CustomMap<String, Long>>() {}.getTypeArguments(Map.class);
      assertArrayEquals(new Type[] {String.class, Long.class}, typeArguments);
    }

    {
      Type[] typeArguments = TypeToken.get(CustomMapNoParams.class).getTypeArguments(Map.class);
      assertArrayEquals(new Type[] {Double.class, Float.class}, typeArguments);
    }

    {
      Type[] typeArguments = new TypeToken<CustomMapOneParam<Byte>>() {}.getTypeArguments(Map.class);
      assertArrayEquals(new Type[] {Byte.class, Short.class}, typeArguments);
    }
  }

  // Implements Map<V, K> (parameters switched)
  interface SwitchedParametersMap<K, V> extends Map<V, K> {}
  public void testGetTypeArguments_SwitchedParameters() {
    Type[] typeArguments = new TypeToken<SwitchedParametersMap<String, Long>>() {}.getTypeArguments(Map.class);
    assertArrayEquals(new Type[] {Long.class, String.class}, typeArguments);
  }

  public void testGetTypeArguments_NotSupertype() {
    try {
      new TypeToken<List<Integer>>() {}.getTypeArguments(Map.class);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals(
        "Type java.util.Map is neither the same as nor a supertype of type java.util.List",
        e.getMessage()
      );
    }
  }

  public void testGetTypeArguments_NoTypeParameters() {
    try {
      new TypeToken<List<Integer>>() {}.getTypeArguments(Object.class);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Type java.lang.Object does not have any type parameters", e.getMessage());
    }
  }

  /** Tests behavior when TypeToken represents raw type */
  public void testGetTypeArguments_Raw() {
    {
      Type[] typeArguments = TypeToken.get(Map.class).getTypeArguments(Map.class);
      assertArrayEquals(new Type[] {Object.class, Object.class}, typeArguments);
    }

    {
      Type[] typeArguments = TypeToken.get(CustomMap.class).getTypeArguments(Map.class);
      assertArrayEquals(new Type[] {Object.class, Object.class}, typeArguments);
    }

    {
      Type[] typeArguments = TypeToken.get(CustomMapOneParam.class).getTypeArguments(Map.class);
      assertArrayEquals(new Type[] {Object.class, Short.class}, typeArguments);
    }

    {
      // Generic with bound
      abstract class WithBounds<T extends Number, U extends T> {}
      Type[] typeArguments = TypeToken.get(WithBounds.class).getTypeArguments(WithBounds.class);
      assertArrayEquals(new Type[] {Number.class, Number.class}, typeArguments);
    }

    {
      // Generic with bound
      abstract class CustomMapWithBound<K extends Number> implements Map<K, String> {}
      Type[] typeArguments = TypeToken.get(CustomMapWithBound.class).getTypeArguments(Map.class);
      assertArrayEquals(new Type[] {Number.class, String.class}, typeArguments);
    }

    /*
     * Note: Don't need to cover the case where type variable is transitively bound by
     * type argument of other variable, e.g.:
     *
     * class Base<T extends CharSequence, U extends T> {}
     * class Sub<U> extends Base<String, U> {}
     *
     * Here `U` of Sub would actually be `U extends String` due to String being provided
     * as type argument for `T`. However the code above won't compile; the compiler
     * complains that `U` of Sub is not within bounds, it requires an explicit bound.
     * Therefore no need to check for this.
     */
  }

  static class Enclosing<T extends Number> {
    class Inner {
      Map<T, String> f;
      List<? extends T> f2;
    }
  }
  /**
   * In general capturing type variables with TypeToken (e.g. {@code TypeToken<T>}) is
   * unsupported because it does not behave the way the user might expect it; the actual
   * type argument is not available to Gson at runtime.
   *
   * However, there are a few legitimate cases where Gson ends up creating a TypeToken
   * containing a type variable; these are covered below.
   */
  public void testGetTypeArguments_TypeVariable() throws Exception {
    {
      class RawWithParameterized<K extends CharSequence, T extends Map<K, String>> {}
      Type[] typeArguments = TypeToken.get(RawWithParameterized.class).getTypeArguments(RawWithParameterized.class);
      assertEquals(2, typeArguments.length);
      assertEquals(CharSequence.class, typeArguments[0]);
      // And now get arguments of Map
      typeArguments = TypeToken.get(typeArguments[1]).getTypeArguments(Map.class);
      assertArrayEquals(new Type[] {CharSequence.class, String.class}, typeArguments);
    }

    {
      class RawWithWildcard<K extends CharSequence, T extends Map<? extends K, ? super K>> {}
      Type[] typeArguments = TypeToken.get(RawWithWildcard.class).getTypeArguments(RawWithWildcard.class);
      assertEquals(2, typeArguments.length);
      assertEquals(CharSequence.class, typeArguments[0]);
      // And now get arguments of Map
      typeArguments = TypeToken.get(typeArguments[1]).getTypeArguments(Map.class);
      assertArrayEquals(new Type[] {CharSequence.class, Object.class}, typeArguments);
    }

    {
      class RawWithArray<K extends CharSequence, T extends List<K[]>> {}
      Type[] typeArguments = TypeToken.get(RawWithArray.class).getTypeArguments(RawWithArray.class);
      assertEquals(2, typeArguments.length);
      assertEquals(CharSequence.class, typeArguments[0]);
      // And now get argument of List
      typeArguments = TypeToken.get(typeArguments[1]).getTypeArguments(List.class);
      assertEquals(1, typeArguments.length);
      assertTrue(typeArguments[0] instanceof GenericArrayType);
      TypeToken<?> arrayComponentType = TypeToken.get(((GenericArrayType) typeArguments[0]).getGenericComponentType());
      assertEquals(CharSequence.class, arrayComponentType.getRawType());
    }

    {
      abstract class NestedList<T extends List<Number>> implements List<T> {}
      // Current implementation returns TypeVariable
      Type elementType = $Gson$Types.getCollectionElementType(NestedList.class, NestedList.class);
      Type[] typeArguments = TypeToken.get(elementType).getTypeArguments(List.class);
      assertArrayEquals(new Type[] {Number.class}, typeArguments);
    }

    {
      // Without Enclosing context field `f` has type `Map<T, String>`
      Type typeWithTypeVariable = Enclosing.Inner.class.getDeclaredField("f").getGenericType();
      Type[] typeArguments = TypeToken.get(typeWithTypeVariable).getTypeArguments(Map.class);
      assertArrayEquals(new Type[] {Number.class, String.class}, typeArguments);
    }

    {
      // Without Enclosing context field `f2` has type `List<? extends T>`
      Type typeWithTypeVariable = Enclosing.Inner.class.getDeclaredField("f2").getGenericType();
      Type[] typeArguments = TypeToken.get(typeWithTypeVariable).getTypeArguments(List.class);
      assertArrayEquals(new Type[] {Number.class}, typeArguments);
    }
  }

  interface Recursive<T extends Map<T, String>> {}
  interface Recursive2<T extends List<T[]>> {}
  interface RecursiveCyclic<T extends List<U>, U extends List<T>> {}
  /**
   * Recursive type variable references must be resolved, otherwise a TypeAdapterFactory
   * looking up arguments of {@code Recursive<T extends List<T>>} and getting the
   * corresponding adapter would indefinitely try looking up {@code T}.
   */
  public void testGetTypeArguments_RecursiveTypeVariable() {
    {
      Type[] typeArguments = new TypeToken<Recursive<?>>() {}.getTypeArguments(Recursive.class);
      // Must have been erased to Map to avoid infinite recursion
      assertArrayEquals(new Type[] {Map.class}, typeArguments);
    }

    {
      Type[] typeArguments = TypeToken.get(Recursive.class).getTypeArguments(Recursive.class);
      // Must have been erased to Map to avoid infinite recursion
      assertArrayEquals(new Type[] {Map.class}, typeArguments);
    }

    {
      Type[] typeArguments = new TypeToken<Recursive2<?>>() {}.getTypeArguments(Recursive2.class);
      // Must have been erased to List to avoid infinite recursion
      assertArrayEquals(new Type[] {List.class}, typeArguments);
    }

    {
      Type[] typeArguments = TypeToken.get(Recursive2.class).getTypeArguments(Recursive2.class);
      // Must have been erased to List to avoid infinite recursion
      assertArrayEquals(new Type[] {List.class}, typeArguments);
    }

    {
      Type[] typeArguments = new TypeToken<RecursiveCyclic<?, ?>>() {}.getTypeArguments(RecursiveCyclic.class);
      assertArrayEquals(new Type[] {List.class, List.class}, typeArguments);
    }

    {
      Type[] typeArguments = TypeToken.get(RecursiveCyclic.class).getTypeArguments(RecursiveCyclic.class);
      assertArrayEquals(new Type[] {List.class, List.class}, typeArguments);
    }
  }

  interface GenericWithBounds<T extends CharSequence, U extends Number> {}
  interface GenericWithTypeVariableBound<T extends CharSequence, U extends T> {}
  interface Base {}
  interface Sub extends Base {}
  interface GenericIllogicalBounds<T extends Sub> {}
  static class EnclosingWildcard<T extends Number> {
    class Inner<I1, I2 extends T> {
      // Wildcard is bound using type variable T of enclosing type
      Inner<String, ?> f;
      List<? extends T> f2;
    }
  }
  static class WildcardTransitiveBound<T extends List<String>> {
    class Inner {
      List<? extends T> f;
    }
  }
  static List<? extends Map<Number, CharSequence>> wildcards;
  public void testGetTypeArguments_Wildcard() throws Exception {
    {
      Type[] typeArguments = new TypeToken<List<?>>() {}.getTypeArguments(List.class);
      assertArrayEquals(new Type[] {Object.class}, typeArguments);
    }

    {
      Type[] typeArguments = new TypeToken<Map<? extends Number, ? super CharSequence>>() {}.getTypeArguments(Map.class);
      // Lower bounds should be ignored so `? super CharSequence` becomes Object
      assertArrayEquals(new Type[] {Number.class, Object.class}, typeArguments);
    }

    {
      // Wildcard uses illogical bound, see https://bugs.openjdk.java.net/browse/JDK-8250936
      // `T extends Sub` but wildcard is `? extends Base`, even though type argument has to be at least `Sub`
      // Trust the type bounds the user chooses; if they use illogical bound then use that bound anyways
      Type[] typeArguments = new TypeToken<GenericIllogicalBounds<? extends Base>>() {}.getTypeArguments(GenericIllogicalBounds.class);
      assertArrayEquals(new Type[] {Base.class}, typeArguments);
    }

    {
      Type[] typeArguments = new TypeToken<GenericWithBounds<?, ? super Integer>>() {}.getTypeArguments(GenericWithBounds.class);
      // Should use bounds of type variables when wildcards define no upper bounds
      assertArrayEquals(new Type[] {CharSequence.class, Number.class}, typeArguments);
    }

    {
      // String as type argument for `T`, and ? for `U`
      Type[] typeArguments = new TypeToken<GenericWithTypeVariableBound<String, ?>>() {}.getTypeArguments(GenericWithTypeVariableBound.class);
      // Because `U extends T` the argument for `U` should be therefore String (from `T`)
      assertArrayEquals(new Type[] {String.class, String.class}, typeArguments);
    }

    {
      Type typeWithWildcard = EnclosingWildcard.Inner.class.getDeclaredField("f").getGenericType();
      Type[] typeArguments = TypeToken.get(typeWithWildcard).getTypeArguments(EnclosingWildcard.Inner.class);
      assertArrayEquals(new Type[] {String.class, Number.class}, typeArguments);
    }

    {
      Type typeWithWildcard = EnclosingWildcard.Inner.class.getDeclaredField("f2").getGenericType();
      Type[] typeArguments = TypeToken.get(typeWithWildcard).getTypeArguments(List.class);
      assertArrayEquals(new Type[] {Number.class}, typeArguments);
    }

    // Support TypeToken containing wildcard for cases where Gson resolved a type
    // variable to a wildcard (e.g. for collection classes), then created a TypeToken
    // containing the wildcard and passed it to type adapters
    {
      // Get the wildcard, that is `? extends Map...`
      Type wildcardType = ((ParameterizedType) TypeTokenTest.class.getDeclaredField("wildcards").getGenericType()).getActualTypeArguments()[0];
      Type[] typeArguments = TypeToken.get(wildcardType).getTypeArguments(Map.class);
      assertArrayEquals(new Type[] {Number.class, CharSequence.class}, typeArguments);
    }

    {
      // Get the wildcard, that is `? extends T`
      Type wildcardType = ((ParameterizedType) WildcardTransitiveBound.Inner.class.getDeclaredField("f").getGenericType()).getActualTypeArguments()[0];
      Type[] typeArguments = TypeToken.get(wildcardType).getTypeArguments(List.class);
      assertArrayEquals(new Type[] {String.class}, typeArguments);
    }

    {
      Type type = new TypeToken<List<? extends List<String>>>() {}.getType();
      // Current implementation returns Wildcard
      Type elementType = $Gson$Types.getCollectionElementType(type, List.class);
      Type[] typeArguments = TypeToken.get(elementType).getTypeArguments(List.class);
      assertArrayEquals(new Type[] {String.class}, typeArguments);
    }
  }
}
