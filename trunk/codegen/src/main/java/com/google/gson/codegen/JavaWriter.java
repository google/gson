/**
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

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Emits Java source files.
 */
public final class JavaWriter {
  private static final Pattern TYPE_PATTERN = Pattern.compile("(?:[\\w$]+\\.)*([\\w$]+)");
  private static final String INDENT = "  ";

  /** Map fully qualified type names to their short names. */
  private final Map<String, String> importedTypes = new HashMap<String, String>();

  private String packagePrefix;
  private final List<Scope> scopes = new ArrayList<Scope>();
  private final Writer out;

  /**
   * @param out the stream to which Java source will be written. This should be
   *     a buffered stream.
   */
  public JavaWriter(Writer out) {
    this.out = out;
  }

  /**
   * Emit a package declaration.
   */
  public void addPackage(String packageName) throws IOException {
    if (this.packagePrefix != null) {
      throw new IllegalStateException();
    }
    out.write("package ");
    out.write(packageName);
    out.write(";\n");
    this.packagePrefix = packageName + ".";
  }

  /**
   * Equivalent to {@code addImport(type.getName())}.
   */
  public void addImport(Class<?> type) throws IOException {
    addImport(type.getName());
  }

  /**
   * Emit an import for {@code type}. For the duration of the file, all
   * references to this class will be automatically shortened.
   */
  public void addImport(String type) throws IOException {
    Matcher matcher = TYPE_PATTERN.matcher(type);
    if (!matcher.matches()) {
      throw new IllegalArgumentException(type);
    }
    if (importedTypes.put(type, matcher.group(1)) != null) {
      throw new IllegalArgumentException(type);
    }
    out.write("import ");
    out.write(type);
    out.write(";\n");
  }

  /**
   * Emits a name like {@code java.lang.String} or {@code
   * java.util.List<java.lang.String>}, shorting it with imports if
   * possible.
   */
  private void type(String type) throws IOException {
    if (this.packagePrefix == null) {
      throw new IllegalStateException();
    }

    Matcher m = TYPE_PATTERN.matcher(type);
    int pos = 0;
    while (true) {
      boolean found = m.find(pos);

      // copy non-matching characters like "<"
      int typeStart = found ? m.start() : type.length();
      out.write(type, pos, typeStart - pos);

      if (!found) {
        break;
      }

      // copy a single class name, shortening it if possible
      String name = m.group(0);
      String imported;
      if ((imported = importedTypes.get(name)) != null) {
        out.write(imported);
      } else if (name.startsWith(packagePrefix)
          && name.indexOf('.', packagePrefix.length()) == -1) {
        out.write(name.substring(packagePrefix.length()));
      } else if (name.startsWith("java.lang.")) {
        out.write(name.substring("java.lang.".length()));
      } else {
        out.write(name);
      }
      pos = m.end();
    }
  }

  /**
   * Emits a type declaration.
   *
   * @param kind such as "class", "interface" or "enum".
   */
  public void beginType(String type, String kind, int modifiers) throws IOException {
    beginType(type, kind, modifiers, null);
  }

  /**
   * Emits a type declaration.
   *
   * @param kind such as "class", "interface" or "enum".
   * @param extendsType the class to extend, or null for no extends clause.
   */
  public void beginType(String type, String kind, int modifiers,
      String extendsType, String... implementsTypes) throws IOException {
    indent();
    modifiers(modifiers);
    out.write(kind);
    out.write(" ");
    type(type);
    if (extendsType != null) {
      out.write("\n");
      indent();
      out.write("    extends ");
      type(extendsType);
    }
    if (implementsTypes.length > 0) {
      out.write("\n");
      indent();
      out.write("    implements ");
      for (int i = 0; i < implementsTypes.length; i++) {
        if (i != 0) {
          out.write(", ");
        }
        type(implementsTypes[i]);
      }
    }
    out.write(" {\n");
    pushScope(Scope.TYPE_DECLARATION);
  }

  /**
   * Completes the current type declaration.
   */
  public void endType() throws IOException {
    if (popScope() != Scope.TYPE_DECLARATION) {
      throw new IllegalStateException();
    }
    indent();
    out.write("}\n");
  }

  /**
   * Emits a field declaration.
   */
  public void field(String type, String name, int modifiers) throws IOException {
    field(type, name, modifiers, null);
  }

  public void field(String type, String name, int modifiers, String initialValue)
      throws IOException {
    indent();
    modifiers(modifiers);
    type(type);
    out.write(" ");
    out.write(name);

    if (initialValue != null) {
      out.write(" = ");
      out.write(initialValue);
    }
    out.write(";\n");
  }

  /**
   * Emit a method declaration.
   *
   * @param returnType the method's return type, or null for constructors.
   * @param parameters alternating parameter types and names.
   * @param name the method name, or the fully qualified class name for
   *     constructors.
   */
  public void beginMethod(String returnType, String name, int modifiers, String... parameters)
      throws IOException {
    indent();
    modifiers(modifiers);
    if (returnType != null) {
      type(returnType);
      out.write(" ");
      out.write(name);
    } else {
      type(name);
    }
    out.write("(");
    for (int p = 0; p < parameters.length; ) {
      if (p != 0) {
        out.write(", ");
      }
      type(parameters[p++]);
      out.write(" ");
      type(parameters[p++]);
    }
    out.write(")");
    if ((modifiers & Modifier.ABSTRACT) != 0) {
      out.write(";\n");
      pushScope(Scope.ABSTRACT_METHOD);
    } else {
      out.write(" {\n");
      pushScope(Scope.NON_ABSTRACT_METHOD);
    }
  }

