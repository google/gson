/**
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

package com.google.gson.internal;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;

import static com.google.gson.internal.$Gson$Preconditions.checkArgument;
import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

/**
 * Static methods for working with types.
 *
 * @author Bob Lee
 * @author Jesse Wilson
 */
public final class $Gson$Types {
  static final Type[] EMPTY_TYPE_ARRAY = new Type[] {};

  private $Gson$Types() {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns a new parameterized type, applying {@code typeArguments} to
   * {@code rawType} and enclosed by {@code ownerType}.
   *
   * @return a {@link java.io.Serializable serializable} parameterized type.
   */
  public static ParameterizedType newParameterizedTypeWithOwner(
      Type ownerType, Type rawType, Type... typeArguments) {
    return new ParameterizedTypeImpl(ownerType, rawType, typeArguments);
  }

  /**
   * Returns an array type whose elements are all instances of
   * {@code componentType}.
   *
   * @return a {@link java.io.Serializable serializable} generic array type.
   */
  public static GenericArrayType arrayOf(Type componentType) {
    return new GenericArrayTypeImpl(componentType);
  }

  /**
   * Returns a type that represents an unknown type that extends {@code bound}.
   * For example, if {@code bound} is {@code CharSequence.class}, this returns
   * {@code ? extends CharSequence}. If {@code bound} is {@code Object.class},
   * this returns {@code ?}, which is shorthand for {@code ? extends Object}.
   */
  public static WildcardType subtypeOf(Type bound) {
    Type[] upperBounds;
    if (bound instanceof WildcardType) {
      upperBounds = ((WildcardType) bound).getUpperBounds();
    } else {
      upperBounds = new Type[] { bound };
    }
    return new WildcardTypeImpl(upperBounds, EMPTY_TYPE_ARRAY);
  }

  /**
   * Returns a type that represents an unknown supertype of {@code bound}. For
   * example, if {@code bound} is {@code String.class}, this returns {@code ?
   * super String}.
   */
  public static WildcardType supertypeOf(Type bound) {
    Type[] lowerBounds;
    if (bound instanceof WildcardType) {
      lowerBounds = ((WildcardType) bound).getLowerBounds();
    } else {
      lowerBounds = new Type[] { bound };
    }
    return new WildcardTypeImpl(new Type[] { Object.class }, lowerBounds);
  }

  /**
   * Returns a type that is functionally equal but not necessarily equal
   * according to {@link Object#equals(Object) Object.equals()}. The returned
   * type is {@link java.io.Serializable}.
   */
  public static Type canonicalize(Type type) {
    if (type instanceof Class) {
      Class<?> c = (Class<?>) type;
      return c.isArray() ? new GenericArrayTypeImpl(canonicalize(c.getComponentType())) : c;

    } else if (type instanceof ParameterizedType) {
      ParameterizedType p = (ParameterizedType) type;
      return new ParameterizedTypeImpl(p.getOwnerType(),
          p.getRawType(), p.getActualTypeArguments());

    } else if (type instanceof GenericArrayType) {
      GenericArrayType g = (GenericArrayType) type;
      return new GenericArrayTypeImpl(g.getGenericComponentType());

    } else if (type instanceof WildcardType) {
      WildcardType w = (WildcardType) type;
      return new WildcardTypeImpl(w.getUpperBounds(), w.getLowerBounds());

    } else {
      // type is either serializable as-is or unsupported
      return type;
    }
  }

  // This implementation matches how the Java Language Specification defines "Type Erasure"
  // And additionally also covers WildcardType
  public static Class<?> getRawType(Type type) {
    if (type instanceof Class<?>) {
      // type is a normal class.
      return (Class<?>) type;

    } else if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;

      // I'm not exactly sure why getRawType() returns Type instead of Class.
      // Neal isn't either but suspects some pathological case related
      // to nested classes exists.
      Type rawType = parameterizedType.getRawType();
      checkArgument(rawType instanceof Class);
      return (Class<?>) rawType;

    } else if (type instanceof GenericArrayType) {
      Type componentType = ((GenericArrayType)type).getGenericComponentType();
      return Array.newInstance(getRawType(componentType), 0).getClass();

    } else if (type instanceof TypeVariable) {
      return getRawType(getUltimateTypeVariableBound((TypeVariable<?>) type));

    } else if (type instanceof WildcardType) {
      return getRawType(((WildcardType) type).getUpperBounds()[0]);

    } else {
      String className = type == null ? "null" : type.getClass().getName();
      throw new IllegalArgumentException("Expected a Class, ParameterizedType, or "
          + "GenericArrayType, but <" + type + "> is of type " + className);
    }
  }

