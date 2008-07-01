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

import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Field;

/**
 * A {@link FieldNamingStrategy} that acts as a chain of responsibility.  If the
 * {@link com.google.gson.annotations.SerializedName} annotation is applied to a field then this
 * strategy will translate the name to the {@code serializedName.value()}; otherwise it delegates
 * to the wrapped {@link FieldNamingStrategy}.
 *
 * <p>NOTE: this class performs JSON field name validation for any of the fields marked with
 * an {@code @SerializedName} annotation.</p>
 *
 * @see SerializedName
 *
 * @author Joel Leitch
 */
class SerializedNameAnnotationInterceptingNamingPolicy implements FieldNamingStrategy {
  private static final JsonFieldNameValidator fieldNameValidator = new JsonFieldNameValidator();
  private final FieldNamingStrategy delegate;

  public SerializedNameAnnotationInterceptingNamingPolicy(FieldNamingStrategy delegate) {
    this.delegate = delegate;
  }

  public String translateName(Field f) {
    Preconditions.checkNotNull(f);
    SerializedName serializedName = f.getAnnotation(SerializedName.class);
    if (serializedName != null) {
      return fieldNameValidator.validate(serializedName.value());
    } else {
      return delegate.translateName(f);
    }
  }
}
