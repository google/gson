import com.google.gson.*;
import java.lang.reflect.Type;

public class Repro {
  static class User {}

  static class UserBadDeserializer implements JsonDeserializer<User> {
    @Override
    public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) {
      // Simulate user code throwing JsonParseException
      throw new JsonParseException("bad parse");
    }
  }

  public static void main(String[] args) {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(User.class, new UserBadDeserializer())
        .create();
    try {
      gson.fromJson("{}", User.class);
      System.out.println("Should not reach here");
    } catch (JsonSyntaxException e) {
      System.out.println("Caught JsonSyntaxException: " + e.getMessage());
    }
  }
}
