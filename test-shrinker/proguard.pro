# Include common rules
-include common.pro

### ProGuard specific rules

# Unlike R8, ProGuard does not perform aggressive optimization which makes classes abstract,
# therefore for ProGuard can successfully perform deserialization, and for that need to
# preserve the field names
-keepclassmembernames class com.example.NoSerializedNameMain$TestClassNoArgsConstructor {
  <fields>;
}
-keepclassmembernames class com.example.NoSerializedNameMain$TestClassNotAbstract {
  <fields>;
}
-keepclassmembernames class com.example.NoSerializedNameMain$TestClassHasArgsConstructor {
  <fields>;
}
