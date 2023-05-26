package com.example;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

@JsonAdapter(ClassWithAdapter.Adapter.class)
public class ClassWithAdapter {
  static class Adapter extends TypeAdapter<ClassWithAdapter> {
    @Override
    public ClassWithAdapter read(JsonReader in) throws IOException {
      in.beginObject();
      String name = in.nextName();
      if (!name.equals("custom")) {
        throw new IllegalArgumentException("Unexpected name: " + name);
      }
      int i = in.nextInt();
      in.endObject();

      return new ClassWithAdapter(i);
    }

    @Override
    public void write(JsonWriter out, ClassWithAdapter value) throws IOException {
      out.beginObject();
      out.name("custom");
      out.value(value.i);
      out.endObject();
    }
  }

  public Integer i;

  public ClassWithAdapter(int i) {
    this.i = i;
  }

  @Override
  public String toString() {
    return "ClassWithAdapter[" + i + "]";
  }
}
