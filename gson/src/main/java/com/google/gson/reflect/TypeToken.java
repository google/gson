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

package com.google.gson.reflect;

import com.google.gson.internal.$Gson$Types;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.$Gson$Preconditions;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a generic type {@code T}. Java doesn't yet provide a way to
 * represent generic types, so this class does. Forces clients to create a
 * subclass of this class which enables retrieval the type information even at
 * runtime.
 *
 * <p>For example, to create a type literal for {@code List<String>}, you can
 * create an empty anonymous inner class:
 *
 * <p>
 * {@code TypeToken<List<String>> list = new TypeToken<List<String>>() {};}
 *
 * <p>This syntax cannot be used to create type literals that have wildcard
 * parameters, such as {@code Class<?>} or {@code List<? extends CharSequence>}.
 *
 * @author Bob Lee
 * @author Sven Mawson
 * @author Jesse Wilson
 */
public class TypeToken<T> {
  final Class<? super T> rawType;
  final Type type;
  final int hashCode;

  /**
   * Constructs a new type literal. Derives represented class from type
   * parameter.
   *
   * <p>Clients create an empty anonymous subclass. Doing so embeds the type
   * parameter in the anonymous class's type hierarchy so we can reconstitute it
   * at runtime despite erasure.
   */
  @SuppressWarnings("unchecked")
  protected TypeToken() {
    this.type = getSuperclassTypeParameter(getClass());
    this.rawType = (Class<? super T>) $Gson$Types.getRawType(type);
    this.hashCode = type.hashCode();
  }

  /**
   * Unsafe. Constructs a type literal manually.
   */
  @SuppressWarnings("unchecked")
  TypeToken(Type type) {
    this.type = $Gson$Types.canonicalize($Gson$Preconditions.checkNotNull(type));
    this.rawType = (Class<? super T>) $Gson$Types.getRawType(this.type);
    this.hashCode = this.type.hashCode();
  }

  /**
   * Returns the type from super class's type parameter in {@link $Gson$Types#canonicalize
   * canonical form}.
   */
  static Type getSuperclassTypeParameter(Class<?> subclass) {
    Type superclass = subclass.getGenericSuperclass();
    if (superclass instanceof Class) {
      throw new RuntimeException("Missing type parameter.");
    }
    ParameterizedType parameterized = (ParameterizedType) superclass;
    return $Gson$Types.canonicalize(parameterized.getActualTypeArguments()[0]);
  }

  /**
   * Returns the raw (non-generic) type for this type.
   */
  public final Class<? super T> getRawType() {
    return rawType;
  }

  /**
   * Gets underlying {@code Type} instance.
   */
  public final Type getType() {
    return type;
  }

