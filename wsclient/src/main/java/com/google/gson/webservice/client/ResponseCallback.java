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
package com.google.gson.webservice.client;

import com.google.greaze.definition.WebServiceSystemException;
import com.google.greaze.definition.webservice.WebServiceCall;
import com.google.greaze.definition.webservice.WebServiceCallSpec;
import com.google.greaze.definition.webservice.WebServiceRequest;

/**
 * A client-supplied callback to be used with {@link WebServiceClientAsync}. When a web-service
 * call is executed asynchronously, this callback is invoked with the results.
 *
 * @author inder
 */
public interface ResponseCallback {
  public void handleResponse(WebServiceCall call);
  public void handleError(WebServiceSystemException e, WebServiceRequest request,
      WebServiceCallSpec callSpec);
}
