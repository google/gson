package com.google.gson.internal;

import com.google.gson.stream.JsonWriter;

/**
 * Internal-only APIs of JsonWriter available only to other classes in Gson.
 */
public abstract class JsonWriterInternalAccess {
  public static JsonWriterInternalAccess INSTANCE;

  /**
   * Overwrites {@link JsonWriter#getSerializeNulls()} for the next value. Has no
   * effect on {@link JsonWriter#forceNullValue()}.
   * Is disabled again once any value (regardless of whether it is {@code null}) has
   * been written.
   *
   * @param writer which should be changed.
   * @param serializeNull whether the next {@code null} should be serialized;
   *   {@code null} to disable overwrite again.
   */
  public abstract void setSerializeNextNullOverwrite(JsonWriter writer, Boolean serializeNull);
}
