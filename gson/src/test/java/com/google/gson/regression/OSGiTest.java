/*
 * Copyright (C) 2016 Google Inc.
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
package com.google.gson.regression;

import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.Manifest;
import org.junit.Test;

public class OSGiTest {
  @Test
  public void testComGoogleGsonAnnotationsPackage() throws Exception {
        Manifest mf = findManifest("com.google.gson");
        String importPkg = mf.getMainAttributes().getValue("Import-Package");
        assertWithMessage("Import-Package statement is there").that(importPkg).isNotNull();
        assertSubstring("There should be com.google.gson.annotations dependency", importPkg, "com.google.gson.annotations");
    }

  @Test
  public void testSunMiscImportPackage() throws Exception {
        Manifest mf = findManifest("com.google.gson");
        String importPkg = mf.getMainAttributes().getValue("Import-Package");
    assertWithMessage("Import-Package statement is there").that(importPkg).isNotNull();
        for (String dep : importPkg.split(",")) {
            if (dep.contains("sun.misc")) {
                assertSubstring("sun.misc import is optional", dep, "resolution:=optional");
                return;
            }
        }
        fail("There should be sun.misc dependency, but was: " + importPkg);
    }

    private Manifest findManifest(String pkg) throws IOException {
        List<URL> urls = new ArrayList<>();
        for (URL u : Collections.list(getClass().getClassLoader().getResources("META-INF/MANIFEST.MF"))) {
            InputStream is = u.openStream();
            Manifest mf = new Manifest(is);
            is.close();
            if (pkg.equals(mf.getMainAttributes().getValue("Bundle-SymbolicName"))) {
                return mf;
            }
            urls.add(u);
        }
        fail("Cannot find " + pkg + " OSGi bundle manifest among: " + urls);
        return null;
    }

    private static void assertSubstring(String msg, String wholeText, String subString) {
        if (wholeText.contains(subString)) {
            return;
        }
        fail(msg + ". Expecting " + subString + " but was: " + wholeText);
    }
}
