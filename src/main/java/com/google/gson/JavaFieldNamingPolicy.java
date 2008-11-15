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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * A simple implementation of the {@link FieldNamingStrategy} interface such that it does not
 * perform any string translation of the incoming field name.
 *
 * <p>The following is an example:</p>
 *
 * <pre>
 * class IntWrapper {
 *   public int integerField = 0;
 * }
 *
 * JavaFieldNamingPolicy policy = new JavaFieldNamingPolicy();
 * String translatedFieldName =
 *     policy.translateName(IntWrapper.class.getField("integerField"));
 *
 * assert("integerField".equals(translatedFieldName));
 * </pre>
 *
 * <p>This is the default {@link FieldNamingStrategy} used by Gson.</p>
 *
 * @author Joel Leitch
 */
class JavaFieldNamingPolicy extends RecursiveFieldNamingPolicy {

  @Override
  protected String translateName(String target, Type fieldType, Annotation[] annotations) {
    return target;
  }
}
