/*
 * Copyright (C) 2009 Google Inc.
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

/**
 * Exception class to indicate a circular reference error.
 * This class is not part of the public API and hence is not public.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
final class CircularReferenceException extends RuntimeException {
  private static final long serialVersionUID = 7444343294106513081L;
  private final Object offendingNode;

  CircularReferenceException(Object offendingNode) {
    super("circular reference error");
    this.offendingNode = offendingNode;
  }
  
  public IllegalStateException createDetailedException(FieldAttributes offendingField) {
    StringBuilder msg = new StringBuilder(getMessage());
    if (offendingField != null) {
      msg.append("\n  ").append("Offending field: ").append(offendingField.getName() + "\n");
    }
    if (offendingNode != null) {
      msg.append("\n  ").append("Offending object: ").append(offendingNode);
    }
    return new IllegalStateException(msg.toString(), this);
  }
}
