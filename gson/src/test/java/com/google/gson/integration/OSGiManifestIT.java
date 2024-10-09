/*
 * Copyright (C) 2024 Google Inc.
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
package com.google.gson.integration;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.Assert.fail;

import com.google.common.base.Splitter;
import com.google.gson.internal.GsonBuildConfig;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;

/**
 * Performs assertions on the generated OSGi manifest attributes. This is an integration test
 * ({@code *IT.java}) intended to be run against the final JAR.
 *
 * <p>Note: These tests must be run on the command line with {@code mvn clean verify}, running them
 * in the IDE will not work because it won't use the generated JAR, and additionally the
 * bnd-maven-plugin seems to behave differently in the IDE (at least Eclipse), adding unexpected
 * {@code Import-Package} entries for JDK classes (<a
 * href="https://github.com/bndtools/bnd/issues/6258>bnd-maven-plugin issue</a>).<br>
 * Running Maven's {@code clean} phase is necessary due to a <a
 * href="https://github.com/bndtools/bnd/issues/6221">bnd-maven-plugin bug</a>.
 */
@SuppressWarnings("MemberName") // class name must end with 'IT' for Maven Failsafe Plugin
public class OSGiManifestIT {
  private static class ManifestData {
    public final URL url;
    public final Manifest manifest;

    public ManifestData(URL url, Manifest manifest) {
      this.url = url;
      this.manifest = manifest;
    }
  }

  private static final String GSON_VERSION = GsonBuildConfig.VERSION;
  private Attributes manifestAttributes;

  @Before
  public void getGsonManifestAttributes() throws Exception {
    ManifestData manifestData = findManifest("com.google.gson");
    // Make sure manifest was loaded from final Gson JAR (and not intermediate manifest is used)
    assertWithMessage(
            "Should load manifest from Gson JAR file; run this test with `mvn clean verify` on"
                + " command line and not from IDE")
        .that(manifestData.url.toString())
        .endsWith(".jar!/META-INF/MANIFEST.MF");

    manifestAttributes = manifestData.manifest.getMainAttributes();
  }

  private String getAttribute(String name) {
    return manifestAttributes.getValue(name);
  }

  @Test
  public void testBundleInformation() {
    assertThat(getAttribute("Bundle-SymbolicName")).isEqualTo("com.google.gson");
    assertThat(getAttribute("Bundle-Name")).isEqualTo("Gson");
    assertThat(getAttribute("Bundle-License"))
        .isEqualTo("\"Apache-2.0\";link=\"https://www.apache.org/licenses/LICENSE-2.0.txt\"");
    assertThat(getAttribute("Bundle-Version"))
        .isEqualTo(GSON_VERSION.replace("-SNAPSHOT", ".SNAPSHOT"));
  }

  @Test
  public void testImports() throws Exception {
    // Keep only 'major.minor', drop the 'patch' version
    String errorProneVersion =
        shortenVersionNumber(
            findManifest("com.google.errorprone.annotations")
                .manifest
                .getMainAttributes()
                .getValue("Bundle-Version"),
            1);
    String nextMajorErrorProneVersion = increaseVersionNumber(errorProneVersion, 0);
    String errorProneVersionRange =
        "[" + errorProneVersion + "," + nextMajorErrorProneVersion + ")";

    List<String> imports = splitPackages(getAttribute("Import-Package"));
    // If imports contains `java.*`, then either user started from IDE, or IDE rebuilt project while
    // Maven build was running, see https://github.com/bndtools/bnd/issues/6258
    if (imports.stream().anyMatch(i -> i.startsWith("java."))) {
      fail(
          "Test must be run from command line with `mvn clean verify`; additionally make sure your"
              + " IDE did not rebuild the project in the meantime");
    }

    assertThat(imports)
        .containsExactly(
            // Dependency on JDK's sun.misc.Unsafe should be optional
            "sun.misc;resolution:=optional",
            "com.google.errorprone.annotations;version=\"" + errorProneVersionRange + "\"");

    // Should not contain any import for Gson's own packages, see
    // https://github.com/google/gson/pull/2735#issuecomment-2330047410
    for (String importedPackage : imports) {
      assertThat(importedPackage).doesNotContain("com.google.gson");
    }
  }

