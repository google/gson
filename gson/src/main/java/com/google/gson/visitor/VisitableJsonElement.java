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

/**
 * Interface for all visitable JsonElement types.
 *
 * @author sjaiswal on 8/2/21
 */
public interface VisitableJsonElement {

    /**
     * This method accepts a JsonElementVisitor. All JsonElement types
     * should implement this and call the visitor by passing itself.
     * @param visitor
     *        JsonElementVisitor for traversing the json tree.
     * @param data
     *        Buffer to hold the result of processed node.
     * @param <T>
     * @return
     *        Returns the processed data.
     */
    public<T> T accept(JsonElementVisitor visitor, T data);
}
