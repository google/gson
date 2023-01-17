# Troubleshooting Guide

This guide describes how to troubleshoot common issues when using Gson.

## `ClassCastException` when using deserialized object

**Symptom:** `ClassCastException` is thrown when accessing an object deserialized by Gson

**Reason:** Your code is most likely not type-safe

**Solution:** Make sure your code adheres to the following:

- Avoid raw types: Instead of calling `fromJson(..., List.class)`, create for example a `TypeToken<List<MyClass>>`.
  See the [user guide](UserGuide.md#collections-examples) for more information.
- When using `TypeToken` prefer the `Gson.fromJson` overloads with `TypeToken` parameter such as [`fromJson(Reader, TypeToken)`](https://www.javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/Gson.html#fromJson(java.io.Reader,com.google.gson.reflect.TypeToken)).
  The overloads with `Type` parameter do not provide any type-safety guarantees.
- When using `TypeToken` make sure you don't capture a type variable. For example avoid something like `new TypeToken<List<T>>()` (where `T` is a type variable). Due to Java type erasure the actual type of `T` is not available at runtime. Refactor your code to pass around `TypeToken` instances or use [`TypeToken.getParameterized(...)`](https://www.javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/reflect/TypeToken.html#getParameterized(java.lang.reflect.Type,java.lang.reflect.Type...)), for example `TypeToken.getParameterized(List.class, elementClass)`.

## `InaccessibleObjectException`: 'module ... does not "opens ..." to unnamed module'

**Symptom:** An exception with a message in the form 'module ... does not "opens ..." to unnamed module' is thrown

**Reason:** You use Gson by accident to access internal fields of third-party classes

**Solution:** Write custom Gson [`TypeAdapter`](https://www.javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/TypeAdapter.html) implementations for the affected classes or change the type of your data. If this occurs for a field in one of your classes which you did not actually want to serialize or deserialize in the first place, you can exclude that field, see the [user guide](UserGuide.md#excluding-fields-from-serialization-and-deserialization).

**Explanation:**

When no built-in adapter for a type exists and no custom adapter has been registered, Gson falls back to using reflection to access the fields of a class (including `private` ones). Most likely you are seeing this error because you (by accident) rely on the reflection-based adapter for third-party classes. That should be avoided because you make yourself dependent on the implementation details of these classes which could change at any point. For the JDK it is also not possible anymore to access internal fields using reflection starting with JDK 17, see [JEP 403](https://openjdk.org/jeps/403).

If you want to prevent using reflection on third-party classes in the future you can write your own [`ReflectionAccessFilter`](https://www.javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/ReflectionAccessFilter.html) or use one of the predefined ones, such as `ReflectionAccessFilter.BLOCK_ALL_PLATFORM`.

## `InaccessibleObjectException`: 'module ... does not "opens ..." to module com.google.gson'

**Symptom:** An exception with a message in the form 'module ... does not "opens ..." to module com.google.gson' is thrown

**Reason:**

- If the reported package is your own package then you have not configured the module declaration of your project to allow Gson to use reflection on your classes.
- If the reported package is from a third party library or the JDK see [this troubleshooting point](#inaccessibleobjectexception-module--does-not-opens--to-unnamed-module).

**Solution:** Make sure the `module-info.java` file of your project allows Gson to use reflection on your classes, for example:

```java
module mymodule {
    requires com.google.gson;

    opens mypackage to com.google.gson;
}
```

Or in case this occurs for a field in one of your classes which you did not actually want to serialize or deserialize in the first place, you can exclude that field, see the [user guide](UserGuide.md#excluding-fields-from-serialization-and-deserialization).

## Android app not working in Release mode; random property names

**Symptom:** Your Android app is working fine in Debug mode but fails in Release mode and the JSON properties have seemingly random names such as `a`, `b`, ...

**Reason:** You probably have not configured ProGuard / R8 correctly

**Solution:** Make sure you have configured ProGuard / R8 correctly to preserve the names of your fields. See the [Android example](examples/android-proguard-example/README.md) for more information.

## Android app unable to parse JSON after app update

**Symptom:** You released a new version of your Android app and it fails to parse JSON data created by the previous version of your app

**Reason:** You probably have not configured ProGuard / R8 correctly; probably the fields names are being obfuscated and their naming changed between the versions of your app

**Solution:** Make sure you have configured ProGuard / R8 correctly to preserve the names of your fields. See the [Android example](examples/android-proguard-example/README.md) for more information.

If you want to preserve backward compatibility for you app you can use [`@SerializedName`](https://www.javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/annotations/SerializedName.html) on the fields to specify the obfuscated name as alternate, for example: `@SerializedName(value = "myprop", alternate = "a")`

Normally ProGuard and R8 produce a mapping file, this makes it easier to find out the obfuscated field names instead of having to find them out through trial and error or other means. See the [Android Studio user guide](https://developer.android.com/studio/build/shrink-code.html#retracing) for more information.

## Default field values not present after deserialization

**Symptom:** You have assign default values to fields but after deserialization the fields have their standard value (such as `null` or `0`)

**Reason:** Gson cannot invoke the constructor of your class and falls back to JDK `Unsafe` (or similar means)

**Solution:** Make sure that the class:

- is `static` (explicitly or implicitly when it is a top-level class)
- has a no-args constructor

Otherwise Gson will by default try to use JDK `Unsafe` or similar means to create an instance of your class without invoking the constructor and without running any initializers. You can also disable that behavior through [`GsonBuilder.disableJdkUnsafe()`](https://www.javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/GsonBuilder.html#disableJdkUnsafe()) to notice such issues early on.

## `null` values for anonymous and local classes

**Symptom:** Objects of a class are always serialized as JSON `null` / always deserialized as Java `null`

**Reason:** The class you are serializing or deserializing is an anonymous or a local class (or you have specified a custom `ExclusionStrategy`)

**Solution:** Convert the class to a `static` nested class. If the class is already `static` make sure you have not specified a Gson `ExclusionStrategy` which might exclude the class.

Notes:

- "double brace-initialization" also creates anonymous classes
- Local record classes (feature added in Java 16) are supported by Gson and are not affected by this

## Map keys having unexpected format in JSON

**Symptom:** JSON output for `Map` keys is unexpected / cannot be deserialized again

**Reason:** The `Map` key type is 'complex' and you have not configured the `GsonBuilder` properly

**Solution:** Use [`GsonBuilder.enableComplexMapKeySerialization()`](https://www.javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/GsonBuilder.html#enableComplexMapKeySerialization()). See also the [user guide](UserGuide.md#maps-examples) for more information.

## Parsing JSON fails with `MalformedJsonException`

**Symptom:** JSON parsing fails with `MalformedJsonException`

**Reason:** The JSON data is actually malformed

**Solution:** During debugging log the JSON data right before calling Gson methods or set a breakpoint to inspect the data and make sure it has the expected format. Sometimes APIs might return HTML error pages (instead of JSON data) when reaching rate limits or when other errors occur. Also read the location information of the `MalformedJsonException` exception message, it indicates where exactly in the document the malformed data was detected, including the [JSONPath](https://goessner.net/articles/JsonPath/).

## Integral JSON number is parsed as `double`

**Symptom:** JSON data contains an integral number such as `45` but Gson returns it as `double`

**Reason:** When parsing a JSON number as `Object`, Gson will by default create always return a `double`

**Solution:** Use [`GsonBuilder.setObjectToNumberStrategy`](https://www.javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/GsonBuilder.html#setObjectToNumberStrategy(com.google.gson.ToNumberStrategy)) to specify what type of number should be returned

## Malformed JSON not rejected

**Symptom:** Gson parses malformed JSON without throwing any exceptions

**Reason:** Due to legacy reasons Gson performs parsing by default in lenient mode

**Solution:** See [`Gson` class documentation](https://www.javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/Gson.html) section "Lenient JSON handling"

Note: Even in non-lenient mode Gson deviates slightly from the JSON specification, see [`JsonReader.setLenient`](https://www.javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/stream/JsonReader.html#setLenient(boolean)) for more details.

## `IllegalStateException`: "Expected ... but was ..."

**Symptom:** An `IllegalStateException` with a message in the form "Expected ... but was ..." is thrown

**Reason:** The JSON data does not have the correct format

**Solution:** Make sure that your classes correctly model the JSON data. Also during debugging log the JSON data right before calling Gson methods or set a breakpoint to inspect the data and make sure it has the expected format. Read the location information of the exception message, it indicates where exactly in the document the error occurred, including the [JSONPath](https://goessner.net/articles/JsonPath/).

## `IllegalStateException`: "Expected ... but was NULL"

**Symptom:** An `IllegalStateException` with a message in the form "Expected ... but was NULL" is thrown

**Reason:** You have written a custom `TypeAdapter` which does not properly handle a JSON null value

**Solution:** Add code similar to the following at the beginning of the `read` method of your adapter:

```java
@Override
public MyClass read(JsonReader in) throws IOException {
    if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
    }
    
    ...
}
```

Alternatively you can call [`nullSafe()`](https://www.javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/TypeAdapter.html#nullSafe()) on the adapter instance you created.

## Properties missing in JSON

**Symptom:** Properties are missing in the JSON output

**Reason:** Gson by default omits JSON null from the output (or: ProGuard / R8 is not configured correctly and removed unused fields)

**Solution:** Use [`GsonBuilder.serializeNulls()`](https://www.javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/GsonBuilder.html#serializeNulls())

Note: Gson does not support anonymous and local classes and will serialize them as JSON null, see the [related troubleshooting point](#null-values-for-anonymous-and-local-classes).

## JSON output changes for newer Android versions

**Symptom:** The JSON output differs when running on newer Android versions

**Reason:** You use Gson by accident to access internal fields of Android classes

**Solution:** Write custom Gson [`TypeAdapter`](https://www.javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/TypeAdapter.html) implementations for the affected classes or change the type of your data

**Explanation:**

When no built-in adapter for a type exists and no custom adapter has been registered, Gson falls back to using reflection to access the fields of a class (including `private` ones). Most likely you are experiencing this issue because you (by accident) rely on the reflection-based adapter for Android classes. That should be avoided because you make yourself dependent on the implementation details of these classes which could change at any point.

If you want to prevent using reflection on third-party classes in the future you can write your own [`ReflectionAccessFilter`](https://www.javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/ReflectionAccessFilter.html) or use one of the predefined ones, such as `ReflectionAccessFilter.BLOCK_ALL_PLATFORM`.

## JSON output contains values of `static` fields

**Symptom:** The JSON output contains values of `static` fields

**Reason:** You used `GsonBuilder.excludeFieldsWithModifiers` to overwrite the default excluded modifiers

**Solution:** When calling `GsonBuilder.excludeFieldsWithModifiers` you overwrite the default excluded modifiers. Therefore, you have to explicitly exclude `static` fields if desired. This can be done by adding `Modifier.STATIC` as additional argument.

## `NoSuchMethodError` when calling Gson methods

**Symptom:** A `java.lang.NoSuchMethodError` is thrown when trying to call certain Gson methods

**Reason:**

- You have multiple versions of Gson on your classpath
- Or, the Gson version you compiled against is different from the one on your classpath
- Or, you are using a code shrinking tool such as ProGuard or R8 which removed methods from Gson

**Solution:** First disable any code shrinking tools such as ProGuard or R8 and check if the issue persists. If not, you have to tweak the configuration of that tool to not modify Gson classes. Otherwise verify that the Gson JAR on your classpath is the same you are compiling against, and that there is only one Gson JAR on your classpath. See [this Stack Overflow question](https://stackoverflow.com/q/227486) to find out where a class is loaded from. For example, for debugging you could include the following code:

```java
System.out.println(Gson.class.getProtectionDomain().getCodeSource().getLocation());
```

If that fails with a `NullPointerException` you have to try one of the other ways to find out where a class is loaded from.
