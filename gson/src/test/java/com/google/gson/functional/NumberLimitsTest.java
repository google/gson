package com.google.gson.functional;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.ToNumberPolicy;
import com.google.gson.ToNumberStrategy;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.LazilyParsedNumber;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.MalformedJsonException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.Test;

public class NumberLimitsTest {
  private static final int MAX_LENGTH = 10_000;

  private static JsonReader jsonReader(String json) {
    return new JsonReader(new StringReader(json));
  }

  @Test
  public void testJsonReader() throws IOException {
    JsonReader reader = jsonReader("1".repeat(1000));
    assertThat(reader.peek()).isEqualTo(JsonToken.NUMBER);
    assertThat(reader.nextString()).isEqualTo("1".repeat(1000));

    JsonReader reader2 = jsonReader("1".repeat(MAX_LENGTH + 1));
    // Currently JsonReader does not recognize large JSON numbers as numbers but treats them
    // as unquoted string
    MalformedJsonException e = assertThrows(MalformedJsonException.class, () -> reader2.peek());
    assertThat(e).hasMessageThat().startsWith("Use JsonReader.setStrictness(Strictness.LENIENT) to accept malformed JSON");

    reader = jsonReader("1e9999");
    assertThat(reader.peek()).isEqualTo(JsonToken.NUMBER);
    assertThat(reader.nextString()).isEqualTo("1e9999");

    reader = jsonReader("1e+9999");
    assertThat(reader.peek()).isEqualTo(JsonToken.NUMBER);
    assertThat(reader.nextString()).isEqualTo("1e+9999");

    JsonReader reader3 = jsonReader("1e10000");
    e = assertThrows(MalformedJsonException.class, () -> reader3.peek());
    assertThat(e).hasMessageThat().isEqualTo("Too many number exponent digits at line 1 column 1 path $");

    JsonReader reader4 = jsonReader("1e00001");
    // Currently JsonReader does not ignore leading 0s in exponent
    e = assertThrows(MalformedJsonException.class, () -> reader4.peek());
    assertThat(e).hasMessageThat().isEqualTo("Too many number exponent digits at line 1 column 1 path $");
  }

  @Test
  public void testJsonPrimitive() {
    assertThat(new JsonPrimitive("1".repeat(MAX_LENGTH)).getAsBigDecimal())
        .isEqualTo(new BigDecimal("1".repeat(MAX_LENGTH)));
    assertThat(new JsonPrimitive("1e9999").getAsBigDecimal())
        .isEqualTo(new BigDecimal("1e9999"));
    assertThat(new JsonPrimitive("1e-9999").getAsBigDecimal())
        .isEqualTo(new BigDecimal("1e-9999"));

    NumberFormatException e = assertThrows(NumberFormatException.class,
        () -> new JsonPrimitive("1".repeat(MAX_LENGTH + 1)).getAsBigDecimal());
    assertThat(e).hasMessageThat().isEqualTo("Number string too large: 111111111111111111111111111111...");

    e = assertThrows(NumberFormatException.class,
        () -> new JsonPrimitive("1e10000").getAsBigDecimal());
    assertThat(e).hasMessageThat().isEqualTo("Number has unsupported scale: 1e10000");

    e = assertThrows(NumberFormatException.class,
        () -> new JsonPrimitive("1e-10000").getAsBigDecimal());
    assertThat(e).hasMessageThat().isEqualTo("Number has unsupported scale: 1e-10000");


    assertThat(new JsonPrimitive("1".repeat(MAX_LENGTH)).getAsBigInteger())
        .isEqualTo(new BigInteger("1".repeat(MAX_LENGTH)));

    e = assertThrows(NumberFormatException.class,
        () -> new JsonPrimitive("1".repeat(MAX_LENGTH + 1)).getAsBigInteger());
    assertThat(e).hasMessageThat().isEqualTo("Number string too large: 111111111111111111111111111111...");
  }

