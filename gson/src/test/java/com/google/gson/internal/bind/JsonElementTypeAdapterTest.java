package com.google.gson.internal.bind;

import static com.google.common.truth.Truth.assertThat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.Strictness;
import com.google.gson.internal.LazilyParsedNumber;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import org.junit.Test;

/**
 * Specific tests for {@link JsonElementTypeAdapter}; more general tests are currently in {@link
 * com.google.gson.functional.DefaultTypeAdaptersTest}.
 */
public class JsonElementTypeAdapterTest {
  /**
   * When deserializing from a {@link JsonTreeReader} the objects it stores should be reused for
   * better performance.
   */
  @Test
  public void reusesObjectsFromTreeReader() throws IOException {
    JsonElement jsonElement = new JsonPrimitive("test");
    JsonReader jsonReader = new JsonTreeReader(jsonElement);
    assertThat(JsonElementTypeAdapter.ADAPTER.read(jsonReader)).isSameInstanceAs(jsonElement);

    // Also applies to mutable `JsonArray` and `JsonObject` (might not be desirable in all
    // situations?)
    jsonElement = new JsonArray();
    jsonReader = new JsonTreeReader(jsonElement);
    assertThat(JsonElementTypeAdapter.ADAPTER.read(jsonReader)).isSameInstanceAs(jsonElement);

    jsonElement = new JsonObject();
    jsonReader = new JsonTreeReader(jsonElement);
    assertThat(JsonElementTypeAdapter.ADAPTER.read(jsonReader)).isSameInstanceAs(jsonElement);
  }

  private static Number readNumber(JsonReader jsonReader) throws IOException {
    JsonElement jsonElement = JsonElementTypeAdapter.ADAPTER.read(jsonReader);
    assertThat(jsonElement).isInstanceOf(JsonPrimitive.class);
    JsonPrimitive jsonPrimitive = (JsonPrimitive) jsonElement;
    assertThat(jsonPrimitive.isNumber()).isTrue();
    return jsonPrimitive.getAsNumber();
  }

  private static Number readNumber(String json) throws IOException {
    return readNumber(new JsonReader(new StringReader(json)));
  }

  @Test
  public void numbers() throws IOException {
    Number number = readNumber("10");
    assertThat(number).isEqualTo(10);
    assertThat(number).isInstanceOf(Integer.class);

    number = readNumber(Integer.toString(Integer.MIN_VALUE));
    assertThat(number).isEqualTo(Integer.MIN_VALUE);
    assertThat(number).isInstanceOf(Integer.class);

    number = readNumber(Integer.toString(Integer.MAX_VALUE));
    assertThat(number).isEqualTo(Integer.MAX_VALUE);
    assertThat(number).isInstanceOf(Integer.class);

    // Integer.MIN_VALUE - 1
    number = readNumber("-2147483649");
    assertThat(number).isEqualTo(-2147483649L);
    assertThat(number).isInstanceOf(Long.class);

    // Integer.MAX_VALUE + 1
    number = readNumber("2147483648");
    assertThat(number).isEqualTo(2147483648L);
    assertThat(number).isInstanceOf(Long.class);

    number = readNumber(Long.toString(Long.MIN_VALUE));
    assertThat(number).isEqualTo(Long.MIN_VALUE);
    assertThat(number).isInstanceOf(Long.class);

    number = readNumber(Long.toString(Long.MAX_VALUE));
    assertThat(number).isEqualTo(Long.MAX_VALUE);
    assertThat(number).isInstanceOf(Long.class);

    // Long.MIN_VALUE - 1
    number = readNumber("-9223372036854775809");
    assertThat(number).isEqualTo(new LazilyParsedNumber("-9223372036854775809"));
    LazilyParsedNumber lazyNumber = (LazilyParsedNumber) number;
    assertThat(lazyNumber.isGuaranteedValidJsonNumber()).isTrue();

    // Long.MAX_VALUE + 1
    number = readNumber("9223372036854775808");
    assertThat(number).isEqualTo(new LazilyParsedNumber("9223372036854775808"));
    lazyNumber = (LazilyParsedNumber) number;
    assertThat(lazyNumber.isGuaranteedValidJsonNumber()).isTrue();

    number = readNumber("10.0");
    assertThat(number).isEqualTo(new LazilyParsedNumber("10.0"));
    lazyNumber = (LazilyParsedNumber) number;
    assertThat(lazyNumber.isGuaranteedValidJsonNumber()).isTrue();
  }

  /**
   * Numbers from custom {@link JsonReader} subclass should not be trusted to be valid JSON numbers.
   */
  @Test
  public void numberCustomReader() throws IOException {
    class CustomJsonReader extends JsonReader {
      CustomJsonReader(Reader in) {
        super(in);
      }
    }

    JsonReader jsonReader = new CustomJsonReader(new StringReader("10"));
    Number number = readNumber(jsonReader);
    assertThat(number).isEqualTo(new LazilyParsedNumber("10"));
    LazilyParsedNumber lazyNumber = (LazilyParsedNumber) number;
    assertThat(lazyNumber.isGuaranteedValidJsonNumber()).isFalse();
  }

  /**
   * Checks handling of large JSON numbers.
   *
   * <p><b>Important:</b> This mainly ensures that {@link JsonReader#peek()} only returns {@link
   * JsonToken#NUMBER} when it has actually peeked at the full number. Currently the code assumes
   * that {@code NUMBER} guarantees that the number read using {@link JsonReader#nextString()} is a
   * valid JSON number.
   */
  @Test
  public void largeNumber() throws IOException {
    String numberJson = "1".repeat(10000);
    JsonReader jsonReader = new JsonReader(new StringReader(numberJson));
    // Make lenient because JsonReader currently reads large number as unquoted string
    jsonReader.setStrictness(Strictness.LENIENT);
    // Note: In case future JsonReader refactoring changes the behavior (e.g. supporting arbitrary
    // length JSON numbers), have to verify that `peek() == NUMBER` still guarantees the number is
    // fully valid; otherwise need to change `JsonElementTypeAdapter` implementation
    JsonPrimitive jsonPrimitive =
        JsonElementTypeAdapter.ADAPTER.read(jsonReader).getAsJsonPrimitive();
    // Large number is currently read as (unquoted) string; if that changes can use `readNumber`
    // here instead
    assertThat(jsonPrimitive.isString()).isTrue();
    assertThat(jsonPrimitive.getAsString()).isEqualTo(numberJson);

    String invalidNumberJson = numberJson + "a";
    jsonReader = new JsonReader(new StringReader(invalidNumberJson));
    jsonReader.setStrictness(Strictness.LENIENT);
    jsonPrimitive = JsonElementTypeAdapter.ADAPTER.read(jsonReader).getAsJsonPrimitive();
    assertThat(jsonPrimitive.isString()).isTrue();
    assertThat(jsonPrimitive.getAsNumber().toString()).isEqualTo(invalidNumberJson);
  }
}
