package com.google.gson;

/**
 * A class representing a Json null value.
 * 
 * @author Inderjeet Singh
 */
public final class JsonNull extends JsonElement {

  @Override
  protected void toString(StringBuilder sb) {
    sb.append("null");
  }
}
