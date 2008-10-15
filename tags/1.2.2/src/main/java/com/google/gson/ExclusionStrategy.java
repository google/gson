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

import java.lang.reflect.Field;

/**
 * A strategy definition that is used by the {@link ObjectNavigator} to
 * determine whether or not the field of the object should be ignored during
 * navigation.
 *
 * As well, for now this class is also responsible for excluding entire
 * classes.  This is somewhat a mixing of concerns for this object, but
 * it will suffice for now.  We can always break it down into two
 * different strategies later.
 *
 * @author Joel Leitch
 */
interface ExclusionStrategy {

  /**
   * @param f the field object that is under test
   * @return true if the field should be ignored otherwise false
   */
  public boolean shouldSkipField(Field f);

  /**
   * @param clazz the class object that is under test
   * @return true if the class should be ignored otherwise false
   */
  public boolean shouldSkipClass(Class<?> clazz);
}
