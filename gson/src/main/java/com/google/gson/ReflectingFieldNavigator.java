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

import com.google.gson.ObjectNavigator.Visitor;
import com.google.gson.internal.LruCache;
import com.google.gson.internal.Preconditions;
import com.google.gson.internal.Types;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Visits each of the fields of the specified class using reflection
 * 
 * @author Inderjeet Singh
 * @author Joel Leitch
 * @author Jesse Wilson
 */
final class ReflectingFieldNavigator {
  private static final LruCache<Type, List<Class<?>>> classCache =
    new LruCache<Type, List<Class<?>>>(500);
  private static final LruCache<Class<?>, Field[]> fieldsCache =
    new LruCache<Class<?>, Field[]>(500);

  private final ExclusionStrategy exclusionStrategy;

  /**
   * @param exclusionStrategy the concrete strategy object to be used to filter out fields of an
   *   object.
   */
  ReflectingFieldNavigator(ExclusionStrategy exclusionStrategy) {
    this.exclusionStrategy = Preconditions.checkNotNull(exclusionStrategy);
  }

  /**
   * @param objTypePair The object,type (fully genericized) being navigated
   * @param visitor the visitor to visit each field with
   */
  void visitFieldsReflectively(ObjectTypePair objTypePair, Visitor visitor) {
    ObjectTypePair currObjTypePair = objTypePair.toMoreSpecificType();
    Class<?> topLevelClass = Types.getRawType(currObjTypePair.type);
    for (Class<?> curr : getInheritanceHierarchy(currObjTypePair.type)) {
      navigateClassFields(objTypePair.getObject(), objTypePair.type, curr, visitor);
    }
  }

  /**
   * Returns a list of classes corresponding to the inheritance of specified type 
   */
  private List<Class<?>> getInheritanceHierarchy(Type type) {
    List<Class<?>> classes = classCache.get(type);
    if (classes == null) {
      classes = new ArrayList<Class<?>>();
      Class<?> topLevelClass = Types.getRawType(type);
      for (Class<?> curr = topLevelClass; curr != null && !curr.equals(Object.class); curr =
        curr.getSuperclass()) {
        if (!curr.isSynthetic()) {
          classes.add(curr);
        }
      }
      classCache.put(type, classes);
    }
    return classes;
  }

  private void navigateClassFields(Object obj, Type objType,
      Class<?> classInInheritanceHierarchyForObj, Visitor visitor) {
    Field[] fields = getFields(classInInheritanceHierarchyForObj);
    for (Field f : fields) {
      FieldAttributes fieldAttributes = new FieldAttributes(classInInheritanceHierarchyForObj, f);
      if (exclusionStrategy.shouldSkipField(fieldAttributes)
          || exclusionStrategy.shouldSkipClass(fieldAttributes.getDeclaredClass())) {
        continue; // skip
      }
      Type declaredTypeOfField = getTypeInfoForField(f, objType);
      boolean visitedWithCustomHandler =
          visitor.visitFieldUsingCustomHandler(fieldAttributes, declaredTypeOfField, obj);
      if (!visitedWithCustomHandler) {
        if (Types.isArray(declaredTypeOfField)) {
          visitor.visitArrayField(fieldAttributes, declaredTypeOfField, obj);
        } else {
          visitor.visitObjectField(fieldAttributes, declaredTypeOfField, obj);
        }
      }
    }
  }

  private Field[] getFields(Class<?> clazz) {
    Field[] fields = fieldsCache.get(clazz);
    if (fields == null) {
      fields = clazz.getDeclaredFields();
      AccessibleObject.setAccessible(fields, true);
      fieldsCache.put(clazz, fields);
    }
    return fields;
  }


  /**
   * Evaluates the "actual" type for the field.  If the field is a "TypeVariable" or has a
   * "TypeVariable" in a parameterized type then it evaluates the real type.
   *
   * @param f the actual field object to retrieve the type from
   * @param typeDefiningF the type that contains the field {@code f}
   * @return the type information for the field
   */
  public static Type getTypeInfoForField(Field f, Type typeDefiningF) {
    Class<?> rawType = Types.getRawType(typeDefiningF);
    if (!f.getDeclaringClass().isAssignableFrom(rawType)) {
      // this field is unrelated to the type; the user probably omitted type information
      return f.getGenericType();
    }
    return Types.resolve(typeDefiningF, rawType, f.getGenericType());
  }
}
