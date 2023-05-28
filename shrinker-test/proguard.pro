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
-keep class com.example.DefaultConstructorMain {
  public static java.lang.String runTest();
  public static java.lang.String runTestNoJdkUnsafe();
}


### Test data setup

# Keep fields without annotations which should be preserved
-keepclassmembers class com.example.ClassWithNamedFields {
  !transient <fields>;
}
