### Common rules for ProGuard and R8
### Should only contains rules needed specifically for the integration test;
### any general rules which are relevant for all users should not be here but in `META-INF/proguard` of Gson

-allowaccessmodification

# On Windows mixed case class names might cause problems
-dontusemixedcaseclassnames

# Ignore notes about duplicate JDK classes
-dontnote module-info,jdk.internal.**


# Keep test entrypoints
-keep class com.example.Main {
  public static void runTests(...);
}
-keep class com.example.NoSerializedNameMain {
  public static java.lang.String runTestNoArgsConstructor();
  public static java.lang.String runTestNoJdkUnsafe();
  public static java.lang.String runTestHasArgsConstructor();
}


### Test data setup

# Keep fields without annotations which should be preserved
-keepclassmembers class com.example.ClassWithNamedFields {
  !transient <fields>;
}

-keepclassmembernames class com.example.ClassWithExposeAnnotation {
  <fields>;
}
-keepclassmembernames class com.example.ClassWithJsonAdapterAnnotation {
  ** f;
}
-keepclassmembernames class com.example.ClassWithVersionAnnotations {
  <fields>;
}

# Keep the name of the class to allow using reflection to check if this class still exists
# after shrinking
-keepnames class com.example.UnusedClass
