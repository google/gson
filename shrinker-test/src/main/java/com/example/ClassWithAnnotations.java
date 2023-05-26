package com.example;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.Since;
import com.google.gson.annotations.Until;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

/**
 * Uses various Gson annotations.
 *
 * <p>{@code @Expose} is covered by {@link ClassWithExposeAnnotation},
 * {@code @SerializedName} is covered by {@link ClassWithSerializedName}.
 */
public class ClassWithAnnotations {
  @JsonAdapter(ClassWithAnnotations.DummyAdapter.class)
  int i1;

  @Since(1)
  int i2;

  @Until(1) // will be ignored with GsonBuilder.setVersion(1)
  int i3;

  @Since(2) // will be ignored with GsonBuilder.setVersion(1)
  int i4;

  @Until(2)
  int i5;

  static class DummyAdapter extends TypeAdapter<Object> {
    @Override
    public Object read(JsonReader in) throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void write(JsonWriter out, Object value) throws IOException {
      out.value("custom");
    }
  }
}