  static boolean equal(Object a, Object b) {
    return a == b || (a != null && a.equals(b));
  }

  /**
   * Returns true if {@code a} and {@code b} are equal.
   */
  public static boolean equals(Type a, Type b) {
    if (a == b) {
      // also handles (a == null && b == null)
      return true;

    } else if (a instanceof Class) {
      // Class already specifies equals().
      return a.equals(b);

    } else if (a instanceof ParameterizedType) {
      if (!(b instanceof ParameterizedType)) {
        return false;
      }

      // TODO: save a .clone() call
      ParameterizedType pa = (ParameterizedType) a;
      ParameterizedType pb = (ParameterizedType) b;
      return equal(pa.getOwnerType(), pb.getOwnerType())
          && pa.getRawType().equals(pb.getRawType())
          && Arrays.equals(pa.getActualTypeArguments(), pb.getActualTypeArguments());

    } else if (a instanceof GenericArrayType) {
      if (!(b instanceof GenericArrayType)) {
        return false;
      }

      GenericArrayType ga = (GenericArrayType) a;
      GenericArrayType gb = (GenericArrayType) b;
      return equals(ga.getGenericComponentType(), gb.getGenericComponentType());

    } else if (a instanceof WildcardType) {
      if (!(b instanceof WildcardType)) {
        return false;
      }

      WildcardType wa = (WildcardType) a;
      WildcardType wb = (WildcardType) b;
      return Arrays.equals(wa.getUpperBounds(), wb.getUpperBounds())
          && Arrays.equals(wa.getLowerBounds(), wb.getLowerBounds());

    } else if (a instanceof TypeVariable) {
      if (!(b instanceof TypeVariable)) {
        return false;
      }
      TypeVariable<?> va = (TypeVariable<?>) a;
      TypeVariable<?> vb = (TypeVariable<?>) b;
      return va.getGenericDeclaration() == vb.getGenericDeclaration()
          && va.getName().equals(vb.getName());

    } else {
      // This isn't a type we support. Could be a generic array type, wildcard type, etc.
      return false;
    }
  }

  static int hashCodeOrZero(Object o) {
    return o != null ? o.hashCode() : 0;
  }

  public static String typeToString(Type type) {
    return type instanceof Class ? ((Class<?>) type).getName() : type.toString();
  }

  /**
   * Returns the parameterized variant of {@code toResolve} which is implemented (transitively)
   * by {@code context}. If {@code contextRawType} and {@code toResolve} are the same, then
   * {@code context} is returned.
   *
   * <p>For example, for a class {@code StringKeyMap<V> implements Map<String, V>} where
   * {@code contextRawType} is {@code StringKeyMap.class} and {@code toResolve} is
   * {@code Map.class}, the result would be {@code Map<String, V>}.
   *
   * @param context Used as starting point to resolve the supertype
   * @param contextRawType Raw type of {@code context}
   * @param toResolve Raw type of the desired generic supertype
   */
  static Type getGenericSupertype(Type context, Class<?> contextRawType, Class<?> toResolve) {
    if (toResolve == contextRawType) {
      return context;
    }

    // we skip searching through interfaces if toResolve is a class
    if (toResolve.isInterface()) {
      Class<?>[] interfaces = contextRawType.getInterfaces();
      for (int i = 0, length = interfaces.length; i < length; i++) {
        if (interfaces[i] == toResolve) {
          return contextRawType.getGenericInterfaces()[i];
        } else if (toResolve.isAssignableFrom(interfaces[i])) {
          return getGenericSupertype(contextRawType.getGenericInterfaces()[i], interfaces[i], toResolve);
        }
      }
    }

    // check superclasses
    if (!contextRawType.isInterface()) {
      while (contextRawType != Object.class) {
        Class<?> rawSupertype = contextRawType.getSuperclass();
        if (rawSupertype == toResolve) {
          return contextRawType.getGenericSuperclass();
        } else if (toResolve.isAssignableFrom(rawSupertype)) {
          return getGenericSupertype(contextRawType.getGenericSuperclass(), rawSupertype, toResolve);
        }
        contextRawType = rawSupertype;
      }
    }

    if (toResolve == Object.class) {
      return toResolve;
    }
    assert !toResolve.isAssignableFrom(contextRawType);
    throw new IllegalArgumentException("Type " + toResolve + " is not a supertype of " + contextRawType);
  }