  @Test
  public void testExports() {
    String gsonVersion = GSON_VERSION.replace("-SNAPSHOT", "");

    List<String> exports = splitPackages(getAttribute("Export-Package"));
    // When not running `mvn clean` the exports might differ slightly, see
    // https://github.com/bndtools/bnd/issues/6221
    assertWithMessage("Unexpected exports; make sure you are running `mvn clean ...`")
        .that(exports)
        // Note: This just represents the currently generated exports; especially the `uses` can be
        // adjusted if necessary when Gson's implementation changes
        .containsExactly(
            "com.google.gson;uses:=\"com.google.gson.reflect,com.google.gson.stream\";version=\""
                + gsonVersion
                + "\"",
            "com.google.gson.annotations;version=\"" + gsonVersion + "\"",
            "com.google.gson.reflect;version=\"" + gsonVersion + "\"",
            "com.google.gson.stream;uses:=\"com.google.gson\";version=\"" + gsonVersion + "\"");
  }

  @Test
  public void testRequireCapability() {
    String expectedJavaVersion = "1.8";

    // Defines the minimum required Java version
    assertThat(getAttribute("Require-Capability"))
        .isEqualTo("osgi.ee;filter:=\"(&(osgi.ee=JavaSE)(version=" + expectedJavaVersion + "))\"");

    // Should not define deprecated "Bundle-RequiredExecutionEnvironment"
    assertThat(getAttribute("Bundle-RequiredExecutionEnvironment")).isNull();
  }

  private ManifestData findManifest(String bundleName) throws IOException {
    List<URL> manifestResources =
        Collections.list(getClass().getClassLoader().getResources("META-INF/MANIFEST.MF"));

    for (URL manifestResource : manifestResources) {
      Manifest manifest;
      try (InputStream is = manifestResource.openStream()) {
        manifest = new Manifest(is);
      }
      if (bundleName.equals(manifest.getMainAttributes().getValue("Bundle-SymbolicName"))) {
        return new ManifestData(manifestResource, manifest);
      }
    }

    fail(
        "Cannot find "
            + bundleName
            + " OSGi bundle manifest among: "
            + manifestResources
            + "\nRun this test with `mvn clean verify` on command line and not from the IDE.");
    return null;
  }

  /** Splits a list of packages separated by {@code ','}. */
  private List<String> splitPackages(String packagesString) {
    List<String> splitPackages = new ArrayList<>();
    int nextSplitStart = 0;
    boolean isInQuotes = false;

    for (int i = 0; i < packagesString.length(); i++) {
      char c = packagesString.charAt(i);
      // Ignore ',' inside quotes
      if (c == '"') {
        isInQuotes = !isInQuotes;
      } else if (c == ',' && !isInQuotes) {
        splitPackages.add(packagesString.substring(nextSplitStart, i));
        nextSplitStart = i + 1; // skip past the ','
      }
    }

    // Add package behind last ','
    splitPackages.add(packagesString.substring(nextSplitStart));
    return splitPackages;
  }

  /**
   * Shortens a version number by dropping lower parts. For example {@code 1.2.3 -> 1.2} (when
   * {@code keepPosition = 1}).
   *
   * @param versionString e.g. "1.2.3"
   * @param keepPosition position of the version to keep: 0 = major, 1 = minor, ...
   * @return shortened version number
   */
  private String shortenVersionNumber(String versionString, int keepPosition) {
    return Splitter.on('.')
        .splitToStream(versionString)
        .limit(keepPosition + 1)
        .collect(Collectors.joining("."));
  }

  /**
   * Increases part of a version number (and drops lower parts). For example {@code 1.2.3 -> 1.3}
   * (when {@code position = 1}).
   *
   * @param versionString e.g. "1.2.3"
   * @param position position of the version to increase: 0 = major, 1 = minor, ...
   * @return increased version number
   */
  private String increaseVersionNumber(String versionString, int position) {
    List<Integer> splitVersion = new ArrayList<>();
    for (String versionPiece : Splitter.on('.').split(versionString)) {
      splitVersion.add(Integer.valueOf(versionPiece));
    }
    // Drop lower version parts
    splitVersion = splitVersion.subList(0, position + 1);
    // Increase version number
    splitVersion.set(position, splitVersion.get(position) + 1);

    return splitVersion.stream().map(i -> i.toString()).collect(Collectors.joining("."));
  }
}
