# android-proguard-example

Example Android project showing how to properly configure [ProGuard](https://www.guardsquare.com/proguard).
ProGuard is a tool for 'shrinking' and obfuscating compiled classes. It can rename methods and fields,
or remove them if they appear to be unused. This can cause issues for Gson which uses Java reflection to
access the fields of a class. It is necessary to configure ProGuard to make sure that Gson works correctly.

Also have a look at the [ProGuard manual](https://www.guardsquare.com/manual/configuration/usage#keepoverview)
for more details on how ProGuard can be configured.

The R8 code shrinker uses the same rule format as ProGuard, but there are differences between these two
tools. Have a look at R8's Compatibility FAQ, and especially at the [Gson section](https://r8.googlesource.com/r8/+/refs/heads/main/compatibility-faq.md#gson).
