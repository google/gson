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

import com.google.gson.annotations.SerializeAs;

import java.lang.reflect.Field;

/**
 * A {@link FieldNamingStrategy} that acts as a chain of responsibility.  If the
 * {@link com.google.gson.annotations.SerializeAs} annotation is applied to a field then this
 * strategy will translate the name to the {@code serializeAs.value()}; otherwise it delegates
 * to the wrapped {@link FieldNamingStrategy}.
 *
 * <p>NOTE: this class performs JSON field name validation for any of the fields marked with
 * an {@code @SerializeAs} annotation.</p>
 *
 * @see SerializeAs
 *
 * @author Joel Leitch
 */
class SerializeAsAnnotationInterceptingNamingPolicy implements FieldNamingStrategy {
  private static final JsonFieldNameValidator fieldNameValidator = new JsonFieldNameValidator();
  private final FieldNamingStrategy delegate;

  public SerializeAsAnnotationInterceptingNamingPolicy(FieldNamingStrategy delegate) {
    this.delegate = delegate;
  }

  public String translateName(Field f) {
    Preconditions.checkNotNull(f);
    SerializeAs serializeAs = f.getAnnotation(SerializeAs.class);
    if (serializeAs != null) {
      return fieldNameValidator.validate(serializeAs.value());
    } else {
      return delegate.translateName(f);
    }
  }
}
