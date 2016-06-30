package com.google.gson;

import java.lang.reflect.Field;

/**
 * Dummy NullableChecker that does not check anything.
 */
class DummyNullableChecker {
  private static NullableChecker nullableChecker = null;

  static NullableChecker getInstance() {
    if (nullableChecker == null) {
      nullableChecker = new NullableChecker() {
        @Override
        public boolean fieldIsNullable(Field field) {
          return true;
        }
      };
    }

    return nullableChecker;
  }
}
