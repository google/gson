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
package com.google.gson.webservice.definition.rest;

import java.lang.reflect.Type;

/**
 * An id for a rest resource
 *
 * @author inder
 *
 * @param <R> type variable for the rest resource
 */
public final class Id<R> {
  private final long value;
  private final Type typeOfId;

  private Id(long value, Type typeOfId) {
    this.value = value;
    this.typeOfId = typeOfId;
  }

  public long getValue() {
    return value;
  }

  public Type getTypeOfId() {
    return typeOfId;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((typeOfId == null) ? 0 : typeOfId.hashCode());
    result = prime * result + (int)(value ^ (value >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    @SuppressWarnings("unchecked")
    Id<R> other = (Id<R>)obj;
    if (typeOfId == null) {
      if (other.typeOfId != null) return false;
    } else if (!typeOfId.equals(other.typeOfId)) return false;
    if (value != other.value) return false;
    return true;
  }

  public static <RS> Id<RS> get(long value, Type typeOfId) {
    return new Id<RS>(value, typeOfId);
  }

  @Override
  public String toString() {
    return String.format("{value:%s,type:%s}", value, typeOfId);
  }
}
