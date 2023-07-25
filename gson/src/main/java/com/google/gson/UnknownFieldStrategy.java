package com.google.gson;

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import java.io.IOException;

/**
 * A strategy defining how to handle unknown fields during reflection-based deserialization.
 *
 * @see GsonBuilder#setUnknownFieldStrategy(UnknownFieldStrategy)
 * @since $next-version$
 */
public interface UnknownFieldStrategy {
  /**
   * This strategy ignores the unknown field.
   *
   * <p>This is the default unknown field strategy.
   */
  UnknownFieldStrategy IGNORE = new UnknownFieldStrategy() {
    @Override
    public void handleUnknownField(TypeToken<?> declaringType, Object instance, String fieldName,
        JsonReader jsonReader, Gson gson) throws IOException {
      jsonReader.skipValue();
    }

    @Override
    public String toString() {
      return "UnknownFieldStrategy.IGNORE";
    }
  };

  /**
   * This strategy throws an exception when an unknown field is encountered.
   *
   * <p><b>Note:</b> Be careful when using this strategy; while it might sound tempting
   * to strictly validate that the JSON data matches the expected format, this strategy
   * makes it difficult to add new fields to the JSON structure in a backward compatible way.
   * Usually it suffices to use only {@link MissingFieldValueStrategy#THROW_EXCEPTION} for
   * validation and to ignore unknown fields.
   */
  UnknownFieldStrategy THROW_EXCEPTION = new UnknownFieldStrategy() {
    @Override
    public void handleUnknownField(TypeToken<?> declaringType, Object instance, String fieldName,
        JsonReader jsonReader, Gson gson) throws IOException {
      // TODO: Proper exception
      throw new RuntimeException("Unknown field '" + fieldName + "' for " + declaringType.getRawType() + " at path " + jsonReader.getPath());
    }

    @Override
    public String toString() {
      return "UnknownFieldStrategy.THROW_EXCEPTION";
    }
  };

  /**
   * Called when an unknown field is encountered. Implementations can throw an exception,
   * store the field value in {@code instance} or ignore the unknown field.
   *
   * <p>The {@code jsonReader} is positioned to read the value of the unknown field. If an
   * implementation of this method does not throw an exception it must consume the value, either
   * by reading it with methods like {@link JsonReader#nextString()} (possibly after peeking
   * at the value type first), or by skipping it with {@link JsonReader#skipValue()}.<br>
   * The {@code gson} object can be used to read from the {@code jsonReader}. It is the same
   * instance which was originally used to perform the deserialization.
   *
   * <p>The {@code instance} represents an instance of the declaring type with the so far already
   * deserialized fields. It can be used to store the value of the unknown field, for example
   * if it declares a {@code transient Map<String, Object>} field for all unknown values.<br>
   * For Record classes (Java 16 feature) the {@code instance} is {@code null}.
   *
   * @param declaringType type declaring the field
   * @param instance instance of the declaring type, {@code null} for Record classes
   * @param fieldName name of the unknown field
   * @param jsonReader reader to be used to read or skip the field value
   * @param gson {@code Gson} instance which can be used to read the field value from {@code jsonReader}
   * @throws IOException if reading or skipping the field value fails
   */
  void handleUnknownField(TypeToken<?> declaringType, Object instance, String fieldName, JsonReader jsonReader, Gson gson) throws IOException;
}
