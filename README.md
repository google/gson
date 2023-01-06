# Gson

Gson is a Java library that can be used to convert Java Objects into their JSON representation. It can also be used to convert a JSON string to an equivalent Java object.
Gson can work with arbitrary Java objects including pre-existing objects that you do not have source-code of.

There are a few open-source projects that can convert Java objects to JSON. However, most of them require that you place Java annotations in your classes; something that you can not do if you do not have access to the source-code. Most also do not fully support the use of Java Generics. Gson considers both of these as very important design goals.

:information_source: Gson is currently in maintenance mode; existing bugs will be fixed, but large new features will likely not be added. If you want to add a new feature, please first search for existing GitHub issues, or create a new one to discuss the feature and get feedback.

### Goals
  * Provide simple `toJson()` and `fromJson()` methods to convert Java objects to JSON and vice-versa
  * Allow pre-existing unmodifiable objects to be converted to and from JSON
  * Extensive support of Java Generics
  * Allow custom representations for objects
  * Support arbitrarily complex objects (with deep inheritance hierarchies and extensive use of generic types)

### Download

Gradle:
```gradle
dependencies {
  implementation 'com.google.code.gson:gson:2.10.1'
}
```

Maven:
```xml
<dependency>
  <groupId>com.google.code.gson</groupId>
  <artifactId>gson</artifactId>
  <version>2.10.1</version>
</dependency>
```

[Gson jar downloads](https://maven-badges.herokuapp.com/maven-central/com.google.code.gson/gson) are available from Maven Central.

![Build Status](https://github.com/google/gson/actions/workflows/build.yml/badge.svg)

### Requirements
#### Minimum Java version
- Gson 2.9.0 and newer: Java 7
- Gson 2.8.9 and older: Java 6

Despite supporting older Java versions, Gson also provides a JPMS module descriptor (module name `com.google.gson`) for users of Java 9 or newer.

#### JPMS dependencies (Java 9+)
These are the optional Java Platform Module System (JPMS) JDK modules which Gson depends on.
This only applies when running Java 9 or newer.

- `java.sql` (optional since Gson 2.8.9)  
When this module is present, Gson provides default adapters for some SQL date and time classes.

- `jdk.unsupported`, respectively class `sun.misc.Unsafe` (optional)  
When this module is present, Gson can use the `Unsafe` class to create instances of classes without no-args constructor.
However, care should be taken when relying on this. `Unsafe` is not available in all environments and its usage has some pitfalls,
see [`GsonBuilder.disableJdkUnsafe()`](https://javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/GsonBuilder.html#disableJdkUnsafe()).

### Documentation
  * [API Javadoc](https://www.javadoc.io/doc/com.google.code.gson/gson): Documentation for the current release
  * [User guide](UserGuide.md): This guide contains examples on how to use Gson in your code
  * [Troubleshooting guide](Troubleshooting.md): Describes how to solve common issues when using Gson
  * [Change log](CHANGELOG.md): Changes in the recent versions
  * [Design document](GsonDesignDocument.md): This document discusses issues we faced while designing Gson. It also includes a comparison of Gson with other Java libraries that can be used for Json conversion

Please use the ['gson' tag on StackOverflow](https://stackoverflow.com/questions/tagged/gson) or the [google-gson Google group](https://groups.google.com/group/google-gson) to discuss Gson or to post questions.

### Related Content Created by Third Parties
  * [Gson Tutorial](https://www.studytrails.com/java/json/java-google-json-introduction/) by `StudyTrails`
  * [Gson Tutorial Series](https://futurestud.io/tutorials/gson-getting-started-with-java-json-serialization-deserialization) by `Future Studio`
  * [Gson API Report](https://abi-laboratory.pro/java/tracker/timeline/gson/)

### License

Gson is released under the [Apache 2.0 license](LICENSE).

```
Copyright 2008 Google Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

### Disclaimer

This is not an officially supported Google product.
