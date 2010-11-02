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
package com.google.gson.webservice.definition;

import com.google.gson.webservice.definition.internal.utils.Preconditions;

/**
 * A typed key for use in a {@link ParamMap} or a {@link ParamMapSpec}.
 *
 * @author inder
 *
 * @param <T> Intended type of the content for the key
 */
public class TypedKey<T> {
  private final String name;
  private final Class<T> classOfT;

  public TypedKey(String name, Class<T> classOfT) {
    Preconditions.checkNotNull(name);
    Preconditions.checkNotNull(classOfT);

    this.name = name;
    this.classOfT = classOfT;
  }

  public String getName() {
    return name;
  }

  public Class<T> getClassOfT() {
    return classOfT;
  }

  
  @Override
  public int hashCode() {
    return name.hashCode() + classOfT.getCanonicalName().hashCode() >> 1;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    TypedKey<?> other = (TypedKey<?>) obj;
    return name.equals(other.name) && classOfT.equals(other.classOfT);
  }

  @Override
  public String toString() {
    return String.format("{name:%s, classOfT:%s}", name, classOfT);
  }
}
