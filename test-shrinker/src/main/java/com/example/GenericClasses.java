package com.example;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

public class GenericClasses {
  private GenericClasses() {}

  static class GenericClass<T> {
    @SerializedName("t")
    T t;

    @Override
    public String toString() {
      return "{t=" + t + "}";
    }
  }

  static class UsingGenericClass {
    @SerializedName("g")
    GenericClass<DummyClass> g;

    @Override
    public String toString() {
      return "{g=" + g + "}";
    }
  }

  static class GenericUsingGenericClass<T> {
    @SerializedName("g")
    GenericClass<T> g;

    @Override
    public String toString() {
      return "{g=" + g + "}";
    }
  }

  @JsonAdapter(DummyClass.Adapter.class)
  static class DummyClass {
    String s;

    DummyClass(String s) {
      this.s = s;
    }

    @Override
    public String toString() {
      return s;
    }

    static class Adapter extends TypeAdapter<DummyClass> {
      @Override
      public DummyClass read(JsonReader in) throws IOException {
        return new DummyClass("read-" + in.nextInt());
      }

      @Override
      public void write(JsonWriter out, DummyClass value) throws IOException {
        throw new UnsupportedOperationException();
      }
    }
  }
}
