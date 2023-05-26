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
}


### Test data setup

# Keep fields without annotations which should be preserved
-keepclassmembers class com.example.ClassWithNamedFields {
  !transient <fields>;
}


### TODO: Move these to `META-INF/proguard`
# Keep generic signatures; needed for correct type resolution
-keepattributes Signature

# Keep Gson annotations
# Note: Cannot perform finer selection here to only cover Gson annotations, see also https://stackoverflow.com/q/47515093
-keepattributes *Annotation*


### The following rules are needed for R8 in "full mode" which only adheres to `-keepattribtues` if
### the corresponding class or field is matches by a `-keep` rule as well, see
### https://r8.googlesource.com/r8/+/refs/heads/master/compatibility-faq.md#r8-full-mode

# Keep class TypeToken (respectively its generic signature)
-keep class com.google.gson.reflect.TypeToken { *; }

# Keep any (anonymous) classes extending TypeToken
-keep class * extends com.google.gson.reflect.TypeToken

# Keep classes with @JsonAdapter annotation
-keep @com.google.gson.annotations.JsonAdapter class *

# Keep fields with @SerializedName annotation, but allow obfuscation of their names
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Keep fields with any other Gson annotation
-keepclassmembers class * {
  @com.google.gson.annotations.Expose <fields>;
  @com.google.gson.annotations.JsonAdapter <fields>;
  @com.google.gson.annotations.Since <fields>;
  @com.google.gson.annotations.Until <fields>;
}