  @Test
  public void testToNumberPolicy() throws IOException {
    ToNumberStrategy strategy = ToNumberPolicy.BIG_DECIMAL;

    assertThat(strategy.readNumber(jsonReader("\"" + "1".repeat(MAX_LENGTH) + "\"")))
        .isEqualTo(new BigDecimal("1".repeat(MAX_LENGTH)));
    assertThat(strategy.readNumber(jsonReader("1e9999")))
        .isEqualTo(new BigDecimal("1e9999"));


    JsonParseException e = assertThrows(JsonParseException.class,
        () -> strategy.readNumber(jsonReader("\"" + "1".repeat(MAX_LENGTH + 1) + "\"")));
    assertThat(e).hasMessageThat().isEqualTo("Cannot parse " + "1".repeat(MAX_LENGTH + 1) + "; at path $");
    assertThat(e).hasCauseThat().hasMessageThat().isEqualTo("Number string too large: 111111111111111111111111111111...");

    e = assertThrows(JsonParseException.class, () -> strategy.readNumber(jsonReader("\"1e10000\"")));
    assertThat(e).hasMessageThat().isEqualTo("Cannot parse 1e10000; at path $");
    assertThat(e).hasCauseThat().hasMessageThat().isEqualTo("Number has unsupported scale: 1e10000");
  }

  @Test
  public void testLazilyParsedNumber() throws IOException {
    assertThat(new LazilyParsedNumber("1".repeat(MAX_LENGTH)).intValue())
        .isEqualTo(new BigDecimal("1".repeat(MAX_LENGTH)).intValue());
    assertThat(new LazilyParsedNumber("1e9999").intValue())
        .isEqualTo(new BigDecimal("1e9999").intValue());

    NumberFormatException e = assertThrows(NumberFormatException.class,
        () -> new LazilyParsedNumber("1".repeat(MAX_LENGTH + 1)).intValue());
    assertThat(e).hasMessageThat().isEqualTo("Number string too large: 111111111111111111111111111111...");

    e = assertThrows(NumberFormatException.class,
        () -> new LazilyParsedNumber("1e10000").intValue());
    assertThat(e).hasMessageThat().isEqualTo("Number has unsupported scale: 1e10000");

    e = assertThrows(NumberFormatException.class,
        () -> new LazilyParsedNumber("1e10000").longValue());
    assertThat(e).hasMessageThat().isEqualTo("Number has unsupported scale: 1e10000");

    ObjectOutputStream objOut = new ObjectOutputStream(OutputStream.nullOutputStream());
    // Number is serialized as BigDecimal; should also enforce limits during this conversion
    e = assertThrows(NumberFormatException.class, () -> objOut.writeObject(new LazilyParsedNumber("1e10000")));
    assertThat(e).hasMessageThat().isEqualTo("Number has unsupported scale: 1e10000");
  }

  @Test
  public void testBigDecimalAdapter() throws IOException {
    TypeAdapter<BigDecimal> adapter = new Gson().getAdapter(BigDecimal.class);

    assertThat(adapter.fromJson("\"" + "1".repeat(MAX_LENGTH) + "\""))
        .isEqualTo(new BigDecimal("1".repeat(MAX_LENGTH)));
    assertThat(adapter.fromJson("\"1e9999\""))
        .isEqualTo(new BigDecimal("1e9999"));

    JsonSyntaxException e = assertThrows(JsonSyntaxException.class,
        () -> adapter.fromJson("\"" + "1".repeat(MAX_LENGTH + 1) + "\""));
    assertThat(e).hasMessageThat().isEqualTo("Failed parsing '" + "1".repeat(MAX_LENGTH + 1) + "' as BigDecimal; at path $");
    assertThat(e).hasCauseThat().hasMessageThat().isEqualTo("Number string too large: 111111111111111111111111111111...");

    e = assertThrows(JsonSyntaxException.class,
        () -> adapter.fromJson("\"1e10000\""));
    assertThat(e).hasMessageThat().isEqualTo("Failed parsing '1e10000' as BigDecimal; at path $");
    assertThat(e).hasCauseThat().hasMessageThat().isEqualTo("Number has unsupported scale: 1e10000");
  }

  @Test
  public void testBigIntegerAdapter() throws IOException {
    TypeAdapter<BigInteger> adapter = new Gson().getAdapter(BigInteger.class);

    assertThat(adapter.fromJson("\"" + "1".repeat(MAX_LENGTH) + "\""))
        .isEqualTo(new BigInteger("1".repeat(MAX_LENGTH)));

    JsonSyntaxException e = assertThrows(JsonSyntaxException.class,
        () -> adapter.fromJson("\"" + "1".repeat(MAX_LENGTH + 1) + "\""));
    assertThat(e).hasMessageThat().isEqualTo("Failed parsing '" + "1".repeat(MAX_LENGTH + 1) + "' as BigInteger; at path $");
    assertThat(e).hasCauseThat().hasMessageThat().isEqualTo("Number string too large: 111111111111111111111111111111...");
  }
}
