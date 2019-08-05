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
package com.google.gson;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * This class provides Gson-local variables.  These variables
 * only exist during the execution of {@link com.google.gson.Gson#toJson},
 * {@link com.google.gson.Gson#fromJson} and related methods.
 *
 * <p>This can be used as a shared storage for working with complex Json entities,
 * where the stateless architecture of {@link com.google.gson.TypeAdapter} doesn't
 * provide enough context.</p>
 *
 * <p>The API mimics that of {@link ThreadLocal}, except that all methods will throw
 * {@link IllegalStateException} if not called from within a Gson operation.</p>
 *
 * @author Paulo Costa
 */
public class GsonLocal<T> {
    /**
     * Returns the value in this
     * gson-local variable.  If the variable has no value for the
     * current gson operation, it is first initialized to the
     * value returned by an invocation of the {@link #initialValue} method.
     *
     * @return the current value of this gson-local
     * @throws IllegalStateException if there is no ongoing Gson operation in the current thread
     */
    @SuppressWarnings("unchecked")
    public T get() {
        Context context = Context.get();
        T value;
        if (!context.states.containsKey(this)) {
            value = initialValue();
            context.states.put(this, value);
        } else {
            value = (T) context.states.get(this);
        }
        return value;
    }

    /**
     * Sets the current value of this gson-local variable
     * to the specified value.  Most subclasses will have no need to
     * override this method, relying solely on the {@link #initialValue}
     * method to set the values of gson-locals.
     *
     * @param value the value to be stored in the current gson-local storage.
     * @throws IllegalStateException if there is no ongoing Gson operation in the current thread
     */
    public void set(T value) {
        Context.get().states.put(this, value);
    }

    /**
     * Removes the current value for this gson-local
     * variable.  If this gson-local variable is subsequently
     * {@linkplain #get read}, its value will be
     * reinitialized by invoking its {@link #initialValue} method,
     * unless its value is {@linkplain #set set}
     * in the interim.  This may result in multiple invocations of the
     * {@code initialValue} method.
     *
     * @throws IllegalStateException if there is no ongoing Gson operation in the current thread
     */
    public void remove() {
        Context.get().states.remove(this);
    }

    /**
     * Returns the "initial value" for this
     * gson-local variable.  This method will be invoked the first
     * time a the variable with the {@link #get}
     * method from a gson-operation, unless the thread previously invoked the {@link #set}
     * method, in which case the {@code initialValue} method will not
     * be invoked for the gson operation.  Normally, this method is invoked at
     * most once per operation, but it may be invoked again in case of
     * subsequent invocations of {@link #remove} followed by {@link #get}.
     *
     * <p>This implementation simply returns {@code null}; if the
     * programmer desires gson-local variables to have an initial
     * value other than {@code null}, {@code GsonLocal} must be
     * subclassed, and this method overridden.  Typically, an
     * anonymous inner class will be used.
     *
     * @return the initial value for this gson-local
     */
    protected T initialValue() {
        return null;
    }

    /**
     * Return the Gson instance that started the current operation
     * @return The current {@link Gson} instance
     * @throws IllegalStateException if there is no ongoing Gson operation in the current thread
     */
    public static Gson gson() {
        return Context.get().gson;
    }

    /**
     * The Context stores information for all the GsonLocal variables.
     *
     * It is thread-local and set during a Gson operation.
     */
    /* package */ static class Context {
        /** Stores each thread's Context */
        private static ThreadLocal<Context> threadContext = new ThreadLocal<Context>();

        /** Stores the state of each GsonLocal during the current operation */
        Map<GsonLocal, Object> states = new IdentityHashMap<GsonLocal, Object>();

        /** Gson instance where the current operation is taking place */
        Gson gson;

        private Context(Gson gson) {
            this.gson = gson;
        }

        /**
         * Creates a new context before a Gson operation.
         *
         * @return The previous context for this thread -- Likely null
         */
        public static Context start(Gson gson) {
            Context oldContext = threadContext.get();
            threadContext.set(new Context(gson));
            return oldContext;
        }

        /**
         * Sets the current context for this thread.
         *
         * Used to restore the previous context after a Gson operation is finished.
         *
         * @param oldContext The new context, possibly null.
         */
        public static void set(Context oldContext) {
            if (oldContext == null) {
                threadContext.remove();
            } else {
                threadContext.set(oldContext);
            }
        }

        /**
         * Gets the context for the curreny Gson operation.
         *
         * @return The context associated with the curreny Gson operation
         * @throws IllegalStateException if there is no ongoing Gson operation in the current thread
         */
        public static Context get() throws IllegalStateException {
            Context context = threadContext.get();
            if (context == null) {
                threadContext.remove();
                throw new IllegalStateException("Not in a Gson operation");
            }
            return context;
        }
    }
}
