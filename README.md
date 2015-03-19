# google-gson
Gson is a Java library that can be used to convert Java Objects into their JSON representation. It can also be used to convert a JSON string to an equivalent Java object.
Gson can work with arbitrary Java objects including pre-existing objects that you do not have source-code of. 

There are a few open-source projects that can convert Java objects to JSON. However, most of them require that you place Java annotations in your classes; something that you can not do if you do not have access to the source-code. Most also do not fully support the use of Java Generics. Gson considers both of these as very important design goals. 

*Gson Goals*
  * Provide simple toJson() and fromJson() methods to convert Java objects to JSON and vice-versa
  * Allow pre-existing unmodifiable objects to be converted to and from JSON
  * Extensive support of Java Generics
  * Allow custom representations for objects
  * Support arbitrarily complex objects (with deep inheritance hierarchies and extensive use of generic types)

*Gson Downloads*
  * Gson 2.3.1 [http://search.maven.org/#artifactdetails%7Ccom.google.code.gson%7Cgson%7C2.3.1%7Cjar downloads at Maven Central]

*Gson Documentation*
  * Gson [http://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/index.html API]: Javadocs for the current Gson release
  * Gson [http://sites.google.com/site/gson/gson-user-guide user guide]: This guide contains examples on how to use Gson in your code.
  * Gson [https://sites.google.com/site/gson/gson-roadmap Roadmap]: Details on upcoming releases 
  * Gson [https://sites.google.com/site/gson/gson-design-document design document]: This document discusses issues we faced while designing Gson. It also include a comparison of Gson with other Java libraries that can be used for Json conversion

Please use the [http://groups.google.com/group/google-gson google-gson Google Group] to discuss Gson, or to post questions. 

*Gson-related Content Created by Third Parties*
  * [http://www.studytrails.com/java/json/java-google-json-introduction.jsp Gson Tutorial] by `StudyTrails`
