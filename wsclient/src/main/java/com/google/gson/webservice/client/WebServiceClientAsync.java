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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

import com.google.gson.webservice.definition.WebServiceSystemException;
import com.google.gson.webservice.definition.procedural.WebServiceCallSpec;
import com.google.gson.webservice.definition.procedural.WebServiceRequest;

/**
 * A client for invoking a JSON-based Web-service in an asynchronous manner. The call is queued,
 * and control returns to the caller. A separate thread executes the call, and invokes the
 * client-supplied callback with results.
 *  
 * @author inder
 */
public class WebServiceClientAsync {

  private final BlockingQueue<QueueEntry> queue;
  private final boolean threadPerTask;
  private final TaskExecutor executor;

  public WebServiceClientAsync(WebServiceConfig serverConfig) {
    this(serverConfig, null);
  }
  public WebServiceClientAsync(WebServiceConfig serverConfig, Level logLevel) {
    this(new WebServiceClient(serverConfig, logLevel));
  }

  public WebServiceClientAsync(WebServiceClient client) {
    queue = new LinkedBlockingQueue<QueueEntry>();
    this.threadPerTask = true;
    QueueConsumer consumer = new QueueConsumer(queue, client);
    executor = getExecutor();
    executor.execute(consumer);
  }

  private TaskExecutor getExecutor() {
    return threadPerTask ? new ThreadPerTaskExecutor() : new SingleThreadExecutor();
  }

  public void callAsync(WebServiceCallSpec callSpec, WebServiceRequest request,
      ResponseCallback responseCallback) {
    try {
      queue.put(new QueueEntry(callSpec, request, responseCallback));
    } catch (InterruptedException e) {
      throw new WebServiceSystemException(e);
    }
  }
  
  public void shutdownNow() {
    executor.shutdownNow();
  }
}
