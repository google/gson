package com.google.gson.internal.bind;

import com.google.gson.TypeAdapter;

/**
 * Type adapter which might delegate serialization to another adapter.
 */
public abstract class SerializationDelegatingTypeAdapter<T> extends TypeAdapter<T> {
  /**
   * Returns the adapter used for serialization, might be {@code this} or another adapter.
   * That other adapter might itself also be a {@code SerializationDelegatingTypeAdapter}.
   */
  public abstract TypeAdapter<T> getSerializationDelegate();
}