  /**
   * Annotates the next element with {@code annotation}. The annotation has no
   * attributes.
   */
  public void annotation(String annotation) throws IOException {
    indent();
    out.write("@");
    type(annotation);
    out.write("\n");
  }

  /**
   * Equivalent to {@code annotation(annotationType.getName())}.
   */
  public void annotation(Class<? extends Annotation> annotationType) throws IOException {
    annotation(annotationType.getName());
  }

  /**
   * @param pattern a code pattern like "int i = %s". Shouldn't contain a
   * trailing semicolon or newline character.
   */
  public void statement(String pattern, Object... args) throws IOException {
    checkInMethod();
    indent();
    out.write(String.format(pattern, args));
    out.write(";\n");
  }

  /**
   * @param controlFlow the control flow construct and its code, such as
   *     "if (foo == 5)". Shouldn't contain braces or newline characters.
   */
  public void beginControlFlow(String controlFlow) throws IOException {
    checkInMethod();
    indent();
    out.write(controlFlow);
    out.write(" {\n");
    pushScope(Scope.CONTROL_FLOW);
  }

  /**
   * @param controlFlow the control flow construct and its code, such as
   *     "else if (foo == 10)". Shouldn't contain braces or newline characters.
   */
  public void nextControlFlow(String controlFlow) throws IOException {
    if (popScope() != Scope.CONTROL_FLOW) {
      throw new IllegalArgumentException();
    }

    indent();
    pushScope(Scope.CONTROL_FLOW);
    out.write("} ");
    out.write(controlFlow);
    out.write(" {\n");
  }

  public void endControlFlow() throws IOException {
    endControlFlow(null);
  }

  /**
   * @param controlFlow the optional control flow construct and its code, such
   *     as "while(foo == 20)". Only used for "do/while" control flows.
   */
  public void endControlFlow(String controlFlow) throws IOException {
    if (popScope() != Scope.CONTROL_FLOW) {
      throw new IllegalArgumentException();
    }

    indent();
    if (controlFlow != null) {
      out.write("} ");
      out.write(controlFlow);
      out.write(";\n");
    } else {
      out.write("}\n");
    }
  }

  /**
   * Completes the current method declaration.
   */
  public void endMethod() throws IOException {
    Scope popped = popScope();
    if (popped == Scope.NON_ABSTRACT_METHOD) {
      indent();
      out.write("}\n");
    } else if (popped != Scope.ABSTRACT_METHOD) {
      throw new IllegalStateException();
    }
  }

  /**
   * Returns the string literal representing {@code data}, including wrapping
   * quotes.
   */
  public static String stringLiteral(String data) {
    StringBuilder result = new StringBuilder();
    result.append('"');
    for (int i = 0; i < data.length(); i++) {
      char c = data.charAt(i);
      switch (c) {
        case '"':
          result.append("\\\"");
          break;
        case '\\':
          result.append("\\\\");
          break;
        case '\t':
          result.append("\\\t");
          break;
        case '\b':
          result.append("\\\b");
          break;
        case '\n':
          result.append("\\\n");
          break;
        case '\r':
          result.append("\\\r");
          break;
        case '\f':
          result.append("\\\f");
          break;
        default:
          result.append(c);
      }
    }
    result.append('"');
    return result.toString();
  }

  public void close() throws IOException {
    out.close();
  }

  /**
   * Emit modifier names.
   */
  private void modifiers(int modifiers) throws IOException {
    if ((modifiers & Modifier.PUBLIC) != 0) {
      out.write("public ");
    }
    if ((modifiers & Modifier.PRIVATE) != 0) {
      out.write("private ");
    }
    if ((modifiers & Modifier.PROTECTED) != 0) {
      out.write("protected ");
    }
    if ((modifiers & Modifier.STATIC) != 0) {
      out.write("static ");
    }
    if ((modifiers & Modifier.FINAL) != 0) {
      out.write("final ");
    }
    if ((modifiers & Modifier.ABSTRACT) != 0) {
      out.write("abstract ");
    }
    if ((modifiers & Modifier.SYNCHRONIZED) != 0) {
      out.write("synchronized ");
    }
    if ((modifiers & Modifier.TRANSIENT) != 0) {
      out.write("transient ");
    }
    if ((modifiers & Modifier.VOLATILE) != 0) {
      out.write("volatile ");
    }
  }

  private void indent() throws IOException {
    for (int i = 0; i < scopes.size(); i++) {
      out.write(INDENT);
    }
  }

  private void checkInMethod() {
    Scope scope = peekScope();
    if (scope != Scope.NON_ABSTRACT_METHOD && scope != Scope.CONTROL_FLOW) {
      throw new IllegalArgumentException();
    }
  }

  private void pushScope(Scope pushed) {
    scopes.add(pushed);
  }

  private Scope peekScope() {
    return scopes.get(scopes.size() - 1);
  }

  private Scope popScope() {
    return scopes.remove(scopes.size() - 1);
  }

  private enum Scope {
    TYPE_DECLARATION,
    ABSTRACT_METHOD,
    NON_ABSTRACT_METHOD,
    CONTROL_FLOW,
  }
}
