package com.google.gson.internal;

import com.google.gson.stream.JsonWriter;

/**
 * Internal-only APIs of JsonWriter available only to other classes in Gson.
 */
public abstract class JsonWriterInternalAccess {
  public static JsonWriterInternalAccess INSTANCE;

  /**
   * Enables / disables {@linkplain JsonWriter#forceNullValue() forced null serialization}
   * of the next value even if the caller did not explicitly request it.
   * Is disabled again once any value (regardless of whether it is {@code null}) has
   * been written.
   *
   * @param writer which should be changed.
   * @param forceSerialize whether the next {@code null} should be forced.
   */
  public abstract void forceSerializeNextNull(JsonWriter writer, boolean forceSerialize);
}
