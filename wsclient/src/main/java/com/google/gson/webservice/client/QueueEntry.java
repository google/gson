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

import com.google.gson.webservice.definition.WebServiceCallSpec;
import com.google.gson.webservice.definition.WebServiceRequest;

/**
 * A holder class for an entry stored in queue. It contains references to the request, callspec,
 * and the client-supplied callback to provide sufficient information to execute a web-service call.
 *
 * @author inder
 */
final class QueueEntry {
  final WebServiceCallSpec callSpec;
  final WebServiceRequest request;
  final ResponseCallback responseCallback;

  QueueEntry(WebServiceCallSpec callSpec, WebServiceRequest request,
      ResponseCallback responseCallback) {
    this.callSpec = callSpec;
    this.request = request;
    this.responseCallback = responseCallback;
  }
}
