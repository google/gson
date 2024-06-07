package com.example;

import static com.example.TestExecutor.same;

import com.example.GenericClasses.DummyClass;
import com.example.GenericClasses.GenericClass;
import com.example.GenericClasses.GenericUsingGenericClass;
import com.example.GenericClasses.UsingGenericClass;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class Main {
  private Main() {}

  /**
   * Main entrypoint, called by {@code ShrinkingIT.test()}.
   *
   * <p>To be safe let all tests put their output to the consumer and let integration test verify
   * it; don't perform any relevant assertions in this code because code shrinkers could affect it.
   *
   * @param outputConsumer consumes the test output: {@code name, content} pairs
   */
  public static void runTests(BiConsumer<String, String> outputConsumer) {
    // Create the TypeToken instances on demand because creation of them can fail when
    // generic signatures were erased
    testTypeTokenWriteRead(
        outputConsumer, "anonymous", () -> new TypeToken<List<ClassWithAdapter>>() {});
    testTypeTokenWriteRead(
        outputConsumer,
        "manual",
        () -> TypeToken.getParameterized(List.class, ClassWithAdapter.class));

    testNamedFields(outputConsumer);
    testSerializedName(outputConsumer);

    testConstructorNoArgs(outputConsumer);
    testConstructorHasArgs(outputConsumer);
    testUnreferencedConstructorNoArgs(outputConsumer);
    testUnreferencedConstructorHasArgs(outputConsumer);

    testNoJdkUnsafe(outputConsumer);

    testEnum(outputConsumer);
    testEnumSerializedName(outputConsumer);

    testExposeAnnotation(outputConsumer);
    testVersionAnnotations(outputConsumer);
    testJsonAdapterAnnotation(outputConsumer);

    testGenericClasses(outputConsumer);
  }

  private static void testTypeTokenWriteRead(
      BiConsumer<String, String> outputConsumer,
      String description,
      Supplier<TypeToken<?>> typeTokenSupplier) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    TestExecutor.run(
        outputConsumer,
        "Write: TypeToken " + description,
        () ->
            gson.toJson(Arrays.asList(new ClassWithAdapter(1)), typeTokenSupplier.get().getType()));
    TestExecutor.run(
        outputConsumer,
        "Read: TypeToken " + description,
        () -> {
          Object deserialized = gson.fromJson("[{\"custom\": 3}]", typeTokenSupplier.get());
          return deserialized.toString();
        });
  }

  /**
   * Calls {@link Gson#toJson}, but (hopefully) in a way which prevents code shrinkers from
   * understanding that reflection is used for {@code obj}.
   */
  private static String toJson(Gson gson, Object obj) {
    return gson.toJson(same(obj));
  }

  /**
   * Calls {@link Gson#fromJson}, but (hopefully) in a way which prevents code shrinkers from
   * understanding that reflection is used for {@code c}.
   */
  private static <T> T fromJson(Gson gson, String json, Class<T> c) {
    return gson.fromJson(json, same(c));
  }

  private static void testNamedFields(BiConsumer<String, String> outputConsumer) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    TestExecutor.run(
        outputConsumer, "Write: Named fields", () -> toJson(gson, new ClassWithNamedFields(2)));
    TestExecutor.run(
        outputConsumer,
        "Read: Named fields",
        () -> {
          ClassWithNamedFields deserialized =
              fromJson(gson, "{\"myField\": 3}", ClassWithNamedFields.class);
          return Integer.toString(deserialized.myField);
        });
  }

  private static void testSerializedName(BiConsumer<String, String> outputConsumer) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    TestExecutor.run(
        outputConsumer,
        "Write: SerializedName",
        () -> toJson(gson, new ClassWithSerializedName(2)));
    TestExecutor.run(
        outputConsumer,
        "Read: SerializedName",
        () -> {
          ClassWithSerializedName deserialized =
              fromJson(gson, "{\"myField\": 3}", ClassWithSerializedName.class);
          return Integer.toString(deserialized.i);
        });
  }

  private static void testConstructorNoArgs(BiConsumer<String, String> outputConsumer) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    TestExecutor.run(
        outputConsumer,
        "Write: No args constructor",
        () -> toJson(gson, new ClassWithNoArgsConstructor()));
    TestExecutor.run(
        outputConsumer,
        "Read: No args constructor; initial constructor value",
        () -> {
          ClassWithNoArgsConstructor deserialized =
              fromJson(gson, "{}", ClassWithNoArgsConstructor.class);
          return Integer.toString(deserialized.i);
        });
    TestExecutor.run(
        outputConsumer,
        "Read: No args constructor; custom value",
        () -> {
          ClassWithNoArgsConstructor deserialized =
              fromJson(gson, "{\"myField\": 3}", ClassWithNoArgsConstructor.class);
          return Integer.toString(deserialized.i);
        });
  }

  private static void testConstructorHasArgs(BiConsumer<String, String> outputConsumer) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    TestExecutor.run(
        outputConsumer,
        "Write: Constructor with args",
        () -> toJson(gson, new ClassWithHasArgsConstructor(2)));
    // This most likely relies on JDK Unsafe (unless the shrinker rewrites the constructor in some
    // way)
    TestExecutor.run(
        outputConsumer,
        "Read: Constructor with args",
        () -> {
          ClassWithHasArgsConstructor deserialized =
              fromJson(gson, "{\"myField\": 3}", ClassWithHasArgsConstructor.class);
          return Integer.toString(deserialized.i);
        });
  }

  private static void testUnreferencedConstructorNoArgs(BiConsumer<String, String> outputConsumer) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    // No write because we're not referencing this class's constructor.

    // This runs the no-args constructor.
    TestExecutor.run(
        outputConsumer,
        "Read: Unreferenced no args constructor; initial constructor value",
        () -> {
          ClassWithUnreferencedNoArgsConstructor deserialized =
              fromJson(gson, "{}", ClassWithUnreferencedNoArgsConstructor.class);
          return Integer.toString(deserialized.i);
        });
    TestExecutor.run(
        outputConsumer,
        "Read: Unreferenced no args constructor; custom value",
        () -> {
          ClassWithUnreferencedNoArgsConstructor deserialized =
              fromJson(gson, "{\"myField\": 3}", ClassWithUnreferencedNoArgsConstructor.class);
          return Integer.toString(deserialized.i);
        });
  }

  private static void testUnreferencedConstructorHasArgs(
      BiConsumer<String, String> outputConsumer) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    // No write because we're not referencing this class's constructor.

    // This most likely relies on JDK Unsafe (unless the shrinker rewrites the constructor in some
    // way)
    TestExecutor.run(
        outputConsumer,
        "Read: Unreferenced constructor with args",
        () -> {
          ClassWithUnreferencedHasArgsConstructor deserialized =
              fromJson(gson, "{\"myField\": 3}", ClassWithUnreferencedHasArgsConstructor.class);
          return Integer.toString(deserialized.i);
        });
  }

  private static void testNoJdkUnsafe(BiConsumer<String, String> outputConsumer) {
    Gson gson = new GsonBuilder().disableJdkUnsafe().create();
    TestExecutor.run(
        outputConsumer,
        "Read: No JDK Unsafe; initial constructor value",
        () -> {
          ClassWithNoArgsConstructor deserialized =
              fromJson(gson, "{}", ClassWithNoArgsConstructor.class);
          return Integer.toString(deserialized.i);
        });
    TestExecutor.run(
        outputConsumer,
        "Read: No JDK Unsafe; custom value",
        () -> {
          ClassWithNoArgsConstructor deserialized =
              fromJson(gson, "{\"myField\": 3}", ClassWithNoArgsConstructor.class);
          return Integer.toString(deserialized.i);
        });
  }

  private static void testEnum(BiConsumer<String, String> outputConsumer) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    TestExecutor.run(outputConsumer, "Write: Enum", () -> toJson(gson, EnumClass.FIRST));
    TestExecutor.run(
        outputConsumer,
        "Read: Enum",
        () -> fromJson(gson, "\"SECOND\"", EnumClass.class).toString());
  }

  private static void testEnumSerializedName(BiConsumer<String, String> outputConsumer) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    TestExecutor.run(
        outputConsumer,
        "Write: Enum SerializedName",
        () -> toJson(gson, EnumClassWithSerializedName.FIRST));
    TestExecutor.run(
        outputConsumer,
        "Read: Enum SerializedName",
        () -> fromJson(gson, "\"two\"", EnumClassWithSerializedName.class).toString());
  }

  private static void testExposeAnnotation(BiConsumer<String, String> outputConsumer) {
    Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    TestExecutor.run(
        outputConsumer, "Write: @Expose", () -> toJson(gson, new ClassWithExposeAnnotation()));
  }

  private static void testVersionAnnotations(BiConsumer<String, String> outputConsumer) {
    Gson gson = new GsonBuilder().setVersion(1).create();
    TestExecutor.run(
        outputConsumer,
        "Write: Version annotations",
        () -> toJson(gson, new ClassWithVersionAnnotations()));
  }

  private static void testJsonAdapterAnnotation(BiConsumer<String, String> outputConsumer) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    TestExecutor.run(
        outputConsumer,
        "Write: JsonAdapter on fields",
        () -> toJson(gson, new ClassWithJsonAdapterAnnotation(1, 2, 3, 4)));

    String json = "{\"f1\": 1, \"f2\": 2, \"f3\": {\"s\": \"3\"}, \"f4\": 4}";
    TestExecutor.run(
        outputConsumer,
        "Read: JsonAdapter on fields",
        () -> fromJson(gson, json, ClassWithJsonAdapterAnnotation.class).toString());
  }

  private static void testGenericClasses(BiConsumer<String, String> outputConsumer) {
    Gson gson = new Gson();
    TestExecutor.run(
        outputConsumer,
        "Read: Generic TypeToken",
        () -> gson.fromJson("{\"t\": 1}", new TypeToken<GenericClass<DummyClass>>() {}).toString());
    TestExecutor.run(
        outputConsumer,
        "Read: Using Generic",
        () -> fromJson(gson, "{\"g\": {\"t\": 1}}", UsingGenericClass.class).toString());
    TestExecutor.run(
        outputConsumer,
        "Read: Using Generic TypeToken",
        () ->
            gson.fromJson(
                    "{\"g\": {\"t\": 1}}", new TypeToken<GenericUsingGenericClass<DummyClass>>() {})
                .toString());
  }
}
