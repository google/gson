package com.google.gson;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ObjectTypeAdapterParameterizedTest {
  @Parameters
  public static Iterable<String> data() {
    return Arrays.asList(
      "[]",
      "{}",
      "null",
      "1.0",
      "true",
      "\"string\"",
      "[true,1.0,null,{},2.0,{\"a\":[false]},[3.0,\"test\"],4.0]",
      "{\"\":1.0,\"a\":true,\"b\":null,\"c\":[],\"d\":{\"a1\":2.0,\"b2\":[true,{\"a3\":3.0}]},\"e\":[{\"f\":4.0},\"test\"]}"
    );
  }

  private final TypeAdapter<Object> adapter = new Gson().getAdapter(Object.class);
  @Parameter
  public String json;

  @Test
  public void testReadWrite() throws IOException {
    Object deserialized = adapter.fromJson(json);
    String actualSerialized = adapter.toJson(deserialized);

    // Serialized Object should be the same as original JSON
    assertEquals(json, actualSerialized);
  }
}
