/*
 * Copyright (C) 2008 Google Inc.
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

package com.google.gson;

import com.google.gson.annotations.JsogEnabled;
import com.google.gson.internal.bind.JsogRegistry;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * The JSOG policy decides on which classes JSOG is enabled, and what prefix
 * (if any) should be assigned for instance IDs during serialization.
 *
 * @see JsogRegistry
 * @see <a href="https://github.com/jsog/jsog">JSOG</a>
 * @author Paulo Costa
 */
public class JsogPolicy {

  /** A vanilla JsogPolicy instance */
  public static JsogPolicy DEFAULT = new JsogPolicy();

  /**
   * Checks whenever Gson should use JSOG on objects of this type
   *
   * <p>The default implementation will check for the presence of @{@link JsogEnabled}
   * annotation.</p>
   *
   * @param type Type being checked
   * @return if JSOG should be enabled for this type
   * @see #withWhitelist(Type...)
   * @see #withBlacklist(Type...)
   */
  public boolean isJsogEnabled(TypeToken type) {
    JsogEnabled annotation = (JsogEnabled)type.getRawType().getAnnotation(JsogEnabled.class);
    if (annotation != null) {
      return true;
    }

    return false;
  }

  /**
   * This can be used to assign meaningful JSOG IDs to instances during serialization.
   *
   * <p>If it returns null, IDs will be simple sequential numbers, "1", "2", "3", etc.</p>
   *
   * <p>Otherwise the id will be generated from the given prefix + sequential number. E.g.:
   * "Person-1", "Dog-1", "Person-2", "Cat-1", "Person-3".</p>
   *
   * <p>Suggested uses are class names (e.g., "Person-1") or existing identifiers (e.g., "JohnSmith-1")</p>
   *
   * @param instance
   * @return A prefix that should be added to the instance's JSOG id
   * @see #withTypePrefix()
   */
  public String getIdPrefix(Object instance) {
    return null;
  }

  /**
   * Creates a new JsogPolicy that enables JSOG for the specified types.
   *
   * <p>Other than that, it behaves exactly like this instace.</p>
   *
   * @param types Types where JSOG should be enabled
   * @return a {@link JsogPolicy} with JSOG enabled for the specified types
   */
  public JsogPolicy withWhitelist(Type... types) {
    final Set<Type> typeSet = new HashSet(Arrays.asList(types));
    return new Wrapper(this) {
      @Override
      public boolean isJsogEnabled(TypeToken type) {
        if (typeSet.contains(type.getType()) || typeSet.contains(type.getRawType())) {
          return true;
        }
        return super.isJsogEnabled(type);
      }
    };
  }

  /**
   * Creates a new JsogPolicy that disables JSOG for the specified types.
   *
   * <p>Other than that, it behaves exactly like this instace.</p>
   *
   * @param types Types where JSOG should be disabled
   * @return a {@link JsogPolicy} with JSOG disabled for the specified types
   */
  public JsogPolicy withBlacklist(Type... types) {
    final Set<Type> typeSet = new HashSet(Arrays.asList(types));
    return new Wrapper(this) {
      @Override
      public boolean isJsogEnabled(TypeToken type) {
        if (typeSet.contains(type.getType()) || typeSet.contains(type.getRawType())) {
          return false;
        }
        return super.isJsogEnabled(type);
      }
    };
  }


  /**
   * Creates a new JsogPolicy that prefixes IDs with the class name.
   *
   * <p>Examples: "Person-1", "Student-1", "Person-2", "OuterClass.Innerclass-1"</p>
   *
   * <p>Other than that, it behaves exactly like this instace.</p>
   *
   * @return a {@link JsogPolicy} which prefixes JSOG ids with the class name
   */
  public JsogPolicy withTypePrefix() {
    return new Wrapper(this) {
      @Override
      public String getIdPrefix(Object instance) {
        String prefix = instance.getClass().getName();
        int indexOfLastDot = prefix.lastIndexOf(".");
        if (indexOfLastDot != -1) {
          prefix = prefix.substring(indexOfLastDot + 1);
        }
        prefix = prefix.replaceAll("\\$", ".");
        return prefix;
      }
    };
  }

  /**
   * Creates a new JsogPolicy that enabled JSOG for all data types
   *
   * <p>Other than that, it behaves exactly like this instace.</p>
   *
   * @return a {@link JsogPolicy} that enables JSOG on all data types
   */
  public JsogPolicy withJsogAlwaysEnabled() {
    return new Wrapper(this) {
      @Override
      public boolean isJsogEnabled(TypeToken type) {
        return true;
      }
    };
  }

  /**
   * Helper class that allows easily tweaking an existing JsogPolicy by
   * overriding the target methods
   */
  private class Wrapper extends JsogPolicy {
    private final JsogPolicy wrapped;

    public Wrapper(JsogPolicy wrapped) {
      this.wrapped = wrapped;
    }

    @Override
    public boolean isJsogEnabled(TypeToken type) {
      return wrapped.isJsogEnabled(type);
    }

    @Override
    public String getIdPrefix(Object instance) {
      return wrapped.getIdPrefix(instance);
    }
  }
}
