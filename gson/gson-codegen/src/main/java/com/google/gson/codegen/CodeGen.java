/*
 * Copyright (C) 2012 Square, Inc.
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

package com.google.gson.codegen;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

public class CodeGen {
  private CodeGen() {
  }

  public static PackageElement getPackage(Element type) {
    while (type.getKind() != ElementKind.PACKAGE) {
      type = type.getEnclosingElement();
    }
    return (PackageElement) type;
  }

  /**
   * Returns a fully qualified class name to complement {@code type}.
   */
  public static String adapterName(TypeElement typeElement, String suffix) {
    StringBuilder builder = new StringBuilder();
    rawTypeToString(builder, typeElement, '$');
    builder.append(suffix);
    return builder.toString();
  }

  static void rawTypeToString(StringBuilder result, TypeElement type, char innerClassSeparator) {
    String packageName = getPackage(type).getQualifiedName().toString();
    String qualifiedName = type.getQualifiedName().toString();
    result.append(packageName);
    result.append('.');
    result.append(
        qualifiedName.substring(packageName.length() + 1).replace('.', innerClassSeparator));
  }
}