  /**
   * Returns the generic form of {@code supertype}. For example, if this is {@code
   * ArrayList<String>}, this returns {@code Iterable<String>} given the input {@code
   * Iterable.class}.
   *
   * @param supertype a superclass of, or interface implemented by, this.
   */
  public static Type getSupertype(Type context, Class<?> contextRawType, Class<?> supertype) {
    // For wildcards and type variables have to use their (ultimate) upper bounds
    // Important: `context` should be determined here in a similar way to how
    // the `getRawType(Type)` method of this class works
    if (context instanceof WildcardType) {
      context = ((WildcardType)context).getUpperBounds()[0];
    }
    // No `else if`; acts as fall-through when bound of wildcard is type variable
    if (context instanceof TypeVariable) {
      context = getUltimateTypeVariableBound((TypeVariable<?>) context);
    }
    checkArgument(supertype.isAssignableFrom(contextRawType));
    return resolve(context, contextRawType,
        $Gson$Types.getGenericSupertype(context, contextRawType, supertype));
  }

  /**
   * Returns the component type of this array type.
   * @throws ClassCastException if this type is not an array.
   */
  public static Type getArrayComponentType(Type array) {
    return array instanceof GenericArrayType
        ? ((GenericArrayType) array).getGenericComponentType()
        : ((Class<?>) array).getComponentType();
  }

  /**
   * Returns the element type of this collection type.
   * @throws IllegalArgumentException if this type is not a collection.
   */
  public static Type getCollectionElementType(Type context, Class<?> contextRawType) {
    Type collectionType = getSupertype(context, contextRawType, Collection.class);

    if (collectionType instanceof WildcardType) {
      collectionType = ((WildcardType)collectionType).getUpperBounds()[0];
    }
    if (collectionType instanceof ParameterizedType) {
      return ((ParameterizedType) collectionType).getActualTypeArguments()[0];
    }
    return Object.class;
  }

  /**
   * Returns a two element array containing this map's key and value types in
   * positions 0 and 1 respectively.
   */
  public static Type[] getMapKeyAndValueTypes(Type context, Class<?> contextRawType) {
    /*
     * Work around a problem with the declaration of java.util.Properties. That
     * class should extend Hashtable<String, String>, but it's declared to
     * extend Hashtable<Object, Object>.
     */
    if (context == Properties.class) {
      return new Type[] { String.class, String.class }; // TODO: test subclasses of Properties!
    }

    Type mapType = getSupertype(context, contextRawType, Map.class);
    // TODO: strip wildcards?
    if (mapType instanceof ParameterizedType) {
      ParameterizedType mapParameterizedType = (ParameterizedType) mapType;
      return mapParameterizedType.getActualTypeArguments();
    }
    return new Type[] { Object.class, Object.class };
  }

  public static Type resolve(Type context, Class<?> contextRawType, Type toResolve) {

    return resolve(context, contextRawType, toResolve, new HashMap<TypeVariable<?>, Type>());
  }

