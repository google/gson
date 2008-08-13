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

package com.google.gson;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * Provides ability to apply a visitor to an object and all of its fields recursively.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
final class ObjectNavigator {

  public interface Visitor {
    /**
     * This is called before the object navigator starts visiting the current object
     */
    void startVisitingObject(Object node);

    /**
     * This is called after the object navigator finishes visiting the current object
     */
    void endVisitingObject(Object node);

    /**
     * This is called to visit the current object if it is an iterable
     *
     * @param componentType the type of each element of the component
     */
    void visitCollection(@SuppressWarnings("unchecked") Collection collection, Type componentType);

    /**
     * This is called to visit the current object if it is an array
     */
    void visitArray(Object array, Type componentType);

    /**
     * This is called to visit the current object if it is a primitive
     */
    void visitPrimitiveValue(Object obj);

    /**
     * This is called to visit an object field of the current object
     */
    void visitObjectField(Field f, Type typeOfF, Object obj);

    /**
     * This is called to visit a field of type Collection of the current object
     */
    void visitCollectionField(Field f, Type typeOfF, Object obj);

    /**
     * This is called to visit an array field of the current object
     */
    void visitArrayField(Field f, Type typeOfF, Object obj);

    /**
     * This is called to visit a primitive field of the current object
     */
    void visitPrimitiveField(Field f, Type typeOfF, Object obj);

    /**
     * This is called to visit an enum object
     */
    public void visitEnum(Object obj, Type objType);

    /**
     * This is called to visit an object using a custom handler
     * @return true if a custom handler exists, false otherwise
     */
    public boolean visitUsingCustomHandler(Object obj, Type objType);
  }

  private final ExclusionStrategy exclusionStrategy;
  private final MemoryRefStack<Object> ancestors;
  private final Object obj;
  private final Type objType;

  /**
   * @param obj The object being navigated
   * @param objType The (fully genericized) type of the object being navigated
   * @param exclusionStrategy the concrete strategy object to be used to
   *        filter out fields of an object.
   */
  ObjectNavigator(Object obj, Type objType, ExclusionStrategy exclusionStrategy,
      MemoryRefStack<Object> ancestors) {
    Preconditions.checkNotNull(exclusionStrategy);
    Preconditions.checkNotNull(ancestors);

    this.obj = obj;
    this.objType = objType;
    this.exclusionStrategy = exclusionStrategy;
    this.ancestors = ancestors;
  }

  /**
   * Navigate all the fields of the specified object.
   * If a field is null, it does not get visited.
   */
  public void accept(Visitor visitor) {
    if (obj == null) {
      return;
    }
    TypeInfo objTypeInfo = new TypeInfo(objType);
    if (exclusionStrategy.shouldSkipClass(objTypeInfo.getRawClass())) {
      return;
    }

    if (ancestors.contains(obj)) {
      throw new IllegalStateException("Circular reference found: " + obj);
    }
    ancestors.push(obj);

    try {
      if (objTypeInfo.isCollectionOrArray()) {
        if (objTypeInfo.isArray()) {
          visitor.visitArray(obj, objType);
        } else { // must be a collection
          visitor.visitCollection((Collection<?>)obj, objType);
        }
      } else if (objTypeInfo.getRawClass().isEnum()) {
        visitor.visitEnum(obj, objType);
      } else if (objTypeInfo.isPrimitiveOrStringAndNotAnArray()) {
        visitor.visitPrimitiveValue(obj);
      } else {
        if (!visitor.visitUsingCustomHandler(obj, objType)) {
          visitor.startVisitingObject(obj);
          // For all classes in the inheritance hierarchy (including the current class),
          // visit all fields
          for (Class<?> curr = objTypeInfo.getRawClass();
              curr != null && !curr.equals(Object.class); curr = curr.getSuperclass()) {
            if (!curr.isSynthetic()) {
              navigateClassFields(obj, curr, visitor);
            }
          }
          visitor.endVisitingObject(obj);
        }
      }
    } finally {
      ancestors.pop();
    }
  }

  private void navigateClassFields(Object obj, Class<?> clazz, Visitor visitor) {
    Field[] fields = clazz.getDeclaredFields();
    AccessibleObject.setAccessible(fields, true);
    for (Field f : fields) {
      TypeInfo fieldTypeInfo = TypeInfoFactory.getTypeInfoForField(f, objType);
      Type actualTypeOfField = fieldTypeInfo.getActualType();
      if (exclusionStrategy.shouldSkipField(f)) {
        continue; // skip
      } else if (fieldTypeInfo.isCollectionOrArray()) {
        if (fieldTypeInfo.isArray()) {
          visitor.visitArrayField(f, actualTypeOfField, obj);
        } else { // must be Collection
          visitor.visitCollectionField(f, actualTypeOfField, obj);
        }
      } else if (fieldTypeInfo.isPrimitiveOrStringAndNotAnArray()) {
        visitor.visitPrimitiveField(f, actualTypeOfField, obj);
      } else {
        visitor.visitObjectField(f, actualTypeOfField, obj);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static final Class[] PRIMITIVE_TYPES = { int.class, long.class, short.class, float.class,
      double.class, byte.class, boolean.class, Integer.class, Long.class, Short.class, Float.class,
      Double.class, Byte.class, Boolean.class };

  @SuppressWarnings("unchecked")
  static boolean isPrimitiveOrString(Object target) {
    if (target instanceof String) {
      return true;
    }
    Class<?> classOfPrimitive = target.getClass();
    for (Class standardPrimitive : PRIMITIVE_TYPES) {
      if (standardPrimitive.isAssignableFrom(classOfPrimitive)) {
        return true;
      }
    }
    return false;
  }
}
