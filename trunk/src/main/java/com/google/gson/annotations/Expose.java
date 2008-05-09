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

package com.google.gson.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that indicates this member should be exposed for Json
 * serialization or deserialization.
 * 
 * <p>
 * This annotation has no effect unless you build {@link com.google.gson.Gson} 
 * with a {@link com.google.gson.GsonBuilder} and invoke 
 * {@link com.google.gson.GsonBuilder#excludeFieldsWithoutExposeAnnotation()}
 * `method.
 *
 * @author Inderjeet Singh
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Expose {
  // This is a marker annotation with no additional properties 
}