  private static Type resolve(Type context, Class<?> contextRawType, Type toResolve,
                              Map<TypeVariable<?>, Type> visitedTypeVariables) {
    // this implementation is made a little more complicated in an attempt to avoid object-creation
    TypeVariable<?> resolving = null;
    while (true) {
      if (toResolve instanceof TypeVariable) {
        TypeVariable<?> typeVariable = (TypeVariable<?>) toResolve;
        Type previouslyResolved = visitedTypeVariables.get(typeVariable);
        if (previouslyResolved != null) {
          // cannot reduce due to infinite recursion
          return (previouslyResolved == Void.TYPE) ? toResolve : previouslyResolved;
        }

        // Insert a placeholder to mark the fact that we are in the process of resolving this type
        visitedTypeVariables.put(typeVariable, Void.TYPE);
        if (resolving == null) {
          resolving = typeVariable;
        }

        toResolve = resolveTypeVariable(context, contextRawType, typeVariable);
        if (toResolve == typeVariable) {
          break;
        }

      } else if (toResolve instanceof Class && ((Class<?>) toResolve).isArray()) {
        Class<?> original = (Class<?>) toResolve;
        Type componentType = original.getComponentType();
        Type newComponentType = resolve(context, contextRawType, componentType, visitedTypeVariables);
        toResolve = equal(componentType, newComponentType)
            ? original
            : arrayOf(newComponentType);
        break;

      } else if (toResolve instanceof GenericArrayType) {
        GenericArrayType original = (GenericArrayType) toResolve;
        Type componentType = original.getGenericComponentType();
        Type newComponentType = resolve(context, contextRawType, componentType, visitedTypeVariables);
        toResolve = equal(componentType, newComponentType)
            ? original
            : arrayOf(newComponentType);
        break;

      } else if (toResolve instanceof ParameterizedType) {
        ParameterizedType original = (ParameterizedType) toResolve;
        Type ownerType = original.getOwnerType();
        Type newOwnerType = resolve(context, contextRawType, ownerType, visitedTypeVariables);
        boolean changed = !equal(newOwnerType, ownerType);

        Type[] args = original.getActualTypeArguments();
        for (int t = 0, length = args.length; t < length; t++) {
          Type resolvedTypeArgument = resolve(context, contextRawType, args[t], visitedTypeVariables);
          if (!equal(resolvedTypeArgument, args[t])) {
            if (!changed) {
              args = args.clone();
              changed = true;
            }
            args[t] = resolvedTypeArgument;
          }
        }

        toResolve = changed
            ? newParameterizedTypeWithOwner(newOwnerType, original.getRawType(), args)
            : original;
        break;

      } else if (toResolve instanceof WildcardType) {
        WildcardType wildcard = (WildcardType) toResolve;
        Type[] originalLowerBound = wildcard.getLowerBounds();
        Type[] originalUpperBound = wildcard.getUpperBounds();

        if (originalLowerBound.length == 1) {
          Type lowerBound = resolve(context, contextRawType, originalLowerBound[0], visitedTypeVariables);
          if (lowerBound != originalLowerBound[0]) {
            toResolve = supertypeOf(lowerBound);
            break;
          }
        } else if (originalUpperBound.length == 1) {
          Type upperBound = resolve(context, contextRawType, originalUpperBound[0], visitedTypeVariables);
          if (upperBound != originalUpperBound[0]) {
            toResolve = subtypeOf(upperBound);
            break;
          }
        }
        toResolve = wildcard;
        break;

      } else {
        break;
      }
    }
    // ensure that any in-process resolution gets updated with the final result
    if (resolving != null) {
      visitedTypeVariables.put(resolving, toResolve);
    }
    return toResolve;
  }

  static Type resolveTypeVariable(Type context, Class<?> contextRawType, TypeVariable<?> unknown) {
    Class<?> declaredByRaw = declaringClassOf(unknown);

    // we can't reduce this further
    if (declaredByRaw == null) {
      return unknown;
    }

    if (declaredByRaw.isAssignableFrom(contextRawType)) {
      Type declaredBy = getGenericSupertype(context, contextRawType, declaredByRaw);
      if (declaredBy instanceof ParameterizedType) {
        int index = indexOf(declaredByRaw.getTypeParameters(), unknown);
        return ((ParameterizedType) declaredBy).getActualTypeArguments()[index];
      }
    }

    return unknown;
  }

  private static int indexOf(Object[] array, Object toFind) {
    for (int i = 0, length = array.length; i < length; i++) {
      if (toFind.equals(array[i])) {
        return i;
      }
    }
    throw new NoSuchElementException();
  }

  /**
   * Returns the declaring class of {@code typeVariable}, or {@code null} if it was not declared by
   * a class.
   */
  private static Class<?> declaringClassOf(TypeVariable<?> typeVariable) {
    GenericDeclaration genericDeclaration = typeVariable.getGenericDeclaration();
    return genericDeclaration instanceof Class
        ? (Class<?>) genericDeclaration
        : null;
  }

