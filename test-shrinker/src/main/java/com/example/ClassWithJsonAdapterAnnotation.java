package com.example;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Type;

/** Uses {@link JsonAdapter} annotation on fields. */
public class ClassWithJsonAdapterAnnotation {
  // For this field don't use @SerializedName and ignore it for deserialization
  // Has custom ProGuard rule to keep the field name
  @JsonAdapter(value = Adapter.class, nullSafe = false)
  DummyClass f;

  @SerializedName("f1")
  @JsonAdapter(Adapter.class)
  DummyClass f1;

  @SerializedName("f2")
  @JsonAdapter(Factory.class)
  DummyClass f2;

  @SerializedName("f3")
  @JsonAdapter(Serializer.class)
  DummyClass f3;

  @SerializedName("f4")
  @JsonAdapter(Deserializer.class)
  DummyClass f4;

  public ClassWithJsonAdapterAnnotation() {}

  // Note: R8 seems to make this constructor the no-args constructor and initialize fields
  // by default; currently this is not visible in the deserialization test because the JSON data
  // contains values for all fields; but it is noticeable once the JSON data is missing fields
  public ClassWithJsonAdapterAnnotation(int i1, int i2, int i3, int i4) {
    f1 = new DummyClass(Integer.toString(i1));
    f2 = new DummyClass(Integer.toString(i2));
    f3 = new DummyClass(Integer.toString(i3));
    f4 = new DummyClass(Integer.toString(i4));

    // Note: Deliberately don't initialize field `f` here to not refer to it anywhere in code
  }

  @Override
  public String toString() {
    return "ClassWithJsonAdapterAnnotation[f1="
        + f1
        + ", f2="
        + f2
        + ", f3="
        + f3
        + ", f4="
        + f4
        + "]";
  }

  static class Adapter extends TypeAdapter<DummyClass> {
    @Override
    public DummyClass read(JsonReader in) throws IOException {
      return new DummyClass("adapter-" + in.nextInt());
    }

    @Override
    public void write(JsonWriter out, DummyClass value) throws IOException {
      out.value("adapter-" + value);
    }
  }

  static class Factory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      // the code below is not type-safe, but does not matter for this test
      @SuppressWarnings("unchecked")
      TypeAdapter<T> r =
          (TypeAdapter<T>)
              new TypeAdapter<DummyClass>() {
                @Override
                public DummyClass read(JsonReader in) throws IOException {
                  return new DummyClass("factory-" + in.nextInt());
                }

                @Override
                public void write(JsonWriter out, DummyClass value) throws IOException {
                  out.value("factory-" + value.s);
                }
              };

      return r;
    }
  }

  static class Serializer implements JsonSerializer<DummyClass> {
    @Override
    public JsonElement serialize(DummyClass src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive("serializer-" + src.s);
    }
  }

  static class Deserializer implements JsonDeserializer<DummyClass> {
    @Override
    public DummyClass deserialize(
        JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      return new DummyClass("deserializer-" + json.getAsInt());
    }
  }

  // Use this separate class mainly to work around incorrect delegation behavior for JsonSerializer
  // and JsonDeserializer used with @JsonAdapter, see https://github.com/google/gson/issues/1783
  static class DummyClass {
    @SerializedName("s")
    String s;

    DummyClass(String s) {
      this.s = s;
    }

    @Override
    public String toString() {
      return s;
    }
  }
}
