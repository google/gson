/*
 * Copyright (C) 2018 Google Inc.
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

package com.google.gson.visitor;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Interface for traversing the json
 * tree using visitor design pattern.
 *
 * @author sjaiswal on 8/2/21
 */
public interface JsonElementVisitor <T> {
    /**
     * Handles the processing of JsonObject.
     *
     * @param jsonObject
     *          holds json object.
     * @param data
     *          Buffer for storing processed data.
     * @return
     *          Data of processed json object.
     */
    public T visitJsonObject(JsonObject jsonObject, T data);


    /**
     * Handles the processing of JsonArray.
     *
     * @param jsonArray
     *          holds json array.
     * @param data
     *          Buffer for storing processed data.
     * @return
     *          Data of processed json array.
     */
    public T visitJsonArray(JsonArray jsonArray, T data);

    /**
     * Handles the processing of JsonPrimitive.
     *
     * @param jsonPrimitive
     *          holds json primitive.
     * @param data
     *          Buffer for storing processed data.
     * @return
     *          Data of processed json primitive.
     */
    public T visitJsonPrimitive(JsonPrimitive jsonPrimitive, T data);


    /**
     * Handles the processing of JsonNull.
     *
     * @param jsonNull
     *          holds json null.
     * @param data
     *          Buffer for storing processed data.
     * @return
     *          Data of processed json null.
     */
    public T visitJsonNull(JsonNull jsonNull, T data);
}
