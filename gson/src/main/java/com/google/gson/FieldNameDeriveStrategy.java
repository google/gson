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

import java.lang.reflect.Field;
import java.util.List;

/**
 * @since 2.82
 */
public interface FieldNameDeriveStrategy {

    /**
     * derive the field name to other name
     *
     * @param f          the field object that we are translating
     * @param fieldNames original field names
     * @return the derive field names.
     * @since 1.3
     */
    public List<String> deriveNames(Field f, List<String> fieldNames);
}
