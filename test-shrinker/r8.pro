# Include common rules
-include common.pro

### The following rules are needed for R8 in "full mode", which performs more aggressive optimizations than ProGuard
### See https://r8.googlesource.com/r8/+/refs/heads/main/compatibility-faq.md#r8-full-mode

# For classes with generic type parameter R8 in "full mode" requires to have a keep rule to
# preserve the generic signature
-keep,allowshrinking,allowoptimization,allowobfuscation,allowaccessmodification class com.example.GenericClasses$GenericClass
-keep,allowshrinking,allowoptimization,allowobfuscation,allowaccessmodification class com.example.GenericClasses$GenericUsingGenericClass

# Don't obfuscate class name, to check it in exception message
-keep,allowshrinking,allowoptimization class com.example.NoSerializedNameMain$TestClassNoArgsConstructor
-keep,allowshrinking,allowoptimization class com.example.NoSerializedNameMain$TestClassHasArgsConstructor

# This rule has the side-effect that R8 still removes the no-args constructor, but does not make the class abstract
-keep class com.example.NoSerializedNameMain$TestClassNotAbstract {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Keep enum constants which are not explicitly used in code
-keepclassmembers class com.example.EnumClass {
  ** SECOND;
}
