# test-jpms

This Maven module contains tests to verify that Gson's `module-info.class` which is used by the Java Platform Module System (JPMS) works properly and can be used by other projects. The module declaration file `src/test/java/module-info.java` uses Gson's module.

This is a separate Maven module to test Gson's final JAR instead of just the compiled classes.