  /**
   * Returns whether {@code start} (transitively) reaches {@code destination} by
   * referencing it as type argument, array component type or bound. All visited
   * type variables are collected in {@code visited} to detect infinite recursion.
   */
  private static boolean reaches(Type start, TypeVariable<?> destination, Set<TypeVariable<?>> visited) {
    if (start == destination) {
      return true;
    }
    // If start is Class, then it cannot reach destination type variable
    if (start instanceof Class) {
      return false;

    } else if (start instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) start;
      for (Type typeArgument : parameterizedType.getActualTypeArguments()) {
        if (reaches(typeArgument, destination, visited)) {
          return true;
        }
      }
      return false;

    } else if (start instanceof TypeVariable) {
      TypeVariable<?> typeVariable = ((TypeVariable<?>) start);
      boolean isNew = visited.add(typeVariable);
      // Prevent infinite recursion if variable has already been visited
      if (isNew) {
        for (Type bound : typeVariable.getBounds()) {
          if (reaches(bound, destination, visited)) {
            return true;
          }
        }
      }
      return false;

    } else if (start instanceof WildcardType) {
      WildcardType wildcard = (WildcardType) start;
      for (Type lowerBound : wildcard.getLowerBounds()) {
        if (reaches(lowerBound, destination, visited)) {
          return true;
        }
      }
      for (Type upperBound : wildcard.getUpperBounds()) {
        if (reaches(upperBound, destination, visited)) {
          return true;
        }
      }
      return false;

    } else if (start instanceof GenericArrayType) {
      return reaches(((GenericArrayType) start).getGenericComponentType(), destination, visited);
    }

