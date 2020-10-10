/*
 * Copyright 2020 https://github.com/911992.
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

 /*
gson
File: MutableJsonWriter.java
Created on: Oct 9, 2020 12:33:49 PM
    @author https://github.com/911992
 
File History:
    initial version: 0.1(20201009)
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
 * Writer _out=...;
 * //Getting a MutableJsonWriter, eitehr by new MutableJsonWriter(), or whatever...
 * MutableJsonWriter _json_writer =...
 * //setting the out, and reset the state
 * _json_writer.reset(_out,true);// or  _json_writer.reset(_out);
 * //using the _json_writer...
 * ...
 * //flushing, and closing the _json_writer
 * _json_writer.flush();
 * _json_writer.close();//could(by default) close the _out too
 * //getting the json result
 * String _json_res = _out.toString();
 *
 * Writer _out1=...;
 * //using the same _json_writer instance to write another json too
 * _json_writer.reset(_out1,true);
 * //ignorring closing the _out1, by closing _json_writer
 * _json_writer.setCloseWriterOnClose(false);
 * //using the _json_writer...
 * ...
 * //flushing the _json_writer
 * _json_writer.flush();
 * //closing the _json_writer
 * _json_writer.close();//WILL NOT close the _out1, because of setCloseWriterOnClose(false)
 * </pre>
 *
 * @author https://github.com/911992
 * @since +2.8.7-SNAPSHOT ?
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
     * {@link #_init()} if appreciated.
     * <p>
     * It <b>does not</b> closes the current associated
     * {@link Writer}({@code out}), neither flushes it.
     * </p>
     * <p>
     * <b>Note:</b> It also does not resets the writing policies(such as
     * lenient, or html-safe), and {@link #closeWriterOnClose} property.
     * </p>
     *
     * @param arg_out the non-{@code null} writer instance to be reset for out
     * @param arg_also_init when {@code true}, then {@link #_init()} will be
     * called too
     * @since +2.8.7-SNAPSHOT ?
     * @see #close()
     * @see #setCloseWriterOnClose(boolean)
     */
    public void reset(Writer arg_out, boolean arg_also_init) {
        super._reset(arg_out, arg_also_init);
    }

    /**
     * Calls the {@code reset(arg_out, true)}
     *
     * @param arg_out the non-{@code null} writer instance to be reset for out
     * @see #reset(java.io.Writer, boolean)
     */
    public void reset(Writer arg_out) {
        reset(arg_out, true);
    }

    /**
     * Setting the policy, if the underlying {@link Writer} should be closed,
     * once {@link #close()} is called, or not.
     * <p>
     * The default value is {@code true}
     * </p>
     *
     * @param arg_closeOutOnClose when {@code true} then underlying
     * {@link Writer} should be closed by closing this instance, {@code false}
     * otherwise
     */
    public void setCloseWriterOnClose(boolean arg_closeOutOnClose) {
        this.closeWriterOnClose = arg_closeOutOnClose;
    }

    /**
     * Returns {@code true}, when underlying {@link Writer} should be closed by
     * closing this instance, {@code false} otherwise.
     */
    public boolean isCloseWriterOnClose() {
        return closeWriterOnClose;
    }

}
