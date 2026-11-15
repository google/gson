/*
 * Copyright (C) 2026 The Gson Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.internal.bind;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertSame;

import com.google.gson.JsonIOException;
import com.google.gson.internal.reflect.ReflectionHelper;
import java.lang.reflect.Field;
import org.junit.Test;

/** Tests for the helper used by {@link ReflectiveTypeAdapterFactory} for final-field mutation. */
public final class ReflectiveTypeAdapterFactoryFinalFieldTest {

  private static final class ClassWithFinalField {
    @SuppressWarnings("unused")
    final String finalField;

    ClassWithFinalField(String finalField) {
      this.finalField = finalField;
    }
  }

  @Test
  public void createExceptionForFinalFieldMutation_includesHelpfulMessageAndJepLink()
      throws NoSuchFieldException {
    Field finalField = ClassWithFinalField.class.getDeclaredField("finalField");
    IllegalAccessException cause = new IllegalAccessException("test");

    JsonIOException exception =
        ReflectionHelper.createExceptionForFinalFieldMutation(finalField, cause);

    assertThat(exception).hasMessageThat().contains("Cannot set value of final");
    assertThat(exception).hasMessageThat().contains("https://openjdk.org/jeps/500");
    assertSame(cause, exception.getCause());
  }
}
