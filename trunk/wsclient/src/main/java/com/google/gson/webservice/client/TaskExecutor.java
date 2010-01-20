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

import java.util.concurrent.Executor;

/**
 * An {@link Executor} with an additional method for shutdown. We could have just used
 * {@link java.util.concurent.ExecutorService}, however, that requires too many methods to be
 * implemented.
 *  
 * @author inder
 */
interface TaskExecutor extends Executor {
  public void shutdownNow();
}
