# Extend the ProGuard rules
-include proguard.pro

### The following rules are needed for R8 in "full mode", which performs more aggressive optimizations than ProGuard
### See https://r8.googlesource.com/r8/+/refs/heads/master/compatibility-faq.md#r8-full-mode

# Keep the no-args constructor of classes referenced by @JsonAdapter
-keep class com.example.ClassWithAdapter$Adapter {
  <init>();
}
-keep class com.example.ClassWithAnnotations$DummyAdapter {
  <init>();
}

# Keep the no-args constructor of deserialized class
-keep class com.example.ClassWithDefaultConstructor {
  <init>();
}

# Don't obfuscate class name, to check it in exception message
-keep,allowshrinking,allowoptimization class com.example.DefaultConstructorMain$TestClass

# Keep enum constants which are not explicitly used in code
-keep class com.example.EnumClass {
  ** SECOND;
}
