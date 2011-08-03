/*
 * Copyright (C) 2011 Google Inc.
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
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.ObjectNavigator.Visitor;
import com.google.gson.internal.$Gson$Preconditions;
import com.google.gson.internal.$Gson$Types;

/**
 * Visits each of the fields of the specified class using reflection
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 * @author Jesse Wilson
 */
final class ReflectingFieldNavigator {

  private static final Cache<Type, List<FieldAttributes>> fieldsCache =
    new LruCache<Type, List<FieldAttributes>>(500);

  private final ExclusionStrategy exclusionStrategy;

  /**
   * @param exclusionStrategy the concrete strategy object to be used to filter out fields of an
   *   object.
   */
  ReflectingFieldNavigator(ExclusionStrategy exclusionStrategy) {
    this.exclusionStrategy = $Gson$Preconditions.checkNotNull(exclusionStrategy);
  }

  /**
   * @param objTypePair The object,type (fully genericized) being navigated
   * @param visitor the visitor to visit each field with
   */
  void visitFieldsReflectively(ObjectTypePair objTypePair, Visitor visitor) {
    Type moreSpecificType = objTypePair.getMoreSpecificType();
    Object obj = objTypePair.getObject();
    for (FieldAttributes fieldAttributes : getAllFields(moreSpecificType, objTypePair.getType())) {
      if (exclusionStrategy.shouldSkipField(fieldAttributes)
          || exclusionStrategy.shouldSkipClass(fieldAttributes.getDeclaredClass())) {
        continue; // skip
      }
      Type resolvedTypeOfField = getMoreSpecificType(fieldAttributes.getResolvedType(), obj, fieldAttributes);
      boolean visitedWithCustomHandler =
        visitor.visitFieldUsingCustomHandler(fieldAttributes, resolvedTypeOfField, obj);
      if (!visitedWithCustomHandler) {
        if ($Gson$Types.isArray(resolvedTypeOfField)) {
          visitor.visitArrayField(fieldAttributes, resolvedTypeOfField, obj);
        } else {
          visitor.visitObjectField(fieldAttributes, resolvedTypeOfField, obj);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static Type getMoreSpecificType(Type type, Object obj, FieldAttributes fieldAttributes) {
    try {
      if (obj != null && (Object.class == type || type instanceof TypeVariable)) {
        Object fieldValue = fieldAttributes.get(obj);
        if (fieldValue != null) {
          type = fieldValue.getClass();
        }
      }
    } catch (IllegalAccessException e) {
    }
    return type;
  }

  private List<FieldAttributes> getAllFields(Type type, Type declaredType) {
    List<FieldAttributes> fields = fieldsCache.getElement(type);
    if (fields == null) {
      fields = new ArrayList<FieldAttributes>();
      for (Class<?> curr : getInheritanceHierarchy(type)) {
        Field[] currentClazzFields = curr.getDeclaredFields();
        AccessibleObject.setAccessible(currentClazzFields, true);
        Field[] classFields = currentClazzFields;
        for (Field f : classFields) {
          fields.add(new FieldAttributes(curr, f, declaredType));
        }
      }
      fieldsCache.addElement(type, fields);
    }
    return fields;
  }

  /**
   * Returns a list of classes corresponding to the inheritance of specified type
   */
  private List<Class<?>> getInheritanceHierarchy(Type type) {
    List<Class<?>> classes = new ArrayList<Class<?>>();
    Class<?> topLevelClass = $Gson$Types.getRawType(type);
    for (Class<?> curr = topLevelClass; curr != null && !curr.equals(Object.class); curr =
      curr.getSuperclass()) {
      if (!curr.isSynthetic()) {
        classes.add(curr);
      }
    }
    return classes;
  }
}
