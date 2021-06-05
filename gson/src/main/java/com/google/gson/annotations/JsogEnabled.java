/*
 * Copyright (C) 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.annotations;

import com.google.gson.TypeAdapter;
import com.google.gson.internal.bind.JsogRegistry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that indicates the Gson should use <a href="https://github.com/jsog/jsog">JSOG</a>
 * to solve circular / redundant references to objects of this class.
 *
 *<p>If your type requires a custom {@link TypeAdapter}s, ensure that it uses
 * {@link JsogRegistry} to support JSOG IDs and references.</p>
 *
 * @author Paulo Costa
 * @see <a href="https://github.com/jsog/jsog">JSOG</a>
 * @see JsogRegistry
 * @see com.google.gson.JsogPolicy
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface JsogEnabled {
}
