Change Log
==========

The change log for versions newer than 2.10 is available only on the [GitHub Releases page](https://github.com/google/gson/releases).

## Version 2.10

* Support for serializing and deserializing Java records, on Java ≥ 16. (https://github.com/google/gson/pull/2201)
* Add `JsonArray.asList` and `JsonObject.asMap` view methods (https://github.com/google/gson/pull/2225)
* Fix `TypeAdapterRuntimeTypeWrapper` not detecting reflective `TreeTypeAdapter` and `FutureTypeAdapter` (https://github.com/google/gson/pull/1787)
* Improve `JsonReader.skipValue()` (https://github.com/google/gson/pull/2062)
* Perform numeric conversion for primitive numeric type adapters (https://github.com/google/gson/pull/2158)
* Add `Gson.fromJson(..., TypeToken)` overloads (https://github.com/google/gson/pull/1700)
* Fix changes to `GsonBuilder` affecting existing `Gson` instances (https://github.com/google/gson/pull/1815)
* Make `JsonElement` conversion methods more consistent and fix javadoc (https://github.com/google/gson/pull/2178)
* Throw `UnsupportedOperationException` when `JsonWriter.jsonValue` is not supported (https://github.com/google/gson/pull/1651)
* Disallow `JsonObject` `Entry.setValue(null)` (https://github.com/google/gson/pull/2167)
* Fix `TypeAdapter.toJson` throwing AssertionError for custom IOException (https://github.com/google/gson/pull/2172)
* Convert null to JsonNull for `JsonArray.set` (https://github.com/google/gson/pull/2170)
* Fixed nullSafe usage. (https://github.com/google/gson/pull/1555)
* Validate `TypeToken.getParameterized` arguments (https://github.com/google/gson/pull/2166)
* Fix #1702: Gson.toJson creates CharSequence which does not implement toString (https://github.com/google/gson/pull/1703)
* Prefer existing adapter for concurrent `Gson.getAdapter` calls (https://github.com/google/gson/pull/2153)
* Improve `ArrayTypeAdapter` for `Object[]` (https://github.com/google/gson/pull/1716)
* Improve `AppendableWriter` performance (https://github.com/google/gson/pull/1706)

## Version 2.9.1

* Make `Object` and `JsonElement` deserialization iterative rather than
  recursive (https://github.com/google/gson/pull/1912)
* Added parsing support for enum that has overridden toString() method (https://github.com/google/gson/pull/1950)
* Removed support for building Gson with Gradle (https://github.com/google/gson/pull/2081)
* Removed obsolete `codegen` hierarchy (https://github.com/google/gson/pull/2099)
* Add support for reflection access filter (https://github.com/google/gson/pull/1905)
* Improve `TypeToken` creation validation (https://github.com/google/gson/pull/2072)
* Add explicit support for `float` in `JsonWriter` (https://github.com/google/gson/pull/2130, https://github.com/google/gson/pull/2132)
* Fail when parsing invalid local date (https://github.com/google/gson/pull/2134)

Also many small improvements to javadoc.

## Version 2.9.0

**The minimum supported Java version changes from 6 to 7.**

* Change target Java version to 7 (https://github.com/google/gson/pull/2043)
* Put `module-info.class` into Multi-Release JAR folder (https://github.com/google/gson/pull/2013)
* Improve error message when abstract class cannot be constructed (https://github.com/google/gson/pull/1814)
* Support EnumMap deserialization (https://github.com/google/gson/pull/2071)
* Add LazilyParsedNumber default adapter (https://github.com/google/gson/pull/2060)
* Fix JsonReader.hasNext() returning true at end of document (https://github.com/google/gson/pull/2061)
* Remove Gradle build support. Build script was outdated and not actively
  maintained anymore (https://github.com/google/gson/pull/2063)
* Add `GsonBuilder.disableJdkUnsafe()` (https://github.com/google/gson/pull/1904)
* Add `UPPER_CASE_WITH_UNDERSCORES` in FieldNamingPolicy (https://github.com/google/gson/pull/2024)
* Fix failing to serialize Collection or Map with inaccessible constructor (https://github.com/google/gson/pull/1902)
* Improve TreeTypeAdapter thread-safety (https://github.com/google/gson/pull/1976)
* Fix `Gson.newJsonWriter` ignoring lenient and HTML-safe setting (https://github.com/google/gson/pull/1989)
* Delete unused LinkedHashTreeMap (https://github.com/google/gson/pull/1992)
* Make default adapters stricter; improve exception messages (https://github.com/google/gson/pull/2000)
* Fix `FieldNamingPolicy.upperCaseFirstLetter` uppercasing non-letter (https://github.com/google/gson/pull/2004)

## Version 2.8.9

* Make OSGi bundle's dependency on `sun.misc` optional (https://github.com/google/gson/pull/1993).
* Deprecate `Gson.excluder()` exposing internal `Excluder` class (https://github.com/google/gson/pull/1986).
* Prevent Java deserialization of internal classes (https://github.com/google/gson/pull/1991).
* Improve number strategy implementation (https://github.com/google/gson/pull/1987).
* Fix LongSerializationPolicy null handling being inconsistent with Gson (https://github.com/google/gson/pull/1990).
* Support arbitrary Number implementation for Object and Number deserialization (https://github.com/google/gson/pull/1290).
* Bump proguard-maven-plugin from 2.4.0 to 2.5.1 (https://github.com/google/gson/pull/1980).
* Don't exclude static local classes (https://github.com/google/gson/pull/1969).
* Fix `RuntimeTypeAdapterFactory` depending on internal `Streams` class (https://github.com/google/gson/pull/1959).
* Improve Maven build (https://github.com/google/gson/pull/1964).
* Make dependency on `java.sql` optional (https://github.com/google/gson/pull/1707).

## Version 2.8.8

* Fixed issue with recursive types (https://github.com/google/gson/issues/1390).
* Better behaviour with Java 9+ and `Unsafe` if there is a security manager (https://github.com/google/gson/pull/1712).
* `EnumTypeAdapter` now works better when ProGuard has obfuscated enum fields (https://github.com/google/gson/pull/1495).

## Version 2.8.7

* Fixed `ISO8601UtilsTest` failing on systems with UTC+X.
* Improved javadoc for `JsonStreamParser`.
* Updated proguard.cfg (https://github.com/google/gson/pull/1693).
* Fixed `IllegalStateException` in `JsonTreeWriter` (https://github.com/google/gson/issues/1592).
* Added `JsonArray.isEmpty()` (https://github.com/google/gson/pull/1640).
* Added new test cases (https://github.com/google/gson/pull/1638).
* Fixed OSGi metadata generation to work on JavaSE < 9 (https://github.com/google/gson/pull/1603).

## Version 2.8.6
_2019-10-04_  [GitHub Diff](https://github.com/google/gson/compare/gson-parent-2.8.5...gson-parent-2.8.6)
 * Added static methods `JsonParser.parseString` and `JsonParser.parseReader` and deprecated instance method `JsonParser.parse`
 * Java 9 module-info support

## Version 2.8.5
_2018-05-21_  [GitHub Diff](https://github.com/google/gson/compare/gson-parent-2.8.4...gson-parent-2.8.5)
 * Print Gson version while throwing AssertionError and IllegalArgumentException
 * Moved `utils.VersionUtils` class to `internal.JavaVersion`. This is a potential backward incompatible change from 2.8.4
 * Fixed issue https://github.com/google/gson/issues/1310 by supporting Debian Java 9

## Version 2.8.4
_2018-05-01_  [GitHub Diff](https://github.com/google/gson/compare/gson-parent-2.8.3...gson-parent-2.8.4)
 * Added a new FieldNamingPolicy, `LOWER_CASE_WITH_DOTS` that mapps JSON name `someFieldName` to `some.field.name`
 * Fixed issue https://github.com/google/gson/issues/1305 by removing compile/runtime dependency on `sun.misc.Unsafe`

## Version 2.8.3
_2018-04-27_  [GitHub Diff](https://github.com/google/gson/compare/gson-parent-2.8.2...gson-parent-2.8.3)
 * Added a new API, `GsonBuilder.newBuilder()` that clones the current builder
 * Preserving DateFormatter behavior on JDK 9
 * Numerous other bugfixes

## Version 2.8.2
_2017-09-19_  [GitHub Diff](https://github.com/google/gson/compare/gson-parent-2.8.1...gson-parent-2.8.2)
 * Introduced a new API, `JsonElement.deepCopy()`
 * Numerous other bugfixes

## Version 2.8.1
_2017-05-30_  [GitHub Diff](https://github.com/google/gson/compare/gson-parent-2.8.0...gson-parent-2.8.1)
 * New: `JsonObject.keySet()`
 * `@JsonAdapter` annotation can now use `JsonSerializer` and `JsonDeserializer` as well.

## Version 2.8
_2016-10-26_  [GitHub Diff](https://github.com/google/gson/compare/gson-parent-2.7...gson-parent-2.8.0)
 * New: `TypeToken.getParameterized()` and `TypeToken.getArray()` make it easier to
   register or look up a `TypeAdapter`.
 * New: `@JsonAdapter(nullSafe=true)` to specify that a custom type adapter handles null.

## Version 2.7
_2016-06-14_  [GitHub Diff](https://github.com/google/gson/compare/gson-parent-2.6.2...gson-parent-2.7)
 * Added support for JsonSerializer/JsonDeserializer in @JsonAdapter annotation
 * Exposing Gson properties excluder(), fieldNamingStrategy(), serializeNulls(), htmlSafe()
 * Added JsonObject.size() method
 * Added JsonWriter.value(Boolean value) method
 * Using ArrayDeque, ConcurrentHashMap, and other JDK 1.6 features
 * Better error reporting
 * Plenty of other bug fixes

## Version 2.6.2
_2016-02-26_  [GitHub Diff](https://github.com/google/gson/compare/gson-parent-2.6.1...gson-parent-2.6.2)
 * Fixed an NPE bug with @JsonAdapter annotation
 * Added back OSGI manifest
 * Some documentation typo fixes

## Version 2.6.1

_2016-02-11_ [GitHub Diff](https://github.com/google/gson/compare/gson-parent-2.6...gson-parent-2.6.1)

 * Fix: The 2.6 release targeted Java 1.7, but we intend to target Java 1.6. The
   2.6.1 release is identical to 2.6, but it targets Java 1.6.


## Version 2.6

_2016-02-11_ [GitHub Diff](https://github.com/google/gson/compare/gson-parent-2.5...gson-parent-2.6)

 * Permit timezones without minutes in the default date adapter.
 * Update reader and writer for RFC 7159. This means that strings, numbers,
   booleans and null may be top-level values in JSON documents, even if the
   reader is strict.
 * New `setLenient()` method on `GsonBuilder`. This setting impacts the new
   factory method `Gson.newJsonReader()`.
 * Adapters discovered with `@JsonAdapter` are now null safe by default.


## Version 2.5

_2015-11-24_ [GitHub Diff](https://github.com/google/gson/compare/gson-parent-2.4...gson-parent-2.5)

 * Updated minimum JDK version to 1.6
 * Improved Date Deserialization by accepting many date formats
 * Added support for `java.util.Currency`, `AtomicLong`, `AtomicLongArray`, `AtomicInteger`, `AtomicIntegerArray`, `AtomicBoolean`. This change is backward-incompatible because the earlier version of Gson used the default serialization which wasn't intuitive. We hope that these classes are not used enough to actually cause problems in the field.
 * Improved debugging information when some exceptions are thrown


## Version 2.4

_2015-10-04_

 * **Drop `IOException` from `TypeAdapter.toJson()`.** This is a binary-compatible change, but may
   cause compiler errors where `IOExceptions` are being caught but no longer thrown. The correct fix
   for this problem is to remove the unnecessary `catch` clause.
 * New: `Gson.newJsonWriter` method returns configured `JsonWriter` instances.
 * New: `@SerializedName` now works with [AutoValue’s][autovalue] abstract property methods.
 * New: `@SerializedName` permits alternate names when deserializing.
 * New: `JsonWriter#jsonValue` writes raw JSON values.
 * New: APIs to add primitives directly to `JsonArray` instances.
 * New: ISO 8601 date type adapter. Find this in _extras_.
 * Fix: `FieldNamingPolicy` now works properly when running on a device with a Turkish locale.
  [autovalue]: https://github.com/google/auto/tree/main/value


## Version 2.3.1

_2014-11-20_

 * Added support to serialize objects with self-referential fields. The self-referential field is set to null in JSON. Previous version of Gson threw a StackOverflowException on encountering any self-referential fields.
   * The most visible impact of this is that Gson can now serialize Throwable (Exception and Error)
 * Added support for @JsonAdapter annotation on enums which are user defined types
 * Fixed bug in getPath() with array of objects and arrays of arrays
 * Other smaller bug fixes


## Version 2.3

_2014-08-11_

 * The new @JsonAdapter annotation to specify a Json TypeAdapter for a class field
 * JsonPath support: JsonReader.getPath() method returns the JsonPath expression
 * New public methods in JsonArray (similar to the java.util.List): `contains(JsonElement), remove(JsonElement), remove(int index), set(int index, JsonElement element)`
 * Many other smaller bug fixes


## Version 2.2.4

_2013-05-13_

 * Fix internal map (LinkedHashTreeMap) hashing bug.
 * Bug fix (Issue 511)


## Version 2.2.3

_2013-04-12_

 * Fixes for possible DoS attack due to poor String hashing


## Version 2.2.2

_2012-07-02_

 * Gson now allows a user to override default type adapters for Primitives and Strings. This behavior was allowed in earlier versions of Gson but was prohibited started Gson 2.0. We decided to allow it again: This enables a user to parse 1/0 as boolean values for compatibility with iOS JSON libraries.
 * (Incompatible behavior change in `JsonParser`): In the past, if `JsonParser` encountered a stream that terminated prematurely, it returned `JsonNull`. This behavior wasn't correct because the stream had invalid JSON, not a null. `JsonParser` is now changed to throw `JsonSyntaxException` in this case. Note that if JsonParser (or Gson) encounter an empty stream, they still return `JsonNull`.


## Version 2.2.1

_2012-05-05_

 * Very minor fixes


## Version 2.2

_2012-05-05_

 * Added getDelegateAdapter in Gson class
 * Fixed a security bug related to denial of service attack with Java HashMap String collisions.


## Version 2.1

_2011-12-30_ (Targeted Dec 31, 2011)

 * Support for user-defined streaming type adapters
 * continued performance enhancements
 * Dropped support for type hierarchy instance creators. We don't expect this to be a problem. We'll also detect fewer errors where multiple type adapters can serialize the same type. With APIs like getNextTypeAdapter, this might actually be an improvement!


## Version 2.0

_2011-11-13_

#### Faster

 * Previous versions first parsed complete document into a DOM-style model (JsonObject or JsonArray) and then bound data against that. Gson 2 does data binding directly from the stream parser.

#### More Predictable

 * Objects are serialized and deserialized in the same way, regardless of where they occur in the object graph.

#### Changes to watch out for

  * Gson 1.7 would serialize top-level nulls as "". 2.0 serializes them as "null".
    ```
    String json = gson.toJson(null, Foo.class);
    1.7: json == ""
    2.0: json == "null"
    ```

  * Gson 1.7 permitted duplicate map keys. 2.0 forbids them.
    ```
    String json = "{'a':1,'a':2}";
    Map<String, Integer> map = gson.fromJson(json, mapType);
    1.7: map == {a=2}
    2.0: JsonSyntaxException thrown
    ```

  * Gson 1.7 won’t serialize subclass fields in collection elements. 2.0 adds this extra information.
    ```
    List<Point2d> points = new ArrayList<Point2d>();
    points.add(new Point3d(1, 2, 3));
    String json = gson.toJson(points,
        new TypeToken<List<Point2d>>() {}.getType());
    1.7: json == "[{'x':1,'y':2}]"
    2.0: json == "[{'x':1,'y':2,'z':3}]"
    ```

  * Gson 1.7 binds single-element arrays as their contents. 2.0 doesn’t.
    ```
    Integer i = gson.fromJson("[42]", Integer.class);
    1.7: i == 42
    2.0: JsonSyntaxException thrown
    ```

#### Other changes to be aware of
 * Gson 2.0 doesn’t support type adapters for primitive types.
 * Gson 1.7 uses arbitrary precision for primitive type conversion (so -122.08e-2132 != 0). Gson 2.0 uses double precision (so -122.08e-2132 == 0).
 * Gson 1.7 sets subclass fields when an InstanceCreator returns a subclass when the value is a field of another object. Gson 2.0 sets fields of the requested type only.
 * Gson 1.7 versioning never skips the top-level object. Gson 2.0 versioning applies to all objects.
 * Gson 1.7 truncates oversized large integers. Gson 2.0 fails on them.
 * Gson 2.0 permits integers to have .0 fractions like "1.0".
 * Gson 1.7 throws IllegalStateException on circular references. Gson 2.0 lets the runtime throw a StackOverflowError.


## Version 1.7.2

_2011-09-30_ (Unplanned release)
 * Fixed a threading issue in FieldAttributes (Issue 354)


## Version 1.7.1

_2011-04-13_ (Unplanned release)

 * Fixed Gson jars in Maven Central repository
 * Removed assembly-descriptor.xml and maven pom.xml/pom.properties files from Gson binary jar. This also ensures that jarjar can be run correctly on Gson.


## Version 1.7

_2011-04-12_ (Targeted: Jan 2011)

 * No need to define no-args constructors for classes serialized with Gson
 * Ability to register a hierarchical type adapter
 * Support for serialization and deserialization of maps with complex keys
 * Serialization and deserialization specific exclusion strategies
 * Allow concrete data structure fields without type adapters
 * Fixes "type" management (i.e. Wildcards, etc.)
 * Major performance enhancements by reducing the need for Java reflection
See detailed announcement at this thread in the Gson Google Group.


## Version 1.6

_2010-11-24_ (Targeted: Oct, 2010)

 * New stream parser APIs
 * New parser that improves parsing performance significantly


## Version 1.5

_2010-08-19_ (Target Date: Aug 18, 2010)

 * Added `UPPER_CAMEL_CASE_WITH_SPACES` naming policy
 * Added SQL date and time support
 * A number of performance improvements: Using caching of field annotations for speeding up reflection, replacing recursive calls in the parser with a for loop.


## Version 1.4 BETA

_2009_10_09_

 * JsonStreamParser: A streaming parser API class to deserialize multiple JSON objects on a stream (such as a pipelined HTTP response)
 * Raised the deserialization limit for byte and object arrays and collection to over 11MB from 80KB. See issue 96.
 * While serializing, Gson now uses the actual type of a field. This allows serialization of base-class references holding sub-classes to the JSON for the sub-class. It also allows serialization of raw collections. See Issue 155, 156.
 * Added a `Gson.toJsonTree()` method that serializes a Java object to a tree of JsonElements. See issue 110.
 * Added a `Gson.fromJson(JsonElement)` method that deserializes from a Json parse tree.
 * Updated `Expose` annotation to contain parameters serialize and deserialize to control whether a field gets serialized or deserialized. See issue 146.
 * Added a new naming policy `LOWER_CASE_WITH_DASHES`
 * Default date type adapter is now thread-safe. See Issue 162.
 * `JsonElement.toString()` now outputs valid JSON after escaping characters properly. See issue 154.
 * `JsonPrimitive.equals()` now returns true for two numbers if their values are equal. All integral types (long, int, short, byte, BigDecimal, Long, Integer, Short, Byte) are treated equivalent for comparison. Similarly, floating point types (double, float, BigDecimal, Double, Float) are treated equivalent as well. See issue 147.
 * Fixed bugs in pretty printing. See issue 153.
 * If a field causes circular reference error, Gson lists the field name instead of the object value. See issue 118.
 * Gson now serializes a list with null elements correctly. See issue 117.
 * Fixed issue 121, 123, 126.
 * Support user defined exclusion strategies (Feature Request 138).


## Version 1.3

_2009-04-01_

 * Fix security token to remove the `<data>` element.
 * Changed JsonParser.parse method to be non-static
 * Throw JsonParseExceptions instead of ClassCastExceptions and UnsupportedOperationExceptions


## Version 1.3 beta3

_2009-03-17_

 * Supported custom mapping of field names by making `FieldNamingStrategy` public and allowing `FieldNamingStrategy` to be set in GsonBuilder. See issue 104.
 * Added a new GsonBuilder setting `generateNonExecutableJson()` that prefixes the generated JSON with some text to make the output non-executable Javascript. Gson now recognizes this text from input while deserializing and filters it out. This feature is meant to prevent script sourcing attacks. See Issue 42.
 * Supported deserialization of sets with elements that do not implement Comparable. See Issue 100
 * Supported deserialization of floating point numbers without a sign after E. See Issue 94


## Version 1.3 beta2

_2009-02-05_

 * Added a new Parser API. See issue 65
 * Supported deserialization of java.util.Properties. See Issue 87
 * Fixed the pretty printing of maps. See Issue 93
 * Supported automatic conversion of strings into numeric and boolean types if possible. See Issue 89
 * Supported deserialization of longs into strings. See Issue 82


## Version 1.3 beta1

_2009_01_ (Target Date Friday, Dec 15, 2008)

 * Made JSON parser lenient by allowing unquoted member names while parsing. See Issue 41
 * Better precision handling for floating points. See Issue 71, 72
 * Support for deserialization of special double values: NaN, infinity and negative infinity. See Issue 81
 * Backward compatibility issue found with serialization of `Collection<Object>` type.  See Issue 73 and 83.
 * Able to serialize null keys and/or values within a Map.  See Issue 77
 * Deserializing non-String value keys for Maps.  See Issue 85.

 * Support for clashing field name.  See Issue 76.
 * Removed the need to invoke instance creator if a deserializer is registered. See issues 37 and 69.
 * Added default support for java.util.UUID. See Issue 79
 * Changed `Gson.toJson()` methods to use `Appendable` instead of `Writer`. Issue 52. This requires that clients recompile their source code that uses Gson.


## Version 1.2.3

_2008-11-15_ (Target Date Friday, Oct 31, 2008)

 * Added support to serialize raw maps. See issue 45
 * Made Gson thread-safe by fixing Issue 63
 * Fixed Issue 68 to allow default type adapters for primitive types to be replaced by custom type adapters.
 * Relaxed the JSON parser to accept escaped slash (\/) as a valid character in the string. See Issue 66


## Version 1.2.2

_2008-10-14_ (Target Date: None, Unplanned)

 * This version was released to fix Issue 58 which caused a regression bug in version 1.2.1. It includes the contents from the release 1.2.1


## Version 1.2.1

_2008-10-13_ (Target Date Friday, Oct 7, 2008)

**Note:** This release was abandoned since it caused a regression (Issue 58) bug.

 * Includes updated parser for JSON that supports much larger strings. For example, Gson 1.2 failed at parsing a 100k string, Gson 1.2.1 has successfully parsed strings of size 15-20MB. The parser also is faster and consumes less memory since it uses a token match instead of a recursion-based Grammar production match. See Issue 47.
 * Gson now supports field names with single quotes ' in addition to double quotes ". See Issue 55.
 * Includes bug fixes for issue 46, 49, 51, 53, 54, and 56.


## Version 1.2

_2008-08-29_ (Target Date Tuesday Aug 26, 2008)

 * Includes support for feature requests 21, 24, 29
 * Includes bug fixes for Issue 22, Issue 23, Issue 25, Issue 26, Issue 32 , Issue 34, Issue 35, Issue 36, Issue 37, Issue 38, Issue 39
 * Performance enhancements (see r137)
 * Documentation updates


## Version 1.1.1

_2008-07-18_ (Target Date Friday, Aug 1, 2008)

 * Includes fixes for Issue 19, Partial fix for Issue 20


## Version 1.1

_2008-07-01_ (Target Date Thursday, July 3, 2008)

 * Includes fixes for Issue 9, Issue 16, Issue 18


## Version 1.0.1

_2008-06-17_ (Target Date Friday,  Jun 13, 2008)

 * Includes fixes for Issue 15, Issue 14, Issue 3, Issue 8
 * Javadoc improvements
