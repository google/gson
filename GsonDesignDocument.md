# Gson Design Document

This document presents issues that we faced while designing Gson. It is meant for advanced users or developers working on Gson. If you are interested in learning how to use Gson, see its user guide. 

**Navigating the Json tree or the target Type Tree while deserializing**

When you are deserializing a Json string into an object of desired type, you can either navigate the tree of the input, or the type tree of the desired type. Gson uses the latter approach of navigating the type of the target object. This keeps you in tight control of instantiating only the type of objects that you are expecting (essentially validating the input against the expected "schema"). By doing this, you also ignore any extra fields that the Json input has but were not expected. 

As part of Gson, we wrote a general purpose ObjectNavigator that can take any object and navigate through its fields calling a visitor of your choice. 

**Supporting richer serialization semantics than deserialization semantics**

Gson supports serialization of arbitrary collections, but can only deserialize genericized collections. this means that Gson can, in some cases, fail to deserialize Json that it wrote. This is primarily a limitation of the Java type system since when you encounter a Json array of arbitrary types there is no way to detect the types of individual elements. We could have chosen to restrict the serialization to support only generic collections, but chose not to.This is because often the user of the library are concerned with either serialization or deserialization, but not both. In such cases, there is no need to artificially restrict the serialization capabilities. 

**Supporting serialization and deserialization of classes that are not under your control and hence can not be modified**

Some Json libraries use annotations on fields or methods to indicate which fields should be used for Json serialization. That approach essentially precludes the use of classes from JDK or third-party libraries. We solved this problem by defining the notion of Custom serializers and deserializers. This approach is not new, and was used by the JAX-RPC technology to solve essentially the same problem. 

**Using Checked vs Unchecked exceptions to indicate a parsing error**

We chose to use unchecked exceptions to indicate a parsing failure. This is primarily done because usually the client can not recover from bad input, and hence forcing them to catch a checked exception results in sloppy code in the catch() block. 

**Creating class instances for deserialization**

Gson needs to create a dummy class instance before it can deserialize Json data into its fields. We could have used Guice to get such an instance, but that would have resulted in a dependency on Guice. Moreover, it probably would have done the wrong thing since Guice is expected to return a valid instance, whereas we need to create a dummy one. Worse, Gson would overwrite the fields of that instance with the incoming data there by modifying the instance for all subsequent Guice injections. This is clearly not a desired behavior. Hence, we create class instances by invoking the parameterless constructor. We also handle the primitive types, enums, collections, sets, maps and trees as a special case. 

To solve the problem of supporting unmodifiable types, we use custom instance creators. So, if you want to use a library types that does not define a default constructor (for example, Money class), then you can register an instance creator that returns a dummy instance when asked.

**Using fields vs getters to indicate Json elements**

Some Json libraries use the getters of a type to deduce the Json elements. We chose to use all fields (up the inheritance hierarchy) that are not transient, static, or synthetic. We did this because not all classes are written with suitably named getters. Moreover, getXXX or isXXX might be semantic rather than indicating properties. 

However, there are good arguments to support properties as well. We intend to enhance Gson in a latter version to support properties as an alternate mapping for indicating Json fields. For now, Gson is fields-based. 

**Why are most classes in Gson marked as final?**

While Gson provides a fairly extensible architecture by providing pluggable serializers and deserializers, Gson classes were not specifically designed to be extensible. Providing non-final classes would have allowed a user to legitimately extend Gson classes, and then expect that behavior to work in all subsequent revisions. We chose to limit such use-cases by marking classes as final, and waiting until a good use-case emerges to allow extensibility. Marking a class final also has a minor benefit of providing additional optimization opportunities to Java compiler and virtual machine. 

**Why are inner interfaces and classes used heavily in Gson?**

Gson uses inner classes substantially. Many of the public interfaces are inner interfaces too (see JsonSerializer.Context or JsonDeserializer.Context as an example). These are primarily done as a matter of style. For example, we could have moved JsonSerializer.Context to be a top-level class JsonSerializerContext, but chose not to do so. However, if you can give us good reasons to rename it alternately, we are open to changing this philosophy. 

**Why do you provide two ways of constructing Gson?**

Gson can be constructed in two ways: by invoking new Gson() or by using a GsonBuilder. We chose to provide a simple no-args constructor to handle simple use-cases for Gson where you want to use default options, and quickly want to get going with writing code. For all other situations, where you need to configure Gson with options such as formatters, version controls etc, we use a builder pattern. The builder pattern allows a user to specify multiple optional settings for what essentially become constructor parameters for Gson. 

**Comparing Gson with Alternate Approaches**

Note that these comparisons were done while developing Gson so these date back to mid to late 2007.

__Comparing Gson with org.json library__

org.json is a much lower-level library that can be used to write a toJson() method in a class. If you can not use Gson directly (may be because of platform restrictions regarding reflection), you could use org.json to hand-code a toJson method in each object. 

__Comparing Gson with org.json.simple library__

org.json.simple library is very similar to org.json library and hence fairly low level. The key issue with this library is that it does not handle exceptions very well. In some cases it appeared to just eat the exception while in other cases it throws an "Error" rather than an exception.
