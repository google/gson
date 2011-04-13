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

import java.io.IOException;

/**
 * Definition of a visitor for a JsonElement tree.
 * 
 * @author Inderjeet Singh
 */
interface JsonElementVisitor {
  void visitPrimitive(JsonPrimitive primitive) throws IOException;
  void visitNull() throws IOException;

  void startArray(JsonArray array) throws IOException;
  void visitArrayMember(JsonArray parent, JsonPrimitive member, boolean isFirst) throws IOException;
  void visitArrayMember(JsonArray parent, JsonArray member, boolean isFirst) throws IOException;
  void visitArrayMember(JsonArray parent, JsonObject member, boolean isFirst) throws IOException;
  void visitNullArrayMember(JsonArray parent, boolean isFirst) throws IOException;
  void endArray(JsonArray array) throws IOException;
  
  void startObject(JsonObject object) throws IOException;
  void visitObjectMember(JsonObject parent, String memberName, JsonPrimitive member, 
      boolean isFirst) throws IOException;
  void visitObjectMember(JsonObject parent, String memberName, JsonArray member, 
      boolean isFirst) throws IOException;
  void visitObjectMember(JsonObject parent, String memberName, JsonObject member, 
      boolean isFirst) throws IOException;
  void visitNullObjectMember(JsonObject parent, String memberName, 
      boolean isFirst) throws IOException;
  void endObject(JsonObject object) throws IOException;
}