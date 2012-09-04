/*
 * Copyright (C) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.codegen;

import static java.lang.reflect.Modifier.FINAL;

import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes("com.google.gson.codegen.GeneratedTypeAdapter")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public final class GeneratedTypeAdapterProcessor extends AbstractProcessor {
  @Override public boolean process(Set<? extends TypeElement> types, RoundEnvironment env) {
    System.out.println("invoked GeneratedTypeAdapterProcessor");
    try {
      for (Element element : env.getElementsAnnotatedWith(GeneratedTypeAdapter.class)) {
        writeAdapter((TypeElement) element);
      }
    } catch (IOException e) {
      processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
    }
    return true;
  }

  private void writeAdapter(TypeElement type) throws IOException {
    String typeAdapterName = CodeGen.adapterName(type, "$TypeAdapter");
    JavaFileObject sourceFile = processingEnv.getFiler()
        .createSourceFile(typeAdapterName, type);
    System.out.println("Generating type adapter: " + typeAdapterName + " in " + sourceFile.getName());

    JavaWriter writer = new JavaWriter(sourceFile.openWriter());
    writer.addPackage(CodeGen.getPackage(type).getQualifiedName().toString());
    writer.beginType(typeAdapterName, "class", FINAL, null);
    writer.endType();
    writer.close();
  }
}
