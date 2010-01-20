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

import java.util.concurrent.BlockingQueue;

import com.google.gson.webservice.definition.WebServiceCall;
import com.google.gson.webservice.definition.WebServiceResponse;
import com.google.gson.webservice.definition.WebServiceSystemException;

/**
 * A consumer that executes in its own thread consuming queue entries and invoking web-service calls
 *
 * @author inder
 */
final class QueueConsumer implements Runnable {

  private final BlockingQueue<QueueEntry> queue;
  private WebServiceClient client;

  QueueConsumer(BlockingQueue<QueueEntry> queue, WebServiceClient client) {
    this.queue = queue;
    this.client = client;
  }

  @Override
  public void run() {
    try {
      while(true) {
        consume(queue.take());
      }
    } catch (InterruptedException e) {
      // exit
    }
  }

  private void consume(QueueEntry entry) {
    try {
      WebServiceResponse response = client.getResponse(entry.callSpec, entry.request);
      WebServiceCall call = new WebServiceCall(entry.callSpec, entry.request, response);
      entry.responseCallback.handleResponse(call);
    } catch (WebServiceSystemException e) {
      entry.responseCallback.handleError(e, entry.request, entry.callSpec);
    }
  }  
}
