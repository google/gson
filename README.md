# google-gson
Gson is a Java library that can be used to convert Java Objects into their JSON representation. It can also be used to convert a JSON string to an equivalent Java object.
Gson can work with arbitrary Java objects including pre-existing objects that you do not have source-code of. 

There are a few open-source projects that can convert Java objects to JSON. However, most of them require that you place Java annotations in your classes; something that you can not do if you do not have access to the source-code. Most also do not fully support the use of Java Generics. Gson considers both of these as very important design goals. 

*Gson Goals*
  * Provide simple `toJson()` and `fromJson()` methods to convert Java objects to JSON and vice-versa
  * Allow pre-existing unmodifiable objects to be converted to and from JSON
  * Extensive support of Java Generics
  * Allow custom representations for objects
  * Support arbitrarily complex objects (with deep inheritance hierarchies and extensive use of generic types)

*Gson Downloads*
  * [Gson 2.5 Download](http://search.maven.org/#artifactdetails%7Ccom.google.code.gson%7Cgson%7C2.5%7Cjar) downloads at Maven Central

*Gson Documentation*
  * Gson [API](http://google.github.io/gson/apidocs/): Javadocs for the current Gson release
  * Gson [user guide](https://github.com/google/gson/blob/master/UserGuide.md): This guide contains examples on how to use Gson in your code.
  * Gson [Roadmap](https://github.com/google/gson/blob/master/CHANGELOG.md): Details of changes in the recent versions
  * Gson [design document](https://github.com/google/gson/blob/master/GsonDesignDocument.md): This document discusses issues we faced while designing Gson. It also include a comparison of Gson with other Java libraries that can be used for Json conversion

Please use the [google-gson Google group](http://groups.google.com/group/google-gson) to discuss Gson, or to post questions. 

*Gson-related Content Created by Third Parties*
  * [Gson Tutorial](http://www.studytrails.com/java/json/java-google-json-introduction.jsp) by `StudyTrails`
