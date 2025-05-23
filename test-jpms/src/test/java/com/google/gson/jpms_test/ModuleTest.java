/*
 * Copyright (C) 2024 Google Inc.
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

package com.google.gson.jpms_test;

import static com.google.common.truth.Truth.assertThat;

import com.google.gson.Gson;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleDescriptor.Exports;
import java.lang.module.ModuleDescriptor.Requires;
import java.net.URL;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

/**
 * Verifies that this test project is properly set up and has a module descriptor, and checks Gson's
 * module descriptor.
 */
public class ModuleTest {
  @Test
  public void testOwnModule() {
    Module module = getClass().getModule();
    assertThat(module.getName()).isEqualTo("com.google.gson.jpms_test");
  }

  @Test
  public void testGsonModule() {
    // Verify that this test actually loads the final Gson JAR, and not only compiled classes
    // Note: This might fail when run from the IDE, but should succeed when run with Maven from
    // command line
    URL gsonLocation = Gson.class.getProtectionDomain().getCodeSource().getLocation();
    assertThat(gsonLocation.getPath()).containsMatch("gson/target/gson-[^/]+\\.jar");

    Module module = Gson.class.getModule();
    ModuleDescriptor moduleDescriptor = module.getDescriptor();

    assertThat(moduleDescriptor.name()).isEqualTo("com.google.gson");
    // Permit `Modifier.SYNTHETIC`; current versions of Moditect seem to set that
    assertThat(moduleDescriptor.modifiers())
        .containsNoneOf(
            ModuleDescriptor.Modifier.AUTOMATIC,
            ModuleDescriptor.Modifier.MANDATED,
            ModuleDescriptor.Modifier.OPEN);
    // Should have implicitly included the Maven project version
    assertThat(moduleDescriptor.rawVersion()).isPresent();

    Set<Requires> moduleRequires = moduleDescriptor.requires();
    assertThat(getModuleDependencies(moduleRequires))
        .containsExactly("com.google.errorprone.annotations", "java.sql", "jdk.unsupported");
    assertThat(getTransitiveModuleDependencies(moduleRequires)).isEmpty();
    assertThat(getOptionalModuleDependencies(moduleRequires))
        .containsExactly("com.google.errorprone.annotations", "java.sql", "jdk.unsupported");

    Set<Exports> packageExports = moduleDescriptor.exports();
    assertThat(packageExports.stream().map(Exports::source))
        .containsExactly(
            "com.google.gson",
            "com.google.gson.annotations",
            "com.google.gson.reflect",
            "com.google.gson.stream");
    // Gson currently does not export packages to specific modules only
    assertThat(packageExports.stream().filter(Exports::isQualified)).isEmpty();

    // Gson does not allow access to its implementation details using reflection
    assertThat(moduleDescriptor.opens()).isEmpty();
    // Gson currently does not use or provide any services
    assertThat(moduleDescriptor.uses()).isEmpty();
    assertThat(moduleDescriptor.provides()).isEmpty();
    // Gson has no main class
    assertThat(moduleDescriptor.mainClass()).isEmpty();
  }

  private static Stream<Requires> filterImplicitRequires(Set<Requires> requires) {
    return requires.stream()
        .filter(
            r ->
                !r.modifiers().contains(Requires.Modifier.MANDATED)
                    && !r.modifiers().contains(Requires.Modifier.SYNTHETIC));
  }

  private static Set<String> getModuleDependencies(Set<Requires> requires) {
    return filterImplicitRequires(requires).map(Requires::name).collect(Collectors.toSet());
  }

  private static Set<String> getTransitiveModuleDependencies(Set<Requires> requires) {
    return filterImplicitRequires(requires)
        .filter(r -> r.modifiers().contains(Requires.Modifier.TRANSITIVE))
        .map(Requires::name)
        .collect(Collectors.toSet());
  }

  private static Set<String> getOptionalModuleDependencies(Set<Requires> requires) {
    return filterImplicitRequires(requires)
        .filter(r -> r.modifiers().contains(Requires.Modifier.STATIC))
        .map(Requires::name)
        .collect(Collectors.toSet());
  }
}
