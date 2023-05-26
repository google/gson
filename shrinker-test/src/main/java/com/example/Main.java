package com.example;

import static com.example.TestExecutor.same;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class Main {
  /**
   * Main entrypoint, called by {@code ShrinkingIT.test()}.
   *
   * <p>To be safe let all tests put their output to the consumer and let integration test verify it;
   * don't perform any relevant assertions in this code because code shrinkers could affect it.
   *
   * @param outputConsumer consumes the test output: {@code name, content} pairs
   */
  public static void runTests(BiConsumer<String, String> outputConsumer) {
    // Create the TypeToken instances on demand because creation of them can fail when
    // generic signatures were erased
    testTypeTokenWriteRead(outputConsumer, "anonymous", () -> new TypeToken<List<ClassWithAdapter>>() {});
    testTypeTokenWriteRead(outputConsumer, "manual", () -> TypeToken.getParameterized(List.class, ClassWithAdapter.class));

    testNamedFields(outputConsumer);
    testSerializedName(outputConsumer);

    testNoJdkUnsafe(outputConsumer);

    testEnum(outputConsumer);
    testEnumSerializedName(outputConsumer);

    testExposeAnnotation(outputConsumer);
    testAnnotations(outputConsumer);
  }

  private static void testTypeTokenWriteRead(BiConsumer<String, String> outputConsumer, String description, Supplier<TypeToken<?>> typeTokenSupplier) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    TestExecutor.run(outputConsumer, "Write: TypeToken " + description,
        () -> gson.toJson(Arrays.asList(new ClassWithAdapter(1)), typeTokenSupplier.get().getType()));
    TestExecutor.run(outputConsumer, "Read: TypeToken " + description, () -> {
      Object deserialized = gson.fromJson("[{\"custom\": 3}]", typeTokenSupplier.get());
      return deserialized.toString();
    });
  }

  /**
   * Calls {@link Gson#toJson}, but (hopefully) in a way which prevents code shrinkers
   * from understanding that reflection is used for {@code obj}.
   */
  private static String toJson(Gson gson, Object obj) {
    return gson.toJson(same(obj));
  }

  /**
   * Calls {@link Gson#fromJson}, but (hopefully) in a way which prevents code shrinkers
   * from understanding that reflection is used for {@code c}.
   */
  private static <T> T fromJson(Gson gson, String json, Class<T> c) {
    return gson.fromJson(json, same(c));
  }

  private static void testNamedFields(BiConsumer<String, String> outputConsumer) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    TestExecutor.run(outputConsumer, "Write: Named fields", () -> toJson(gson, new ClassWithNamedFields(2)));
    TestExecutor.run(outputConsumer, "Read: Named fields", () -> {
      ClassWithNamedFields deserialized = fromJson(gson, "{\"myField\": 3}", ClassWithNamedFields.class);
      return Integer.toString(deserialized.myField);
    });
  }

  private static void testSerializedName(BiConsumer<String, String> outputConsumer) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    TestExecutor.run(outputConsumer, "Write: SerializedName", () -> toJson(gson, new ClassWithSerializedName(2)));
    TestExecutor.run(outputConsumer, "Read: SerializedName", () -> {
      ClassWithSerializedName deserialized = fromJson(gson, "{\"myField\": 3}", ClassWithSerializedName.class);
      return Integer.toString(deserialized.i);
    });
  }

  private static void testNoJdkUnsafe(BiConsumer<String, String> outputConsumer) {
    Gson gson = new GsonBuilder().disableJdkUnsafe().create();
    TestExecutor.run(outputConsumer, "Read: No JDK Unsafe; initial constructor value", () -> {
      ClassWithDefaultConstructor deserialized = fromJson(gson, "{}", ClassWithDefaultConstructor.class);
      return Integer.toString(deserialized.i);
    });
    TestExecutor.run(outputConsumer, "Read: No JDK Unsafe; custom value", () -> {
      ClassWithDefaultConstructor deserialized = fromJson(gson, "{\"myField\": 3}", ClassWithDefaultConstructor.class);
      return Integer.toString(deserialized.i);
    });
  }

  private static void testEnum(BiConsumer<String, String> outputConsumer) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    TestExecutor.run(outputConsumer, "Write: Enum", () -> toJson(gson, EnumClass.FIRST));
    TestExecutor.run(outputConsumer, "Read: Enum", () -> fromJson(gson, "\"SECOND\"", EnumClass.class).toString());
  }

  private static void testEnumSerializedName(BiConsumer<String, String> outputConsumer) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    TestExecutor.run(outputConsumer, "Write: Enum SerializedName",
        () -> toJson(gson, EnumClassWithSerializedName.FIRST));
    TestExecutor.run(outputConsumer, "Read: Enum SerializedName",
        () -> fromJson(gson, "\"two\"", EnumClassWithSerializedName.class).toString());
  }

  private static void testExposeAnnotation(BiConsumer<String, String> outputConsumer) {
    Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    TestExecutor.run(outputConsumer, "Write: @Expose", () -> toJson(gson, new ClassWithExposeAnnotation()));
  }

  private static void testAnnotations(BiConsumer<String, String> outputConsumer) {
    Gson gson = new GsonBuilder().setVersion(1).create();
    TestExecutor.run(outputConsumer, "Write: Annotations", () -> toJson(gson, new ClassWithAnnotations()));
  }
}
