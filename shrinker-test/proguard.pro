# Include common rules
-include common.pro

### ProGuard specific rules

-keep class com.example.DefaultConstructorMain$TestClassWithoutDefaultConstructor {
  <fields>;
}
