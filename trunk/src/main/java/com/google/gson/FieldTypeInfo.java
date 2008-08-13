package com.google.gson;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;

/**
 * Class to extract information about type for a field.
 *
 * @author Inderjeet Singh
 */
final class FieldTypeInfo {

  private final Field f;
  private final Class<?> classOfF;
  private final Type genericizedTypeOfClassOfF;
  private final Type actualTypeOfF;
  private final Class<?> actualClassOfF;

  FieldTypeInfo(Field f, Class<?> classOfF, Type genericizedTypeOfClassOfF) {
    this.f = f;
    this.classOfF = classOfF;
    this.genericizedTypeOfClassOfF = genericizedTypeOfClassOfF;
    this.actualTypeOfF = getActualTypeOfField();
    this.actualClassOfF = TypeUtils.toRawClass(actualTypeOfF);
  }

  public Type getActualType() {
    return actualTypeOfF;
  }

  public boolean isCollectionOrArray() {
    return Collection.class.isAssignableFrom(actualClassOfF) || isArray();
  }

  public boolean isArray() {
    return TypeUtils.isArray(actualClassOfF);
  }

  public boolean isPrimitive() {
    return Primitives.isWrapperType(Primitives.wrap(actualClassOfF));
  }

  public boolean isString() {
    return actualClassOfF == String.class;
  }

  public boolean isPrimitiveOrStringAndNotAnArray() {
    return (isPrimitive() || isString()) && !isArray();
  }

  private Type getActualTypeOfField() {
    Type type = f.getGenericType();
    if (type instanceof Class || type instanceof ParameterizedType
        || type instanceof GenericArrayType) {
      return type;
    } else if (type instanceof TypeVariable) {
      // The class definition has the actual types used for the type variables.
      // Find the matching actual type for the Type Variable used for the field.
      // For example, class Foo<A> { A a; }
      // new Foo<Integer>(); defines the actual type of A to be Integer.
      // So, to find the type of the field a, we will have to look at the class'
      // actual type arguments.
      TypeVariable<?> fieldTypeVariable = (TypeVariable<?>) type;
      TypeVariable<?>[] classTypeVariables = classOfF.getTypeParameters();
      ParameterizedType objParameterizedType = (ParameterizedType) genericizedTypeOfClassOfF;
      int indexOfActualTypeArgument = getIndex(classTypeVariables, fieldTypeVariable);
      Type[] actualTypeArguments = objParameterizedType.getActualTypeArguments();
      return actualTypeArguments[indexOfActualTypeArgument];
    } else {
      throw new IllegalArgumentException("Type \'" + type + "\' is not a Class, "
          + "ParameterizedType, GenericArrayType or TypeVariable. Can't extract type.");
    }
  }

  private static int getIndex(TypeVariable<?>[] types, TypeVariable<?> type) {
    for (int i = 0; i < types.length; ++i) {
      if (type.equals(types[i])) {
        return i;
      }
    }
    throw new IllegalStateException("How can the type variable not be present in the class declaration!");
  }
}
