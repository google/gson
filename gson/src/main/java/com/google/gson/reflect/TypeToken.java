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

import com.google.gson.internal.GsonTypes;
import com.google.gson.internal.TroubleshootingGuide;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a generic type {@code T}. Java doesn't yet provide a way to represent generic types,
 * so this class does. Forces clients to create a subclass of this class which enables retrieval the
 * type information even at runtime.
 *
 * <p>For example, to create a type literal for {@code List<String>}, you can create an empty
 * anonymous class:
 *
 * <p>{@code TypeToken<List<String>> list = new TypeToken<List<String>>() {};}
 *
 * <p>Capturing a type variable as type argument of an anonymous {@code TypeToken} subclass is not
 * allowed, for example {@code TypeToken<List<T>>}. Due to type erasure the runtime type of a type
 * variable is not available to Gson and therefore it cannot provide the functionality one might
 * expect. This would give a false sense of type-safety at compile time and could lead to an
 * unexpected {@code ClassCastException} at runtime.
 *
 * <p>If the type arguments of the parameterized type are only available at runtime, for example
 * when you want to create a {@code List<E>} based on a {@code Class<E>} representing the element
 * type, the method {@link #getParameterized(Type, Type...)} can be used.
 *
 * @author Bob Lee
 * @author Sven Mawson
 * @author Jesse Wilson
 */
public class TypeToken<T> {
  private final Class<? super T> rawType;
  private final Type type;
  private final int hashCode;

  /**
   * Constructs a new type literal. Derives represented class from type parameter.
   *
   * <p>Clients create an empty anonymous subclass. Doing so embeds the type parameter in the
   * anonymous class's type hierarchy so we can reconstitute it at runtime despite erasure, for
   * example:
   *
   * <p>{@code new TypeToken<List<String>>() {}}
   *
   * @throws IllegalArgumentException If the anonymous {@code TypeToken} subclass captures a type
   *     variable, for example {@code TypeToken<List<T>>}. See the {@code TypeToken} class
   *     documentation for more details.
   */
  @SuppressWarnings("unchecked")
  protected TypeToken() {
    this.type = getTypeTokenTypeArgument();
    this.rawType = (Class<? super T>) GsonTypes.getRawType(type);
    this.hashCode = type.hashCode();
  }

  /** Unsafe. Constructs a type literal manually. */
  @SuppressWarnings("unchecked")
  private TypeToken(Type type) {
    this.type = GsonTypes.canonicalize(Objects.requireNonNull(type));
    this.rawType = (Class<? super T>) GsonTypes.getRawType(this.type);
    this.hashCode = this.type.hashCode();
  }

  private static boolean isCapturingTypeVariablesForbidden() {
    return !Objects.equals(System.getProperty("gson.allowCapturingTypeVariables"), "true");
  }

  /**
   * Verifies that {@code this} is an instance of a direct subclass of TypeToken and returns the
   * type argument for {@code T} in {@link GsonTypes#canonicalize canonical form}.
   */
  private Type getTypeTokenTypeArgument() {
    Type superclass = getClass().getGenericSuperclass();
    if (superclass instanceof ParameterizedType) {
      ParameterizedType parameterized = (ParameterizedType) superclass;
      if (parameterized.getRawType() == TypeToken.class) {
        Type typeArgument = GsonTypes.canonicalize(parameterized.getActualTypeArguments()[0]);

        if (isCapturingTypeVariablesForbidden()) {
          verifyNoTypeVariable(typeArgument);
        }
        return typeArgument;
      }
    }
    // Check for raw TypeToken as superclass
    else if (superclass == TypeToken.class) {
      throw new IllegalStateException(
          "TypeToken must be created with a type argument: new TypeToken<...>() {}; When using code"
              + " shrinkers (ProGuard, R8, ...) make sure that generic signatures are preserved."
              + "\nSee "
              + TroubleshootingGuide.createUrl("type-token-raw"));
    }

    // User created subclass of subclass of TypeToken
    throw new IllegalStateException("Must only create direct subclasses of TypeToken");
  }

  private static void verifyNoTypeVariable(Type type) {
    if (type instanceof TypeVariable) {
      TypeVariable<?> typeVariable = (TypeVariable<?>) type;
      throw new IllegalArgumentException(
          "TypeToken type argument must not contain a type variable; captured type variable "
              + typeVariable.getName()
              + " declared by "
              + typeVariable.getGenericDeclaration()
              + "\nSee "
              + TroubleshootingGuide.createUrl("typetoken-type-variable"));
    } else if (type instanceof GenericArrayType) {
      verifyNoTypeVariable(((GenericArrayType) type).getGenericComponentType());
    } else if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;
      Type ownerType = parameterizedType.getOwnerType();
      if (ownerType != null) {
        verifyNoTypeVariable(ownerType);
      }

      for (Type typeArgument : parameterizedType.getActualTypeArguments()) {
        verifyNoTypeVariable(typeArgument);
      }
    } else if (type instanceof WildcardType) {
      WildcardType wildcardType = (WildcardType) type;
      for (Type bound : wildcardType.getLowerBounds()) {
        verifyNoTypeVariable(bound);
      }
      for (Type bound : wildcardType.getUpperBounds()) {
        verifyNoTypeVariable(bound);
      }
    } else if (type == null) {
      // Occurs in Eclipse IDE and certain Java versions (e.g. Java 11.0.18) when capturing type
      // variable declared by method of local class, see
      // https://github.com/eclipse-jdt/eclipse.jdt.core/issues/975
      throw new IllegalArgumentException(
          "TypeToken captured `null` as type argument; probably a compiler / runtime bug");
    }
  }

  /** Returns the raw (non-generic) type for this type. */
  public final Class<? super T> getRawType() {
    return rawType;
  }

  /** Gets underlying {@code Type} instance. */
  public final Type getType() {
    return type;
  }

  /**
   * Check if this type is assignable from the given class object.
   *
   * @deprecated this implementation may be inconsistent with javac for types with wildcards.
   */
  @Deprecated
  public boolean isAssignableFrom(Class<?> cls) {
    return isAssignableFrom((Type) cls);
  }

  /**
   * Check if this type is assignable from the given Type.
   *
   * @deprecated this implementation may be inconsistent with javac for types with wildcards.
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
      return rawType.isAssignableFrom(GsonTypes.getRawType(from));
    } else if (type instanceof ParameterizedType) {
      return isAssignableFrom(from, (ParameterizedType) type, new HashMap<String, Type>());
    } else if (type instanceof GenericArrayType) {
      return rawType.isAssignableFrom(GsonTypes.getRawType(from))
          && isAssignableFrom(from, (GenericArrayType) type);
    } else {
      throw buildUnsupportedTypeException(
          type, Class.class, ParameterizedType.class, GenericArrayType.class);
    }
  }

  /**
   * Check if this type is assignable from the given type token.
   *
   * @deprecated this implementation may be inconsistent with javac for types with wildcards.
   */
  @Deprecated
  public boolean isAssignableFrom(TypeToken<?> token) {
    return isAssignableFrom(token.getType());
  }

  /**
   * Private helper function that performs some assignability checks for the provided
   * GenericArrayType.
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
      return isAssignableFrom(
          t, (ParameterizedType) toGenericComponentType, new HashMap<String, Type>());
    }
    // No generic defined on "to"; therefore, return true and let other
    // checks determine assignability
    return true;
  }

  /** Private recursive helper function to actually do the type-safe checking of assignability. */
  private static boolean isAssignableFrom(
      Type from, ParameterizedType to, Map<String, Type> typeVarMap) {

    if (from == null) {
      return false;
    }

    if (to.equals(from)) {
      return true;
    }

    // First figure out the class and any type information.
    Class<?> clazz = GsonTypes.getRawType(from);
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
      if (isAssignableFrom(itype, to, new HashMap<>(typeVarMap))) {
        return true;
      }
    }

    // Interfaces didn't work, try the superclass.
    Type sType = clazz.getGenericSuperclass();
    return isAssignableFrom(sType, to, new HashMap<>(typeVarMap));
  }

  /**
   * Checks if two parameterized types are exactly equal, under the variable replacement described
   * in the typeVarMap.
   */
  private static boolean typeEquals(
      ParameterizedType from, ParameterizedType to, Map<String, Type> typeVarMap) {
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

  private static IllegalArgumentException buildUnsupportedTypeException(
      Type token, Class<?>... expected) {

    // Build exception message
    StringBuilder exceptionMessage = new StringBuilder("Unsupported type, expected one of: ");
    for (Class<?> clazz : expected) {
      exceptionMessage.append(clazz.getName()).append(", ");
    }
    exceptionMessage
        .append("but got: ")
        .append(token.getClass().getName())
        .append(", for type token: ")
        .append(token.toString());

    return new IllegalArgumentException(exceptionMessage.toString());
  }

  /**
   * Checks if two types are the same or are equivalent under a variable mapping given in the type
   * map that was provided.
   */
  private static boolean matches(Type from, Type to, Map<String, Type> typeMap) {
    return to.equals(from)
        || (from instanceof TypeVariable
            && to.equals(typeMap.get(((TypeVariable<?>) from).getName())));
  }

  @Override
  public final int hashCode() {
    return this.hashCode;
  }

  @Override
  public final boolean equals(Object o) {
    return o instanceof TypeToken<?> && GsonTypes.equals(type, ((TypeToken<?>) o).type);
  }

  @Override
  public final String toString() {
    return GsonTypes.typeToString(type);
  }

  /** Gets type literal for the given {@code Type} instance. */
  public static TypeToken<?> get(Type type) {
    return new TypeToken<>(type);
  }

  /** Gets type literal for the given {@code Class} instance. */
  public static <T> TypeToken<T> get(Class<T> type) {
    return new TypeToken<>(type);
  }

  /**
   * Gets a type literal for the parameterized type represented by applying {@code typeArguments} to
   * {@code rawType}. This is mainly intended for situations where the type arguments are not
   * available at compile time. The following example shows how a type token for {@code Map<K, V>}
   * can be created:
   *
   * <pre>{@code
   * Class<K> keyClass = ...;
   * Class<V> valueClass = ...;
   * TypeToken<?> mapTypeToken = TypeToken.getParameterized(Map.class, keyClass, valueClass);
   * }</pre>
   *
   * As seen here the result is a {@code TypeToken<?>}; this method cannot provide any type-safety,
   * and care must be taken to pass in the correct number of type arguments.
   *
   * <p>If {@code rawType} is a non-generic class and no type arguments are provided, this method
   * simply delegates to {@link #get(Class)} and creates a {@code TypeToken(Class)}.
   *
   * @throws IllegalArgumentException If {@code rawType} is not of type {@code Class}, or if the
   *     type arguments are invalid for the raw type
   */
  public static TypeToken<?> getParameterized(Type rawType, Type... typeArguments) {
    Objects.requireNonNull(rawType);
    Objects.requireNonNull(typeArguments);

    // Perform basic validation here because this is the only public API where users
    // can create malformed parameterized types
    if (!(rawType instanceof Class)) {
      // See also https://bugs.openjdk.org/browse/JDK-8250659
      throw new IllegalArgumentException("rawType must be of type Class, but was " + rawType);
    }
    Class<?> rawClass = (Class<?>) rawType;
    TypeVariable<?>[] typeVariables = rawClass.getTypeParameters();

    int expectedArgsCount = typeVariables.length;
    int actualArgsCount = typeArguments.length;
    if (actualArgsCount != expectedArgsCount) {
      throw new IllegalArgumentException(
          rawClass.getName()
              + " requires "
              + expectedArgsCount
              + " type arguments, but got "
              + actualArgsCount);
    }

    // For legacy reasons create a TypeToken(Class) if the type is not generic
    if (typeArguments.length == 0) {
      return get(rawClass);
    }

    // Check for this here to avoid misleading exception thrown by ParameterizedTypeImpl
    if (GsonTypes.requiresOwnerType(rawType)) {
      throw new IllegalArgumentException(
          "Raw type "
              + rawClass.getName()
              + " is not supported because it requires specifying an owner type");
    }

    for (int i = 0; i < expectedArgsCount; i++) {
      Type typeArgument =
          Objects.requireNonNull(typeArguments[i], "Type argument must not be null");
      Class<?> rawTypeArgument = GsonTypes.getRawType(typeArgument);
      TypeVariable<?> typeVariable = typeVariables[i];

      for (Type bound : typeVariable.getBounds()) {
        Class<?> rawBound = GsonTypes.getRawType(bound);

        if (!rawBound.isAssignableFrom(rawTypeArgument)) {
          throw new IllegalArgumentException(
              "Type argument "
                  + typeArgument
                  + " does not satisfy bounds for type variable "
                  + typeVariable
                  + " declared by "
                  + rawType);
        }
      }
    }

    return new TypeToken<>(GsonTypes.newParameterizedTypeWithOwner(null, rawClass, typeArguments));
  }

  /**
   * Gets type literal for the array type whose elements are all instances of {@code componentType}.
   */
  public static TypeToken<?> getArray(Type componentType) {
    return new TypeToken<>(GsonTypes.arrayOf(componentType));
  }
}