    // Should not be reachable
    throw new AssertionError("Unsupported start " + start + " (" + start.getClass() + ")");
  }

  /**
   * Returns the ultimate bound of a type variable, which might itself have other type variables
   * as bounds. For example for {@code Generic<T1 extends T2, T2 extends T3, T3 extends Number>}
   * getting the ultimate bound of {@code T1} returns {@code Number}.
   */
  public static Type getUltimateTypeVariableBound(TypeVariable<?> typeVariable) {
    // Only get the first bound (ignoring other bounds of intersection types)
    Type bound = getUltimateTypeVariable(typeVariable).getBounds()[0];

    // If there is recursion return the raw type
    if (reaches(bound, typeVariable, new HashSet<TypeVariable<?>>(2))) {
      /*
       * Note: The most extensive solution would be to only erase recursive type variable usage
       * within `current`, e.g. `T extends List<T>` would become `List<List>`
       * However, that would be quite complex and might, depending on the relations of the type
       * variables, require recreating a lot of Types with updated parameters (e.g.
       * `List<T>` -> `List<List>`), therefore for now simply erase `current`.
       */
      return getRawType(bound);
    }
    return bound;
  }

  /**
   * Returns the type variable, in a potential chain of type variables, which has
   * a bound which is not a type variable. For example for
   * {@code Generic<T1 extends T2, T2 extends T3, T3 extends Number>} getting the
   * ultimate type variable of {@code T1} returns {@code T3}.
   */
  public static TypeVariable<?> getUltimateTypeVariable(TypeVariable<?> typeVariable) {
    while (true) {
      // Only follow the first bound (ignoring other bounds of intersection types)
      Type bound = typeVariable.getBounds()[0];
      if (bound instanceof TypeVariable) {
        typeVariable = (TypeVariable<?>) bound;
      } else {
        return typeVariable;
      }
    }
  }

  static void checkNotPrimitive(Type type) {
    checkArgument(!(type instanceof Class<?>) || !((Class<?>) type).isPrimitive());
  }

  private static final class ParameterizedTypeImpl implements ParameterizedType, Serializable {
    private final Type ownerType;
    private final Type rawType;
    private final Type[] typeArguments;

    public ParameterizedTypeImpl(Type ownerType, Type rawType, Type... typeArguments) {
      // require an owner type if the raw type needs it
      if (rawType instanceof Class<?>) {
        Class<?> rawTypeAsClass = (Class<?>) rawType;
        boolean isStaticOrTopLevelClass = Modifier.isStatic(rawTypeAsClass.getModifiers())
            || rawTypeAsClass.getEnclosingClass() == null;
        if (ownerType == null && !isStaticOrTopLevelClass) {
          throw new IllegalArgumentException("Local and anonymous classes are not supported");
        }
      }

      this.ownerType = ownerType == null ? null : canonicalize(ownerType);
      this.rawType = canonicalize(rawType);
      this.typeArguments = typeArguments.clone();
      for (int t = 0, length = this.typeArguments.length; t < length; t++) {
        checkNotNull(this.typeArguments[t]);
        checkNotPrimitive(this.typeArguments[t]);
        this.typeArguments[t] = canonicalize(this.typeArguments[t]);
      }
    }

    public Type[] getActualTypeArguments() {
      return typeArguments.clone();
    }

    public Type getRawType() {
      return rawType;
    }

    public Type getOwnerType() {
      return ownerType;
    }

    @Override public boolean equals(Object other) {
      return other instanceof ParameterizedType
          && $Gson$Types.equals(this, (ParameterizedType) other);
    }

    @Override public int hashCode() {
      return Arrays.hashCode(typeArguments)
          ^ rawType.hashCode()
          ^ hashCodeOrZero(ownerType);
    }

    @Override public String toString() {
      int length = typeArguments.length;
      if (length == 0) {
        return typeToString(rawType);
      }

      StringBuilder stringBuilder = new StringBuilder(30 * (length + 1));
      stringBuilder.append(typeToString(rawType)).append("<").append(typeToString(typeArguments[0]));
      for (int i = 1; i < length; i++) {
        stringBuilder.append(", ").append(typeToString(typeArguments[i]));
      }
      return stringBuilder.append(">").toString();
    }

    private static final long serialVersionUID = 0;
  }

  private static final class GenericArrayTypeImpl implements GenericArrayType, Serializable {
    private final Type componentType;

    public GenericArrayTypeImpl(Type componentType) {
      this.componentType = canonicalize(componentType);
    }

    public Type getGenericComponentType() {
      return componentType;
    }

    @Override public boolean equals(Object o) {
      return o instanceof GenericArrayType
          && $Gson$Types.equals(this, (GenericArrayType) o);
    }

    @Override public int hashCode() {
      return componentType.hashCode();
    }

    @Override public String toString() {
      return typeToString(componentType) + "[]";
    }

    private static final long serialVersionUID = 0;
  }

  /**
   * The WildcardType interface supports multiple upper bounds and multiple
   * lower bounds. We only support what the Java 6 language needs - at most one
   * bound. If a lower bound is set, the upper bound must be Object.class.
   */
  private static final class WildcardTypeImpl implements WildcardType, Serializable {
    private final Type upperBound;
    private final Type lowerBound;

    public WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds) {
      checkArgument(lowerBounds.length <= 1);
      checkArgument(upperBounds.length == 1);

      if (lowerBounds.length == 1) {
        checkNotNull(lowerBounds[0]);
        checkNotPrimitive(lowerBounds[0]);
        checkArgument(upperBounds[0] == Object.class);
        this.lowerBound = canonicalize(lowerBounds[0]);
        this.upperBound = Object.class;

      } else {
        checkNotNull(upperBounds[0]);
        checkNotPrimitive(upperBounds[0]);
        this.lowerBound = null;
        this.upperBound = canonicalize(upperBounds[0]);
      }
    }

    public Type[] getUpperBounds() {
      return new Type[] { upperBound };
    }

    public Type[] getLowerBounds() {
      return lowerBound != null ? new Type[] { lowerBound } : EMPTY_TYPE_ARRAY;
    }

    @Override public boolean equals(Object other) {
      return other instanceof WildcardType
          && $Gson$Types.equals(this, (WildcardType) other);
    }

    @Override public int hashCode() {
      // this equals Arrays.hashCode(getLowerBounds()) ^ Arrays.hashCode(getUpperBounds());
      return (lowerBound != null ? 31 + lowerBound.hashCode() : 1)
          ^ (31 + upperBound.hashCode());
    }

    @Override public String toString() {
      if (lowerBound != null) {
        return "? super " + typeToString(lowerBound);
      } else if (upperBound == Object.class) {
        return "?";
      } else {
        return "? extends " + typeToString(upperBound);
      }
    }

    private static final long serialVersionUID = 0;
  }
}
