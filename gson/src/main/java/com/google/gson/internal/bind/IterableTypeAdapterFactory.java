package com.google.gson.internal.bind;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.$Gson$Types;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public final class IterableTypeAdapterFactory implements TypeAdapterFactory {
  public static final IterableTypeAdapterFactory INSTANCE = new IterableTypeAdapterFactory();

  private IterableTypeAdapterFactory() {
  }

  @Override
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
    Type type = typeToken.getType();

    Class<? super T> rawType = typeToken.getRawType();
    /*
     * Only support Iterable, but not subtypes
     * This allows freely choosing Iterable implementation on deserialization
     */
    if (rawType != Iterable.class) {
      return null;
    }

    Type elementType = $Gson$Types.getIterableElementType(type, rawType);
    TypeAdapter<?> elementTypeAdapter = gson.getAdapter(TypeToken.get(elementType));

    @SuppressWarnings({"unchecked", "rawtypes"}) // create() doesn't define a type parameter
    TypeAdapter<T> result = new Adapter(gson, elementType, elementTypeAdapter);
    return result;
  }

  private static final class Adapter<E> extends TypeAdapter<Iterable<E>> {
    private final TypeAdapter<E> elementTypeAdapter;

    public Adapter(Gson context, Type elementType, TypeAdapter<E> elementTypeAdapter) {
      this.elementTypeAdapter =
          new TypeAdapterRuntimeTypeWrapper<E>(context, elementTypeAdapter, elementType);
    }

    @Override public Iterable<E> read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }

      List<E> list = new ArrayList<E>();
      in.beginArray();
      while (in.hasNext()) {
        E instance = elementTypeAdapter.read(in);
        list.add(instance);
      }
      in.endArray();
      return list;
    }

    @Override public void write(JsonWriter out, Iterable<E> iterable) throws IOException {
      if (iterable == null) {
        out.nullValue();
        return;
      }

      out.beginArray();
      for (E element : iterable) {
        elementTypeAdapter.write(out, element);
      }
      out.endArray();
    }
  }
}
