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
 * Performs numerous field naming translations wrapped up as one object.
 *
 * @author Joel Leitch
 */
abstract class CompositionFieldNamingPolicy extends RecursiveFieldNamingPolicy {

  private final RecursiveFieldNamingPolicy[] fieldPolicies;

  public CompositionFieldNamingPolicy(RecursiveFieldNamingPolicy... fieldNamingPolicies) {
    if (fieldNamingPolicies == null) {
      throw new NullPointerException("naming policies can not be null.");
    }
    this.fieldPolicies = fieldNamingPolicies;
  }

  @Override
  protected String translateName(String target, Type fieldType, Annotation[] annotations) {
    for (RecursiveFieldNamingPolicy policy : fieldPolicies) {
      target = policy.translateName(target, fieldType, annotations);
    }
    return target;
  }
}