  /**
   * Gets the generic type arguments this type token has for the type parameters
   * of {@code supertype}. {@code supertype} can either be the
   * {@linkplain #getRawType() raw type} or a supertype of the raw type.
   *
   * <p>For wildcard type arguments the upper bound is returned, unless that is
   * not specified (or {@code Object}) in which case the bound of the corresponding
   * type parameter is returned.
   *
   * <p>For type parameters the first upper bound which is itself not a type
   * parameter is returned. This is applied recursively until a non-type-parameter
   * bound has been found. For type parameter bounds with infinite recursion
   * (e.g. {@code T extends List<T>}) the erasure of the bound is returned.
   *
   * <h2>Example</h2>
   * <p>Let's assume you want to write a custom {@link TypeAdapterFactory} for type
   * {@code Map<K, V>}, then you can use this method to determine the key and value types:
   * <pre>{@code
   * Class<?> rawType = typeToken.getRawType();
   * if (!Map.class.isAssignableFrom(rawType)) {
   *   return null;
   * }
   *
   * Type[] typeArguments = typeToken.getTypeArguments(Map.class);
   * Type keyType = typeArguments[0]; // Argument for parameter K of type Map<K, V>
   * Type valueType = typeArguments[1]; // Argument for parameter V of type Map<K, V>
   * }</pre>
   *
   * This will even work correctly when the {@code TypeToken} this method is
   * called for represents a custom subtype with different type parameters.
   * For example:
   * <pre>{@code
   * class StringKeyMap<V> implements Map<String, V> {
   *   ...
   * }
   *
   * ...
   *
   * TypeToken<StringKeyMap<Integer>> typeToken = ...;
   * // Will be [String.class (K), Integer.class (V)]
   * Type[] typeArguments = typeToken.getTypeArguments(Map.class);
   * }</pre>
   *
   * @param supertype Supertype for which the type arguments should be resolved
   * @return The resolved type arguments for the supertype
   * @throws IllegalArgumentException If {@code supertype} is neither the same as
   *    nor a supertype of {@link #getRawType()}
   * @throws IllegalArgumentException If {@code supertype} does not have any type
   *    parameters
   */
  public final Type[] getTypeArguments(Class<?> supertype) {
    if (!supertype.isAssignableFrom(rawType)) {
      throw new IllegalArgumentException("Type " + supertype.getName() +
        " is neither the same as nor a supertype of type " + rawType.getName());
    }

    TypeVariable<?>[] typeParameters = supertype.getTypeParameters();
    int typeParametersCount = typeParameters.length;
    if (typeParametersCount == 0) {
      throw new IllegalArgumentException("Type " + supertype.getName() + " does not have any type parameters");
    }

    Type parameterizedSupertype = $Gson$Types.getSupertype(type, rawType, supertype);

    // Arguments requested for same raw type, e.g. Map<K, V> for for Map<K, V>
    // In this case use bounds of type variables
    if (parameterizedSupertype == supertype) {
      Type[] typeArguments = new Type[typeParametersCount];
      for (int i = 0; i < typeParametersCount; i++) {
        typeArguments[i] = $Gson$Types.getUltimateTypeVariableBound(typeParameters[i]);
      }
      return typeArguments;
    }

    assert parameterizedSupertype instanceof ParameterizedType :
      "Unsupported type " + parameterizedSupertype + " (" + parameterizedSupertype.getClass() + ")";

    Type[] typeArguments = ((ParameterizedType) parameterizedSupertype).getActualTypeArguments();
    assert typeArguments.length == typeParametersCount;

    boolean[] needsFinalResolve = new boolean[typeParametersCount];

    // Convert wildcards and type variables
    for (int i = 0; i < typeParametersCount; i++) {
      Type typeArgument = typeArguments[i];
      if (typeArgument instanceof WildcardType) {
        // Only consider upper bounds (ignore lower bounds because they don't impose any restriction)
        // Trust the upper bound (even though it could be 'illogical', see https://bugs.openjdk.java.net/browse/JDK-8250936)
        Type bound = ((WildcardType) typeArgument).getUpperBounds()[0];
        if (bound instanceof TypeVariable) {
          typeArguments[i] = $Gson$Types.getUltimateTypeVariableBound((TypeVariable<?>) bound);
        } else if (bound == Object.class) {
          // When no upper bound is specified use the bounds of the underlying type variable
          // Look up the type at the end since type variables can have forwards bounds
          // for which the type argument is not available yet, e.g. `Generic<T extends U, U>`
          // with `Generic<?, String>`
          needsFinalResolve[i] = true;
        } else {
          typeArguments[i] = bound;
        }
      } else if (typeArgument instanceof TypeVariable) {
        typeArguments[i] = $Gson$Types.getUltimateTypeVariableBound((TypeVariable<?>) typeArgument);
      }
    }

    // Resolve wildcards whose bounds could not be resolved yet
    for (int i = 0; i < typeParametersCount; i++) {
      if (needsFinalResolve[i]) {
        TypeVariable<?> typeParameter = typeParameters[i];
        TypeVariable<?> ultimateTypeParameter = $Gson$Types.getUltimateTypeVariable(typeParameter);

        // Type parameter has non-type-parameter as bound, use it as argument
        if (typeParameter == ultimateTypeParameter) {
          typeArguments[i] = $Gson$Types.getUltimateTypeVariableBound(typeParameter);
          continue;
        }

        // Look up the type argument for ultimateTypeParameter
        boolean foundArgument = false;
        for (int typeParamIndex = 0; typeParamIndex < typeParametersCount; typeParamIndex++) {
          if (typeParameters[typeParamIndex].equals(ultimateTypeParameter)) {
            foundArgument = true;
            Type typeArgument = typeArguments[typeParamIndex];
            typeArguments[i] = typeArgument;
            assert !(typeArgument instanceof TypeVariable || typeArgument instanceof WildcardType);
            break;
          }
        }

        // ultimateTypeParameter is not one of the type parameters of this type
        // (but maybe of enclosing type); fall back to using its bound
        if (!foundArgument) {
          typeArguments[i] = $Gson$Types.getUltimateTypeVariableBound(ultimateTypeParameter);
        }
      }
    }

    return typeArguments;
  }

