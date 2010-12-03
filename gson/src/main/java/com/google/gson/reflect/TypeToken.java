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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
 * {@code TypeLiteral<List<String>> list = new TypeToken<List<String>>() {};}
 *
 * <p>This syntax cannot be used to create type literals that have wildcard
 * parameters, such as {@code Class<?>} or {@code List<? extends CharSequence>}.
 * Such type literals must be constructed programatically, either by {@link
 * Method#getGenericReturnType extracting types from members} or by using the
 * {@link Types} factory class.
 *
 * <p>Along with modeling generic types, this class can resolve type parameters.
 * For example, to figure out what type {@code keySet()} returns on a {@code
 * Map<Integer, String>}, use this code:<pre>   {@code
 *
 *   TypeLiteral<Map<Integer, String>> mapType
 *       = new TypeToken<Map<Integer, String>>() {};
 *   TypeToken<?> keySetType
 *       = mapType.getReturnType(Map.class.getMethod("keySet"));
 *   System.out.println(keySetType); // prints "Set<Integer>"}</pre>
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
    this.rawType = (Class<? super T>) Types.getRawType(type);
    this.hashCode = type.hashCode();
  }

  /**
   * Unsafe. Constructs a type literal manually.
   */
  @SuppressWarnings("unchecked")
  TypeToken(Type type) {
    checkNotNull(type);
    this.type = Types.canonicalize(type);
    this.rawType = (Class<? super T>) Types.getRawType(this.type);
    this.hashCode = this.type.hashCode();
  }

  /**
   * Returns the type from super class's type parameter in {@link Types#canonicalize(java.lang.reflect.Type)
   * canonical form}.
   */
  static Type getSuperclassTypeParameter(Class<?> subclass) {
    Type superclass = subclass.getGenericSuperclass();
    if (superclass instanceof Class) {
      throw new RuntimeException("Missing type parameter.");
    }
    ParameterizedType parameterized = (ParameterizedType) superclass;
    return Types.canonicalize(parameterized.getActualTypeArguments()[0]);
  }

  /**
   * Gets type literal from super class's type parameter.
   */
  static TypeToken<?> fromSuperclassTypeParameter(Class<?> subclass) {
    return new TypeToken<Object>(getSuperclassTypeParameter(subclass));
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
   * Check if this type is assignable from the given class object.
   */
  public boolean isAssignableFrom(Class<?> cls) {
    return isAssignableFrom((Type) cls);
  }

  /**
   * Check if this type is assignable from the given Type.
   */
  public boolean isAssignableFrom(Type from) {
    // TODO: resolve from first, then do something lightweight?

    if (from == null) {
      return false;
    }

    if (type.equals(from)) {
      return true;
    }

    if (type instanceof Class<?>) {
      return rawType.isAssignableFrom(Types.getRawType(from));
    } else if (type instanceof ParameterizedType) {
      return isAssignableFrom(from, (ParameterizedType) type,
          new HashMap<String, Type>());
    } else if (type instanceof GenericArrayType) {
      return rawType.isAssignableFrom(Types.getRawType(from))
          && isAssignableFrom(from, (GenericArrayType) type);
    } else {
      throw buildUnexpectedTypeError(
          type, Class.class, ParameterizedType.class, GenericArrayType.class);
    }
  }

  /**
   * Check if this type is assignable from the given type token.
   */
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
    Class<?> clazz = Types.getRawType(from);
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
    if (isAssignableFrom(sType, to, new HashMap<String, Type>(typeVarMap))) {
      return true;
    }

    return false;
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
  private static boolean matches(Type from, Type to,
      Map<String, Type> typeMap) {
    if (to.equals(from)) return true;

    if (from instanceof TypeVariable<?>) {
      return to.equals(typeMap.get(((TypeVariable<?>)from).getName()));
    }

    return false;
  }

  @Override public final int hashCode() {
    return this.hashCode;
  }

  @Override public final boolean equals(Object o) {
    return o instanceof TypeToken<?>
        && Types.equals(type, ((TypeToken) o).type);
  }

  @Override public final String toString() {
    return Types.typeToString(type);
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


  /** Returns an immutable list of the resolved types. */
  private List<TypeToken<?>> resolveAll(Type[] types) {
    TypeToken<?>[] result = new TypeToken<?>[types.length];
    for (int t = 0; t < types.length; t++) {
      result[t] = resolve(types[t]);
    }
    return Arrays.asList(result);
  }

  /**
   * Resolves known type parameters in {@code toResolve} and returns the result.
   */
  TypeToken<?> resolve(Type toResolve) {
    return TypeToken.get(resolveType(toResolve));
  }

  Type resolveType(Type toResolve) {
    // this implementation is made a little more complicated in an attempt to avoid object-creation
    while (true) {
      if (toResolve instanceof TypeVariable) {
        TypeVariable original = (TypeVariable) toResolve;
        toResolve = Types.resolveTypeVariable(type, rawType, original);
        if (toResolve == original) {
          return toResolve;
        }

      } else if (toResolve instanceof GenericArrayType) {
        GenericArrayType original = (GenericArrayType) toResolve;
        Type componentType = original.getGenericComponentType();
        Type newComponentType = resolveType(componentType);
        return componentType == newComponentType
            ? original
            : Types.arrayOf(newComponentType);

      } else if (toResolve instanceof ParameterizedType) {
        ParameterizedType original = (ParameterizedType) toResolve;
        Type ownerType = original.getOwnerType();
        Type newOwnerType = resolveType(ownerType);
        boolean changed = newOwnerType != ownerType;

        Type[] args = original.getActualTypeArguments();
        for (int t = 0, length = args.length; t < length; t++) {
          Type resolvedTypeArgument = resolveType(args[t]);
          if (resolvedTypeArgument != args[t]) {
            if (!changed) {
              args = args.clone();
              changed = true;
            }
            args[t] = resolvedTypeArgument;
          }
        }

        return changed
            ? Types.newParameterizedTypeWithOwner(newOwnerType, original.getRawType(), args)
            : original;

      } else if (toResolve instanceof WildcardType) {
        WildcardType original = (WildcardType) toResolve;
        Type[] originalLowerBound = original.getLowerBounds();
        Type[] originalUpperBound = original.getUpperBounds();

        if (originalLowerBound.length == 1) {
          Type lowerBound = resolveType(originalLowerBound[0]);
          if (lowerBound != originalLowerBound[0]) {
            return Types.supertypeOf(lowerBound);
          }
        } else if (originalUpperBound.length == 1) {
          Type upperBound = resolveType(originalUpperBound[0]);
          if (upperBound != originalUpperBound[0]) {
            return Types.subtypeOf(upperBound);
          }
        }
        return original;

      } else {
        return toResolve;
      }
    }
  }

  /**
   * Returns the generic form of {@code supertype}. For example, if this is {@code
   * ArrayList<String>}, this returns {@code Iterable<String>} given the input {@code
   * Iterable.class}.
   *
   * @param supertype a superclass of, or interface implemented by, this.
   */
  public TypeToken<?> getSupertype(Class<?> supertype) {
    checkArgument(supertype.isAssignableFrom(rawType));
    return resolve(Types.getGenericSupertype(type, rawType, supertype));
  }

  /**
   * Returns the resolved generic type of {@code field}.
   *
   * @param field a field defined by this or any superclass.
   */
  public TypeToken<?> getFieldType(Field field) {
    if (!field.getDeclaringClass().isAssignableFrom(rawType)) {
      throw new IllegalArgumentException(rawType.getName() + " does not declare field " + field);
    }
    return resolve(field.getGenericType());
  }

  /**
   * Returns the resolved generic parameter types of {@code methodOrConstructor}.
   *
   * @param methodOrConstructor a method or constructor defined by this or any supertype.
   */
  public List<TypeToken<?>> getParameterTypes(Member methodOrConstructor) {
    Type[] genericParameterTypes;

    if (methodOrConstructor instanceof Method) {
      Method method = (Method) methodOrConstructor;
      checkArgument(method.getDeclaringClass().isAssignableFrom(rawType));
      genericParameterTypes = method.getGenericParameterTypes();

    } else if (methodOrConstructor instanceof Constructor) {
      Constructor<?> constructor = (Constructor<?>) methodOrConstructor;
      checkArgument(constructor.getDeclaringClass().isAssignableFrom(rawType));
      genericParameterTypes = constructor.getGenericParameterTypes();

    } else {
      throw new IllegalArgumentException("Not a method or a constructor: " + methodOrConstructor);
    }

    return resolveAll(genericParameterTypes);
  }

  /**
   * Returns the resolved generic exception types thrown by {@code constructor}.
   *
   * @param methodOrConstructor a method or constructor defined by this or any supertype.
   */
  public List<TypeToken<?>> getExceptionTypes(Member methodOrConstructor) {
    Type[] genericExceptionTypes;

    if (methodOrConstructor instanceof Method) {
      Method method = (Method) methodOrConstructor;
      checkArgument(method.getDeclaringClass().isAssignableFrom(rawType));
      genericExceptionTypes = method.getGenericExceptionTypes();

    } else if (methodOrConstructor instanceof Constructor) {
      Constructor<?> constructor = (Constructor<?>) methodOrConstructor;
      checkArgument(constructor.getDeclaringClass().isAssignableFrom(rawType));
      genericExceptionTypes = constructor.getGenericExceptionTypes();

    } else {
      throw new IllegalArgumentException("Not a method or a constructor: " + methodOrConstructor);
    }

    return resolveAll(genericExceptionTypes);
  }

  /**
   * Returns the resolved generic return type of {@code method}.
   *
   * @param method a method defined by this or any supertype.
   */
  public TypeToken<?> getReturnType(Method method) {
    checkArgument(method.getDeclaringClass().isAssignableFrom(rawType));
    return resolve(method.getGenericReturnType());
  }

  static void checkNotNull(Object obj) {
    checkArgument(obj != null);
  }

  static void checkArgument(boolean condition) {
    if (!condition) {
      throw new IllegalArgumentException("condition failed: " + condition);
    }
  }

  // TODO: these methods are required by GSON but don't need to be public. Remove?

  /**
   * Returns true if this type is an array.
   */
  public boolean isArray() {
    return type instanceof GenericArrayType;
  }

  /**
   * Returns true if this type is a primitive.
   */
  public boolean isPrimitive() {
    return type == boolean.class
        || type == byte.class
        || type == char.class
        || type == double.class
        || type == float.class
        || type == int.class
        || type == long.class
        || type == short.class
        || type == void.class;
  }

  /**
   * Returns the component type of this array type.
   * @throws ClassCastException if this type is not an array.
   */
  public Type getArrayComponentType() {
    return ((GenericArrayType) type).getGenericComponentType();
  }

  /**
   * Returns the element type of this collection type.
   * @throws IllegalArgumentException if this type is not a collection.
   */
  public Type getCollectionElementType() {
    TypeToken<?> collectionType = getSupertype(Collection.class);
    return ((ParameterizedType) collectionType.getType()).getActualTypeArguments()[0];
  }

  /**
   * Returns a two element array containing this map's key and value types in
   * positions 0 and 1 respectively.
   */
  public Type[] getMapKeyAndValueTypes() {
    /*
     * Work around a problem with the declaration of java.util.Properties. That
     * class should extend Hashtable<String, String>, but it's declared to
     * extend Hashtable<Object, Object>.
     */
    if (type == Properties.class) {
      return new Type[] { String.class, String.class }; // TODO: test subclasses of Properties!
    }

    TypeToken<?> mapTypeToken = TypeToken.get(type).getSupertype(Map.class);
    ParameterizedType mapParameterizedType = (ParameterizedType) mapTypeToken.getType();
    return mapParameterizedType.getActualTypeArguments();
  }
}
