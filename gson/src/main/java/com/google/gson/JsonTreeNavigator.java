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
import java.util.Map;

/**
 * A navigator to navigate a tree of JsonElement nodes in Depth-first order
 * 
 * @author Inderjeet Singh
 */
final class JsonTreeNavigator {
  private final JsonElementVisitor visitor;
  private final boolean visitNulls;

  JsonTreeNavigator(JsonElementVisitor visitor, boolean visitNulls) {
    this.visitor = visitor;
    this.visitNulls = visitNulls;
  }
  
  public void navigate(JsonElement element) throws IOException {
    if (element.isJsonNull()) {
      visitor.visitNull();
    } else if (element.isJsonArray()) {
      JsonArray array = element.getAsJsonArray();
      visitor.startArray(array);
      boolean isFirst = true;
      for (JsonElement child : array) {
        visitChild(array, child, isFirst);
        if (isFirst) {
          isFirst = false;
        }
      }
      visitor.endArray(array);
    } else if (element.isJsonObject()) {
      JsonObject object = element.getAsJsonObject();
      visitor.startObject(object);
      boolean isFirst = true;
      for (Map.Entry<String, JsonElement> member : object.entrySet()) {
        boolean visited = visitChild(object, member.getKey(), member.getValue(), isFirst);
        if (visited && isFirst) {
          isFirst = false;
        }
      }
      visitor.endObject(object);
    } else { // must be JsonPrimitive
      visitor.visitPrimitive(element.getAsJsonPrimitive());
    }    
  }

  /**
   * Returns true if the child was visited, false if it was skipped.
   */
  private boolean visitChild(JsonObject parent, String childName, JsonElement child, 
      boolean isFirst) throws IOException {
    if (child.isJsonNull()) {
      if (visitNulls) {
        visitor.visitNullObjectMember(parent, childName, isFirst);
        navigate(child.getAsJsonNull());
      } else { // Null value is being skipped.
        return false;
      }
    } else if (child.isJsonArray()) {
      JsonArray childAsArray = child.getAsJsonArray();
      visitor.visitObjectMember(parent, childName, childAsArray, isFirst);
      navigate(childAsArray);
    } else if (child.isJsonObject()) {
      JsonObject childAsObject = child.getAsJsonObject();
      visitor.visitObjectMember(parent, childName, childAsObject, isFirst);
      navigate(childAsObject);
    } else { // is a JsonPrimitive
      visitor.visitObjectMember(parent, childName, child.getAsJsonPrimitive(), isFirst);          
    }
    return true;
  }

  /**
   * Returns true if the child was visited, false if it was skipped.
   */
  private void visitChild(JsonArray parent, JsonElement child, boolean isFirst) throws IOException {
    if (child.isJsonNull()) {
      visitor.visitNullArrayMember(parent, isFirst);
      navigate(child);
	  } else if (child.isJsonArray()) {
      JsonArray childAsArray = child.getAsJsonArray();
      visitor.visitArrayMember(parent, childAsArray, isFirst);
      navigate(childAsArray);
    } else if (child.isJsonObject()) {
      JsonObject childAsObject = child.getAsJsonObject();
      visitor.visitArrayMember(parent, childAsObject, isFirst);
      navigate(childAsObject);
    } else { // is a JsonPrimitive
      visitor.visitArrayMember(parent, child.getAsJsonPrimitive(), isFirst);          
    }
  }
}
