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

import com.google.gson.reflect.ObjectNavigator;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * A visitor that adds JSON elements corresponding to each field of an object
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
final class JsonSerializationVisitor implements ObjectNavigator.Visitor {

  public void endVisitingObject(Object node) {
    throw new UnsupportedOperationException();
  }

  public void startVisitingObject(Object node) {
    throw new UnsupportedOperationException();
  }

  public void visitArray(Object array, Type componentType) {
    throw new UnsupportedOperationException();
  }

  public void visitArrayField(Field f, Object obj) {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings("unchecked")
  public void visitCollection(Collection collection, Type componentType) {
    throw new UnsupportedOperationException();
  }

  public void visitCollectionField(Field f, Object obj) {
    throw new UnsupportedOperationException();
  }

  public void visitEnum(Object obj, Type objType) {
    throw new UnsupportedOperationException();
  }

  public void visitObjectField(Field f, Object obj) {
    throw new UnsupportedOperationException();
  }

  public void visitPrimitiveField(Field f, Object obj) {
    throw new UnsupportedOperationException();
  }

  public void visitPrimitiveValue(Object obj) {
    throw new UnsupportedOperationException();
  }

  public boolean visitUsingCustomHandler(Object obj, Type objType) {
    throw new UnsupportedOperationException();
  }
}
