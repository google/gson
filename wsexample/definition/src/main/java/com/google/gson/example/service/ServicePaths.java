/*
 * Copyright (C) 2010 Google Inc.
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
package com.google.gson.example.service;

import com.google.gson.webservice.definition.CallPath;

/**
 * An enum describing all paths for this service
 *
 * @author Inderjeet Singh
 */
public enum ServicePaths {
  NULL_REQUEST(null),
  CART("/rest/cart"),
  ORDER("/rest/order");
  
  private final CallPath path;

  private ServicePaths(String pathInfo) {
    this.path = new CallPath(pathInfo);
  }

  public CallPath getCallPath() {
    return path;
  }

  public static CallPath getCallPath(CallPath invokedPath) {
    for (ServicePaths path : values()) {
      CallPath callPath = path.path;
      String callPathInfo = callPath.get();
      // A rest path can end with a resource-id too.
      // For example, /rest/cart/1234 should match with /rest/cart
      if (callPathInfo != null && invokedPath.matches(callPath)) {
        return callPath;
      }
    }
    return null;
  }
}