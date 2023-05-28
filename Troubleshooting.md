# Troubleshooting Guide

This guide describes how to troubleshoot common issues when using Gson.

<!-- The '<a id="..."></a>' anchors below are used to create stable links; don't remove or rename them -->
<!-- Use only lowercase IDs, GitHub seems to not support uppercase IDs, see also https://github.com/orgs/community/discussions/50962 -->

## <a id="class-cast-exception"></a> `ClassCastException` when using deserialized object

**Symptom:** `ClassCastException` is thrown when accessing an object deserialized by Gson

**Reason:** Your code is most likely not type-safe

**Solution:** Make sure your code adheres to the following:

- Avoid raw types: Instead of calling `fromJson(..., List.class)`, create for example a `TypeToken<List<MyClass>>`.
  See the [user guide](UserGuide.md#collections-examples) for more information.
- When using `TypeToken` prefer the `Gson.fromJson` overloads with `TypeToken` parameter such as [`fromJson(Reader, TypeToken)`](https://www.javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/Gson.html#fromJson(java.io.Reader,com.google.gson.reflect.TypeToken)).
  The overloads with `Type` parameter do not provide any type-safety guarantees.
- When using `TypeToken` make sure you don't capture a type variable. For example avoid something like `new TypeToken<List<T>>()` (where `T` is a type variable). Due to Java type erasure the actual type of `T` is not available at runtime. Refactor your code to pass around `TypeToken` instances or use [`TypeToken.getParameterized(...)`](https://www.javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/reflect/TypeToken.html#getParameterized(java.lang.reflect.Type,java.lang.reflect.Type...)), for example `TypeToken.getParameterized(List.class, elementClass)`.

## <a id="reflection-inaccessible"></a> `InaccessibleObjectException`: 'module ... does not "opens ..." to unnamed module'

**Symptom:** An exception with a message in the form 'module ... does not "opens ..." to unnamed module' is thrown

**Reason:** You use Gson by accident to access internal fields of third-party classes

**Solution:** Write custom Gson [`TypeAdapter`](https://www.javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/TypeAdapter.html) implementations for the affected classes or change the type of your data. If this occurs for a field in one of your classes which you did not actually want to serialize or deserialize in the first place, you can exclude that field, see the [user guide](UserGuide.md#excluding-fields-from-serialization-and-deserialization).

**Explanation:**

When no built-in adapter for a type exists and no custom adapter has been registered, Gson falls back to using reflection to access the fields of a class (including `private` ones). Most likely you are seeing this error because you (by accident) rely on the reflection-based adapter for third-party classes. That should be avoided because you make yourself dependent on the implementation details of these classes which could change at any point. For the JDK it is also not possible anymore to access internal fields using reflection starting with JDK 17, see [JEP 403](https://openjdk.org/jeps/403).

If you want to prevent using reflection on third-party classes in the future you can write your own [`ReflectionAccessFilter`](https://www.javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/ReflectionAccessFilter.html) or use one of the predefined ones, such as `ReflectionAccessFilter.BLOCK_ALL_PLATFORM`.

## <a id="reflection-inaccessible-to-module-gson"></a> `InaccessibleObjectException`: 'module ... does not "opens ..." to module com.google.gson'

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

## <a id="android-app-random-names"></a> Android app not working in Release mode; random property names

**Symptom:** Your Android app is working fine in Debug mode but fails in Release mode and the JSON properties have seemingly random names such as `a`, `b`, ...

**Reason:** You probably have not configured ProGuard / R8 correctly

**Solution:** Make sure you have configured ProGuard / R8 correctly to preserve the names of your fields. See the [Android example](examples/android-proguard-example/README.md) for more information.

## <a id="android-app-broken-after-app-update"></a> Android app unable to parse JSON after app update

**Symptom:** You released a new version of your Android app and it fails to parse JSON data created by the previous version of your app

**Reason:** You probably have not configured ProGuard / R8 correctly; probably the fields names are being obfuscated and their naming changed between the versions of your app

**Solution:** Make sure you have configured ProGuard / R8 correctly to preserve the names of your fields. See the [Android example](examples/android-proguard-example/README.md) for more information.

If you want to preserve backward compatibility for you app you can use [`@SerializedName`](https://www.javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/annotations/SerializedName.html) on the fields to specify the obfuscated name as alternate, for example: `@SerializedName(value = "myprop", alternate = "a")`

Normally ProGuard and R8 produce a mapping file, this makes it easier to find out the obfuscated field names instead of having to find them out through trial and error or other means. See the [Android Studio user guide](https://developer.android.com/studio/build/shrink-code.html#retracing) for more information.

## <a id="default-field-values-missing"></a> Default field values not present after deserialization

**Symptom:** You have assign default values to fields but after deserialization the fields have their standard value (such as `null` or `0`)

**Reason:** Gson cannot invoke the constructor of your class and falls back to JDK `Unsafe` (or similar means)

**Solution:** Make sure that the class:

- is `static` (explicitly or implicitly when it is a top-level class)
- has a no-args constructor

Otherwise Gson will by default try to use JDK `Unsafe` or similar means to create an instance of your class without invoking the constructor and without running any initializers. You can also disable that behavior through [`GsonBuilder.disableJdkUnsafe()`](https://www.javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/GsonBuilder.html#disableJdkUnsafe()) to notice such issues early on.

## <a id="anonymous-local-null"></a> `null` values for anonymous and local classes

**Symptom:** Objects of a class are always serialized as JSON `null` / always deserialized as Java `null`

**Reason:** The class you are serializing or deserializing is an anonymous or a local class (or you have specified a custom `ExclusionStrategy`)

**Solution:** Convert the class to a `static` nested class. If the class is already `static` make sure you have not specified a Gson `ExclusionStrategy` which might exclude the class.

Notes:

- "double brace-initialization" also creates anonymous classes
- Local record classes (feature added in Java 16) are supported by Gson and are not affected by this

## <a id="map-key-wrong-json"></a> Map keys having unexpected format in JSON

**Symptom:** JSON output for `Map` keys is unexpected / cannot be deserialized again

**Reason:** The `Map` key type is 'complex' and you have not configured the `GsonBuilder` properly

**Solution:** Use [`GsonBuilder.enableComplexMapKeySerialization()`](https://www.javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/GsonBuilder.html#enableComplexMapKeySerialization()). See also the [user guide](UserGuide.md#maps-examples) for more information.

## <a id="malformed-json"></a> Parsing JSON fails with `MalformedJsonException`

**Symptom:** JSON parsing fails with `MalformedJsonException`

**Reason:** The JSON data is actually malformed

**Solution:** During debugging, log the JSON data right before calling Gson methods or set a breakpoint to inspect the data and make sure it has the expected format. Sometimes APIs might return HTML error pages (instead of JSON data) when reaching rate limits or when other errors occur. Also read the location information of the `MalformedJsonException` exception message, it indicates where exactly in the document the malformed data was detected, including the [JSONPath](https://goessner.net/articles/JsonPath/).

For example, let's assume you want to deserialize the following JSON data:

```json
{
  "languages": [
    "English",
    "French",
  ]
}
```

This will fail with an exception similar to this one: `MalformedJsonException: Use JsonReader.setLenient(true) to accept malformed JSON at line 5 column 4 path $.languages[2]`  
The problem here is the trailing comma (`,`) after `"French"`, trailing commas are not allowed by the JSON specification. The location information "line 5 column 4" points to the `]` in the JSON data (with some slight inaccuracies) because Gson expected another value after `,` instead of the closing `]`. The JSONPath `$.languages[2]` in the exception message also points there: `$.` refers to the root object, `languages` refers to its member of that name and `[2]` refers to the (missing) third value in the JSON array value of that member (numbering starts at 0, so it is `[2]` instead of `[3]`).  
The proper solution here is to fix the malformed JSON data.

To spot syntax errors in the JSON data easily you can open it in an editor with support for JSON, for example Visual Studio Code. It will highlight within the JSON data the error location and show why the JSON data is considered invalid.

## <a id="number-parsed-as-double"></a> Integral JSON number is parsed as `double`

**Symptom:** JSON data contains an integral number such as `45` but Gson returns it as `double`

**Reason:** When parsing a JSON number as `Object`, Gson will by default create always return a `double`

**Solution:** Use [`GsonBuilder.setObjectToNumberStrategy`](https://www.javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/GsonBuilder.html#setObjectToNumberStrategy(com.google.gson.ToNumberStrategy)) to specify what type of number should be returned

## <a id="default-lenient"></a> Malformed JSON not rejected

**Symptom:** Gson parses malformed JSON without throwing any exceptions

**Reason:** Due to legacy reasons Gson performs parsing by default in lenient mode

**Solution:** See [`Gson` class documentation](https://www.javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/Gson.html#default-lenient) section "Lenient JSON handling"

Note: Even in non-lenient mode Gson deviates slightly from the JSON specification, see [`JsonReader.setLenient`](https://www.javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/stream/JsonReader.html#setLenient(boolean)) for more details.

## <a id="unexpected-json-structure"></a> `IllegalStateException`: "Expected ... but was ..."

**Symptom:** An `IllegalStateException` with a message in the form "Expected ... but was ..." is thrown

**Reason:** The JSON data does not have the correct format

**Solution:** Make sure that your classes correctly model the JSON data. Also during debugging log the JSON data right before calling Gson methods or set a breakpoint to inspect the data and make sure it has the expected format. Read the location information of the exception message, it indicates where exactly in the document the error occurred, including the [JSONPath](https://goessner.net/articles/JsonPath/).

For example, let's assume you have the following Java class:

```java
class WebPage {
    String languages;
}
```

And you want to deserialize the following JSON data:

```json
{
  "languages": ["English", "French"]
}
```

This will fail with an exception similar to this one: `IllegalStateException: Expected a string but was BEGIN_ARRAY at line 2 column 17 path $.languages`  
This means Gson expected a JSON string value but found the beginning of a JSON array (`[`). The location information "line 2 column 17" points to the `[` in the JSON data (with some slight inaccuracies), so does the JSONPath `$.languages` in the exception message. It refers to the `languages` member of the root object (`$.`).  
The solution here is to change in the `WebPage` class the field `String languages` to `List<String> languages`.

## <a id="adapter-not-null-safe"></a> `IllegalStateException`: "Expected ... but was NULL"

**Symptom:** An `IllegalStateException` with a message in the form "Expected ... but was NULL" is thrown

**Reason:**

- A built-in adapter does not support JSON null values
- You have written a custom `TypeAdapter` which does not properly handle JSON null values

**Solution:** If this occurs for a custom adapter you wrote, add code similar to the following at the beginning of its `read` method:

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

## <a id="serialize-nulls"></a> Properties missing in JSON

**Symptom:** Properties are missing in the JSON output

**Reason:** Gson by default omits JSON null from the output (or: ProGuard / R8 is not configured correctly and removed unused fields)

**Solution:** Use [`GsonBuilder.serializeNulls()`](https://www.javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/GsonBuilder.html#serializeNulls())

Note: Gson does not support anonymous and local classes and will serialize them as JSON null, see the [related troubleshooting point](#null-values-for-anonymous-and-local-classes).

## <a id="android-internal-fields"></a> JSON output changes for newer Android versions

**Symptom:** The JSON output differs when running on newer Android versions

**Reason:** You use Gson by accident to access internal fields of Android classes

**Solution:** Write custom Gson [`TypeAdapter`](https://www.javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/TypeAdapter.html) implementations for the affected classes or change the type of your data

**Explanation:**

When no built-in adapter for a type exists and no custom adapter has been registered, Gson falls back to using reflection to access the fields of a class (including `private` ones). Most likely you are experiencing this issue because you (by accident) rely on the reflection-based adapter for Android classes. That should be avoided because you make yourself dependent on the implementation details of these classes which could change at any point.

If you want to prevent using reflection on third-party classes in the future you can write your own [`ReflectionAccessFilter`](https://www.javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/ReflectionAccessFilter.html) or use one of the predefined ones, such as `ReflectionAccessFilter.BLOCK_ALL_PLATFORM`.

## <a id="json-static-fields"></a> JSON output contains values of `static` fields

**Symptom:** The JSON output contains values of `static` fields

**Reason:** You used `GsonBuilder.excludeFieldsWithModifiers` to overwrite the default excluded modifiers

**Solution:** When calling `GsonBuilder.excludeFieldsWithModifiers` you overwrite the default excluded modifiers. Therefore, you have to explicitly exclude `static` fields if desired. This can be done by adding `Modifier.STATIC` as additional argument.

## <a id="no-such-method-error"></a> `NoSuchMethodError` when calling Gson methods

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

## <a id="duplicate-fields"></a> `IllegalArgumentException`: 'Class ... declares multiple JSON fields named '...''

**Symptom:** An exception with the message 'Class ... declares multiple JSON fields named '...'' is thrown

**Reason:**

- The name you have specified with a [`@SerializedName`](https://www.javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/annotations/SerializedName.html) annotation for a field collides with the name of another field
- The [`FieldNamingStrategy`](https://javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/FieldNamingStrategy.html) you have specified produces conflicting field names
- A field of your class has the same name as the field of a superclass

Gson prevents multiple fields with the same name because during deserialization it would be ambiguous for which field the JSON data should be deserialized. For serialization it would cause the same field to appear multiple times in JSON. While the JSON specification permits this, it is likely that the application parsing the JSON data will not handle it correctly.

**Solution:** First identify the fields with conflicting names based on the exception message. Then decide if you want to rename one of them using the [`@SerializedName`](https://www.javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/annotations/SerializedName.html) annotation, or if you want to [exclude](UserGuide.md#excluding-fields-from-serialization-and-deserialization) one of them. When excluding one of the fields you have to include it for both serialization and deserialization (even if your application only performs one of these actions) because the duplicate field check cannot differentiate between these actions.

## <a id="java-lang-class-unsupported"></a> `UnsupportedOperationException` when serializing or deserializing `java.lang.Class`

**Symptom:** An `UnsupportedOperationException` is thrown when trying to serialize or deserialize `java.lang.Class`

**Reason:** Gson intentionally does not permit serializing and deserializing `java.lang.Class` for security reasons. Otherwise a malicious user could make your application load an arbitrary class from the classpath and, depending on what your application does with the `Class`, in the worst case perform a remote code execution attack.

**Solution:** First check if you really need to serialize or deserialize a `Class`. Often it is possible to use string aliases and then map them to the known `Class`; you could write a custom [`TypeAdapter`](https://javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/TypeAdapter.html) to do this. If the `Class` values are not known in advance, try to introduce a common base class or interface for all these classes and then verify that the deserialized class is a subclass. For example assuming the base class is called `MyBaseClass`, your custom `TypeAdapter` should load the class like this:

```java
Class.forName(jsonString, false, getClass().getClassLoader()).asSubclass(MyBaseClass.class)
```

This will not initialize arbitrary classes, and it will throw a `ClassCastException` if the loaded class is not the same as or a subclass of `MyBaseClass`.

## <a id="type-token-raw"></a> `IllegalStateException`: 'TypeToken must be created with a type argument' <br> `RuntimeException`: 'Missing type parameter'

**Symptom:** An `IllegalStateException` with the message 'TypeToken must be created with a type argument' is thrown.  
For older Gson versions a `RuntimeException` with message 'Missing type parameter' is thrown.

**Reason:**

- You created a `TypeToken` without type argument, for example `new TypeToken() {}` (note the missing `<...>`). You always have to provide the type argument, for example like this: `new TypeToken<List<String>>() {}`. Normally the compiler will also emit a 'raw types' warning when you forget the `<...>`.
- You are using a code shrinking tool such as ProGuard or R8 (Android app builds normally have this enabled by default) but have not configured it correctly for usage with Gson.

**Solution:** When you are using a code shrinking tool such as ProGuard or R8 you have to adjust your configuration to include the following rules:

```
# Keep generic signatures; needed for correct type resolution
-keepattributes Signature

# Keep class TypeToken (respectively its generic signature)
-keep class com.google.gson.reflect.TypeToken { *; }

# Keep any (anonymous) classes extending TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
```

See also the [Android example](examples/android-proguard-example/README.md) for more information.

Note: For newer Gson versions these rules might be applied automatically; make sure you are using the latest Gson version and the latest version of the code shrinking tool.

## <a id="r8-abstract-class"></a> `JsonIOException`: 'Abstract classes can't be instantiated!' (R8)

**Symptom:** A `JsonIOException` with the message 'Abstract classes can't be instantiated!' is thrown; the class mentioned in the exception message is not actually `abstract` in your source code, and you are using the code shrinking tool R8 (Android app builds normally have this configured by default).

**Reason:** The code shrinking tool R8 performs optimizations where it removes the no-args constructor from a class and makes the class `abstract`. Due to this Gson cannot create an instance of the class.

**Solution:** Make sure the class has a no-args constructor, then adjust your R8 configuration file to keep the constructor of the class. For example:

```
# Keep the no-args constructor of the deserialized class
-keepclassmembers class com.example.MyClass {
  <init>();
}
```

For Android you can add this rule to the `proguard-rules.pro` file, see also the [Android documentation](https://developer.android.com/build/shrink-code#keep-code). In case the class name in the exception message is obfuscated, see the Android documentation about [retracing](https://developer.android.com/build/shrink-code#retracing).

Note: If the class which you are trying to deserialize is actually abstract, then this exception is probably unrelated to R8 and you will have to implement a custom [`InstanceCreator`](https://javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/InstanceCreator.html) or [`TypeAdapter`](https://javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/TypeAdapter.html) which creates an instance of a non-abstract subclass of the class.
