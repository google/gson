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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * An executor that uses a single thread to execute all calls
 *
 * @author inder
 */
final class SingleThreadExecutor implements TaskExecutor {
  private ExecutorService executor;
  public void execute(final Runnable r) {
    executor = Executors.newSingleThreadExecutor();
    executor.execute(r);
  }

  @Override
  public void shutdownNow() {
    executor.shutdownNow();
  }
}