  /**
   * Check if this type is assignable from the given class object.
   *
   * @deprecated this implementation may be inconsistent with javac for types
   *     with wildcards.
   */
  @Deprecated
  public boolean isAssignableFrom(Class<?> cls) {
    return isAssignableFrom((Type) cls);
  }

  /**
   * Check if this type is assignable from the given Type.
   *
   * @deprecated this implementation may be inconsistent with javac for types
   *     with wildcards.
   */
  @Deprecated
  public boolean isAssignableFrom(Type from) {
    if (from == null) {
      return false;
    }

    if (type.equals(from)) {
      return true;
    }

    if (type instanceof Class<?>) {
      return rawType.isAssignableFrom($Gson$Types.getRawType(from));
    } else if (type instanceof ParameterizedType) {
      return isAssignableFrom(from, (ParameterizedType) type,
          new HashMap<String, Type>());
    } else if (type instanceof GenericArrayType) {
      return rawType.isAssignableFrom($Gson$Types.getRawType(from))
          && isAssignableFrom(from, (GenericArrayType) type);
    } else {
      throw buildUnexpectedTypeError(
          type, Class.class, ParameterizedType.class, GenericArrayType.class);
    }
  }

  /**
   * Check if this type is assignable from the given type token.
   *
   * @deprecated this implementation may be inconsistent with javac for types
   *     with wildcards.
   */
  @Deprecated
  public boolean isAssignableFrom(TypeToken<?> token) {
    return isAssignableFrom(token.getType());
  }

  /**
   * Private helper function that performs some assignability checks for
   * the provided GenericArrayType.
   */
  private static boolean isAssignableFrom(Type from, GenericArrayType to) {
    Type toGenericComponentType = to.getGenericComponentType();
    if (toGenericComponentType instanceof ParameterizedType) {
      Type t = from;
      if (from instanceof GenericArrayType) {
        t = ((GenericArrayType) from).getGenericComponentType();
      } else if (from instanceof Class<?>) {
        Class<?> classType = (Class<?>) from;
        while (classType.isArray()) {
          classType = classType.getComponentType();
        }
        t = classType;
      }
      return isAssignableFrom(t, (ParameterizedType) toGenericComponentType,
          new HashMap<String, Type>());
    }
    // No generic defined on "to"; therefore, return true and let other
    // checks determine assignability
    return true;
  }

