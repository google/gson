package com.google.gson.interceptors;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

/**
 * A type adapter factory that implements {@code @Intercept}.
 */
public final class InterceptorFactory implements TypeAdapterFactory {
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
    Intercept intercept = type.getRawType().getAnnotation(Intercept.class);
    if (intercept == null) {
      return null;
    }

    TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
    return new InterceptorAdapter<T>(delegate, intercept);
  }

  static class InterceptorAdapter<T> extends TypeAdapter<T> {
    private final TypeAdapter<T> delegate;
    private final JsonPostDeserializer<T> postDeserializer;

    @SuppressWarnings("unchecked") // ?
    public InterceptorAdapter(TypeAdapter<T> delegate, Intercept intercept) {
      try {
        this.delegate = delegate;
        this.postDeserializer = intercept.postDeserialize().newInstance();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    @Override public void write(JsonWriter out, T value) throws IOException {
      delegate.write(out, value);
    }

    @Override public T read(JsonReader in) throws IOException {
      T result = delegate.read(in);
      postDeserializer.postDeserialize(result);
      return result;
    }
  }
}
