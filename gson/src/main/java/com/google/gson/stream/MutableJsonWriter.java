/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.stream;

import java.io.Writer;

/**
 * A Mutable(and poolable) type of {@link JsonWriter}.
 * <p>
 * This type has no any special business(functionality/alg) overriding, instead
 * it allows the caller recycle/reuse this writer instance again without
 * instancing another one.
 * </p>
 * <p>
 * By default, the underlying {@link Writer} stream is closed by closing this
 * type. This policy could be reset using
 * {@link #setCloseWriterOnClose(boolean)} method.
 * </p>
 * <p>
 * If mutability is not a consideration, then please use {@link JsonWriter}
 * </p>
 * <p>
 * Example usage:
 * </p>
 * <pre>
 * Writer out0=...;
 * //Getting a MutableJsonWriter, eitehr by new MutableJsonWriter(), or whatever...
 * MutableJsonWriter writer =...
 * //setting the out, and reset the state
 * writer.reset(out0,true);// or  writer.reset(out0);
 * //using the writer...
 * ...
 * //flushing, and closing the writer
 * writer.flush();
 * writer.close();//could(by default) close the out0 too
 * //getting the json result
 * String _json_res = out0.toString();
 *
 * Writer out1=...;
 * //using the same writer instance to write another json too
 * writer.reset(out1,true);
 * //ignorring closing the out1, by closing writer
 * writer.setCloseWriterOnClose(false);
 * //using the writer...
 * ...
 * //flushing the writer
 * writer.flush();
 * //closing the writer
 * writer.close();//WILL NOT close the out1, because of setCloseWriterOnClose(false)
 * </pre>
 *
 */
public class MutableJsonWriter extends JsonWriter {

    /**
     * Calls the super {@link JsonWriter#JsonWriter(java.io.Writer) }, to create
     * and initialize an instance of this type.
     * <p>
     * If associating the stream is not applicable during instancing, so use the
     * default constructor({@link #JsonWriterMutable() }), and then call the
     * {@link #reset(java.io.Writer, boolean)} when required.
     * </p>
     *
     * @param out the underlying {@link Writer} stream needs to be associated
     * (must be non-{@code null})
     * @throws NullPointerException if the given {@code out} is {@code null}
     * @see #JsonWriterMutable()
     * @see #reset(java.io.Writer, boolean)
     */
    public MutableJsonWriter(Writer out) {
        super(out);
    }

    /**
     * Creates an state of this class with no-state.
     * <p>
     * After instancing, {@link #reset(java.io.Writer, boolean) } must be called
     * in order to setting the underlying {@link Writer} stream, same
     * initializing the state if needed.
     * </p>
     */
    public MutableJsonWriter() {
        super();
    }

    /**
     * Returns the underlying {@link Writer} associated to this JSON writer.
     * It's {@code null}, if there is no any associated {@link Writer} yet.
     */
    public Writer getOut() {
        return out;
    }

    /**
     * Sets the working {@code out}({@link Writer}), also call for
     * {@link #init()} if appreciated.
     * <p>
     * It <b>does not</b> closes the current associated
     * {@link Writer}({@code out}), neither flushes it.
     * </p>
     * <p>
     * <b>Note:</b> It also does not resets the writing policies(such as
     * lenient, or html-safe), and {@link #closeWriterOnClose} property.
     * </p>
     *
     * @param out the non-{@code null} writer instance to be reset for out
     * @param alsoInit when {@code true}, then {@link #init()} will be
     * called too
     * @since +2.8.7-SNAPSHOT ?
     * @see #close()
     * @see #setCloseWriterOnClose(boolean)
     */
    @Override
    public void reset(Writer out, boolean alsoInit) {
        super.reset(out, alsoInit);
    }

    /**
     * Calls the {@code reset(out, true)}.
     *
     * @param out the non-{@code null} writer instance to be reset for out
     * @see #reset(java.io.Writer, boolean)
     */
    public void reset(Writer out) {
        reset(out, true);
    }

    /**
     * Setting the policy, if the underlying {@link Writer} should be closed,
     * once {@link #close()} is called, or not.
     * <p>
     * The default value is {@code true}
     * </p>
     *
     * @param closeOutOnClose when {@code true} then underlying
     * {@link Writer} should be closed by closing this instance, {@code false}
     * otherwise
     */
    public void setCloseWriterOnClose(boolean closeOutOnClose) {
        this.closeWriterOnClose = closeOutOnClose;
    }

    /**
     * Returns {@code true}, when underlying {@link Writer} should be closed by
     * closing this instance, {@code false} otherwise.
     */
    public boolean isCloseWriterOnClose() {
        return closeWriterOnClose;
    }

}