  /**
   * Private recursive helper function to actually do the type-safe checking
   * of assignability.
   */
  private static boolean isAssignableFrom(Type from, ParameterizedType to,
      Map<String, Type> typeVarMap) {

    if (from == null) {
      return false;
    }

    if (to.equals(from)) {
      return true;
    }

    // First figure out the class and any type information.
    Class<?> clazz = $Gson$Types.getRawType(from);
    ParameterizedType ptype = null;
    if (from instanceof ParameterizedType) {
      ptype = (ParameterizedType) from;
    }

    // Load up parameterized variable info if it was parameterized.
    if (ptype != null) {
      Type[] tArgs = ptype.getActualTypeArguments();
      TypeVariable<?>[] tParams = clazz.getTypeParameters();
      for (int i = 0; i < tArgs.length; i++) {
        Type arg = tArgs[i];
        TypeVariable<?> var = tParams[i];
        while (arg instanceof TypeVariable<?>) {
          TypeVariable<?> v = (TypeVariable<?>) arg;
          arg = typeVarMap.get(v.getName());
        }
        typeVarMap.put(var.getName(), arg);
      }

      // check if they are equivalent under our current mapping.
      if (typeEquals(ptype, to, typeVarMap)) {
        return true;
      }
    }

    for (Type itype : clazz.getGenericInterfaces()) {
      if (isAssignableFrom(itype, to, new HashMap<String, Type>(typeVarMap))) {
        return true;
      }
    }

    // Interfaces didn't work, try the superclass.
    Type sType = clazz.getGenericSuperclass();
    return isAssignableFrom(sType, to, new HashMap<String, Type>(typeVarMap));
  }

  /**
   * Checks if two parameterized types are exactly equal, under the variable
   * replacement described in the typeVarMap.
   */
  private static boolean typeEquals(ParameterizedType from,
      ParameterizedType to, Map<String, Type> typeVarMap) {
    if (from.getRawType().equals(to.getRawType())) {
      Type[] fromArgs = from.getActualTypeArguments();
      Type[] toArgs = to.getActualTypeArguments();
      for (int i = 0; i < fromArgs.length; i++) {
        if (!matches(fromArgs[i], toArgs[i], typeVarMap)) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  private static AssertionError buildUnexpectedTypeError(
      Type token, Class<?>... expected) {

    // Build exception message
    StringBuilder exceptionMessage =
        new StringBuilder("Unexpected type. Expected one of: ");
    for (Class<?> clazz : expected) {
      exceptionMessage.append(clazz.getName()).append(", ");
    }
    exceptionMessage.append("but got: ").append(token.getClass().getName())
        .append(", for type token: ").append(token.toString()).append('.');

    return new AssertionError(exceptionMessage.toString());
  }

  /**
   * Checks if two types are the same or are equivalent under a variable mapping
   * given in the type map that was provided.
   */
  private static boolean matches(Type from, Type to, Map<String, Type> typeMap) {
    return to.equals(from)
        || (from instanceof TypeVariable
        && to.equals(typeMap.get(((TypeVariable<?>) from).getName())));

  }

  @Override public final int hashCode() {
    return this.hashCode;
  }

  @Override public final boolean equals(Object o) {
    return o instanceof TypeToken<?>
        && $Gson$Types.equals(type, ((TypeToken<?>) o).type);
  }

  @Override public final String toString() {
    return $Gson$Types.typeToString(type);
  }

  /**
   * Gets type literal for the given {@code Type} instance.
   */
  public static TypeToken<?> get(Type type) {
    return new TypeToken<Object>(type);
  }

  /**
   * Gets type literal for the given {@code Class} instance.
   */
  public static <T> TypeToken<T> get(Class<T> type) {
    return new TypeToken<T>(type);
  }

  /**
   * Gets type literal for the parameterized type represented by applying {@code typeArguments} to
   * {@code rawType}.
   */
  public static TypeToken<?> getParameterized(Type rawType, Type... typeArguments) {
    return new TypeToken<Object>($Gson$Types.newParameterizedTypeWithOwner(null, rawType, typeArguments));
  }

  /**
   * Gets type literal for the array type whose elements are all instances of {@code componentType}.
   */
  public static TypeToken<?> getArray(Type componentType) {
    return new TypeToken<Object>($Gson$Types.arrayOf(componentType));
  }
}
