/*
 * Copyright (C) 2016 Markus Pfeiffer.
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
 * An annotation that defines additional conditions to apply when serializing a member that is
 * {@code null}.
 * <p>
 * This annotation has no effect if a member is excluded from serialization. A member may be
 * excluded from serialization if it has a {@code transient} modifier or if it does not have an
 * {@link Expose} annotation and {@link com.google.gson.Gson} has been built with a
 * {@link com.google.gson.GsonBuilder} and the
 * {@link com.google.gson.GsonBuilder#excludeFieldsWithoutExposeAnnotation()} method has been
 * invoked.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Serialize {

    /**
     * Returns a value indicating whether the field should be serialized if it is {@code null}.
     * @return the desired inclusion of the field when it is serialized.
     */
    Inclusion value() default Inclusion.DEFAULT;

    /**
     * Indicates the conditions under which a member should be serialized.
     */
    public static enum Inclusion {

        /**
         * Use Gson's default behavior.
         * <p>
         * The value is included unless it is {@code null} or {@link com.google.gson.Gson} has been
         * built with a {@link com.google.gson.GsonBuilder} and the
         * {@link com.google.gson.GsonBuilder#serializeNulls()} method has been invoked.
         */
        DEFAULT,

        /**
         * The value is excluded if it is {@code null}.
         * <p>
         * Note that this matches the default behavior unless {@link com.google.gson.Gson} has been
         * built with a {@link com.google.gson.GsonBuilder} and the
         * {@link com.google.gson.GsonBuilder#serializeNulls()} method has been invoked.
         */
        NON_NULL,

        /**
         * The value is always included, even if it is {@code null}.
         * <p>
         * Note that this has no effect if {@link com.google.gson.Gson} has been built with a
         * {@link com.google.gson.GsonBuilder} and the
         * {@link com.google.gson.GsonBuilder#serializeNulls()} method has been invoked.
         */
        ALWAYS
    }
}
