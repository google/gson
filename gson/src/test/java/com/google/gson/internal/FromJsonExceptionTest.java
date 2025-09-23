package com.google.gson.internal;

import static org.junit.Assert.fail;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import java.lang.reflect.Type;
import org.junit.Test;

/** Tests wrapping of JsonParseException into JsonSyntaxException (issue #2816). */
public class FromJsonExceptionTest {
  static class User {}

  static class UserBadDeserializer implements JsonDeserializer<User> {
    @Override
    public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) {
      throw new JsonParseException("bad parse");
    }
  }

  @Test
  public void testFromJsonWrapsJsonParseException() {
    Gson gson =
        new GsonBuilder().registerTypeAdapter(User.class, new UserBadDeserializer()).create();
    try {
      gson.fromJson("{}", User.class);
      fail("Expected JsonSyntaxException to be thrown");
    } catch (JsonSyntaxException expected) {
      // success
    }
  }
}
