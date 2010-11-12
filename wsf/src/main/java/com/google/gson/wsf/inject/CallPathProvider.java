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

import javax.servlet.http.HttpServletRequest;

import com.google.greaze.definition.CallPath;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Guice provider for {@link CallPath} for an incoming web service request.
 * 
 * @author inder
 */
public final class CallPathProvider implements Provider<CallPath> {
  private final CallPath callPath;

  @Inject 
  public CallPathProvider(HttpServletRequest request) {
    this(request.getPathInfo());
  }
  
  public CallPathProvider(String pathInfo) {
    this.callPath = new CallPath(pathInfo);
  }

  @Override
  public CallPath get() {
    return callPath;
  }
}
