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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Manifest;

import junit.framework.TestCase;

public class OSGiTest extends TestCase {
    public void testSunMiscImportPackage() throws Exception {
        Manifest mf = findManifest("com.google.gson");
        String importPkg = mf.getMainAttributes().getValue("Import-Package");
        assertNotNull("Import-Package statement is currently there", importPkg);
        assertEquals("There should be no sun.misc dependency, but was: " + importPkg, -1, importPkg.indexOf("sun.misc"));
    }

    private Manifest findManifest(String pkg) throws IOException {
        Enumeration<URL> en = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
        List<URL> urls = new ArrayList<URL>();
        while (en.hasMoreElements()) {
            URL u = en.nextElement();
            Manifest mf = new Manifest(u.openStream());
            if (pkg.equals(mf.getMainAttributes().getValue("Bundle-SymbolicName"))) {
                return mf;
            }
            urls.add(u);
        }
        fail("Cannot find com.google.gson OSGi bundle manifest among: " + urls);
        return null;
    }
}
