package com.google.gson.interceptors;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A type adapter factory that allows to do post deserialization processing of newly created object instances. This
 * class realises two strategies:
 * <ul>
 *   <li>For situation in which adding annotation for classes it's not acceptable: Uses pre configured map of type to
 *   post-deserialiser binding. For such cases use {@link #registerInterception(Class, Class)}</li>
 *   <li>If adding annotation is desired approach, classes which should be postprocessed should be marked with
 *   {@link Intercept} annotation. </li>
 * </ul>
 *
 * For usage with annotation please refer to documentation of {@link Intercept}, for usage of pre configured strategy:
 * <p><pre>
 * public class User {
 *   String name;
 *   String password;
 *   String emailAddress;
 * }
 *
 * public class UserValidator implements JsonPostDeserializer&lt;User&gt; {
 *   public void postDeserialize(User user) {
 *     // Do some checks on user
 *     if (user.name == null || user.password == null) {
 *       throw new JsonParseException("name and password are required fields.");
 *     }
 *     if (user.emailAddress == null) {
 *       emailAddress = "unknown"; // assign a default value.
 *     }
 *   }
 * }
 *
 * InterceptorFactory factory = new InterceptorFactory();
 * factory.registerInterception(User.class, UserValidator.class);
 * </pre></p>
 *
 * @author Inderjeet Singh
 * @author Bartłomiej Żarnowski
 **/
public final class InterceptorFactory implements TypeAdapterFactory {

  private Map<Class<?>, Class<? extends JsonPostDeserializer>> interceptors;

  public void registerInterception(Class<?> type, Class<? extends JsonPostDeserializer> interceptor) {
    if (interceptors == null) {
      interceptors = new HashMap<Class<?>, Class<? extends JsonPostDeserializer>>();
    }
    if (interceptors.containsKey(type)) {
      throw new IllegalArgumentException("Type " + type + " is already registered for interception.");
    }
    interceptors.put(type, interceptor);
  }

  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
    JsonPostDeserializer<T> postDeserializer = null;

    try {
      //check pre configured interceptors it any
      if (interceptors != null) {
        Class<? extends JsonPostDeserializer> interceptorClass = interceptors.get(type.getRawType());
        if (interceptorClass != null) {
          postDeserializer = interceptorClass.newInstance();
        }
      }

      //if no pre configured interceptor, try annotation approach
      if (postDeserializer == null) {
        Intercept intercept = type.getRawType().getAnnotation(Intercept.class);
        if (intercept == null) {
          return null;
        }
        postDeserializer = intercept.postDeserialize().newInstance();
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
    return new InterceptorAdapter<T>(delegate, postDeserializer);
  }

  static class InterceptorAdapter<T> extends TypeAdapter<T> {
    private final TypeAdapter<T> delegate;
    private final JsonPostDeserializer<T> postDeserializer;

    @SuppressWarnings("unchecked") // ?
    public InterceptorAdapter(TypeAdapter<T> delegate, JsonPostDeserializer<T> postDeserializer) {
      this.delegate = delegate;
      this.postDeserializer = postDeserializer;
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