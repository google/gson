package com.google.gson;

/**
 * Definition of a visitor for a JsonElement tree
 * 
 * @author Inderjeet Singh
 */
interface JsonElementVisitor {
  void visitPrimitive(JsonPrimitive primitive);
  
  void startArray(JsonArray array);
  void visitArrayMember(JsonArray parent, JsonPrimitive member, boolean isFirst);
  void visitArrayMember(JsonArray parent, JsonArray member, boolean isFirst);
  void visitArrayMember(JsonArray parent, JsonObject member, boolean isFirst);
  void endArray(JsonArray array);
  
  void startObject(JsonObject object);
  void visitObjectMember(JsonObject parent, String memberName, JsonPrimitive member, 
      boolean isFirst);
  void visitObjectMember(JsonObject parent, String memberName, JsonArray member, boolean isFirst);
  void visitObjectMember(JsonObject parent, String memberName, JsonObject member, boolean isFirst);
  void endObject(JsonObject object);
}