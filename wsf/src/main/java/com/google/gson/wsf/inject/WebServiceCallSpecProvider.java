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
package com.google.gson.wsf.inject;

import com.google.gson.webservice.definition.CallPath;
import com.google.gson.webservice.definition.procedural.WebServiceCallSpec;
import com.google.gson.webservice.definition.procedural.WebServiceSpec;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Guice provider for {@link WebServiceCallSpec}.
 *
 * @author inder
 */
public final class WebServiceCallSpecProvider implements Provider<WebServiceCallSpec> {
  private final CallPath callPath;
  private final WebServiceSpec webServiceSpec;
  
  @Inject
  WebServiceCallSpecProvider(CallPath callPath, WebServiceSpec webServiceSpec) {
    this.callPath = callPath;
    this.webServiceSpec = webServiceSpec;
  }

  @Override
  public WebServiceCallSpec get() {
    return webServiceSpec.getCalls().get(callPath);
  }    
}