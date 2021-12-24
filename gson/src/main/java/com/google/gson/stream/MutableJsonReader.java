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

import java.io.Reader;

/**
 * A Mutable(and poolable) type of {@link JsonReader}.
 * <p>
 * This type has no any special business(functionality/alg) overriding, instead
 * it allows the caller recycle/reuse this reader instance again without
 * instancing another one.
 * </p>
 * <p>
 * By default, the underlying {@link Reader} stream is closed by closing this
 * type. This policy could be reset using {@link #setCloseReaderOnClose(boolean)
 * }
 * method.
 * </p>
 * <p>
 * If mutability is not a consideration, then please use {@link JsonReader}
 * </p>
 * <p>
 * Example usage:
 * </p>
 * <pre>
 * Reader in0=...;
 * //Getting a MutableJsonReader, either by new MutableJsonReader(), or whatever...
 * MutableJsonReader reader = ...
 * //setting the in, and reset the state
 * reader.reset(in0);
 * //using the reader...
 * ...
 * //closing the reader
 * reader.close();//could(by default) close the in0 too
 *
 * Reader in1=...;
 * //using the same reader instance to read another json too
 * reader.reset(in1,true);//same as reader.reset(in1);
 * //ignorring closing the in1, by closing reader
 * reader.setCloseReaderOnClose(false);
 * //using the reader...
 * ...
 * //closing the reader
 * reader.close();//WILL NOT close the _out1, becasue of setCloseReaderOnClose(false)
 * </pre>
 *
 */
public class MutableJsonReader extends JsonReader {

    /**
     * Calls the super {@link JsonReader#JsonReader(java.io.Reader) }, to create
     * and initialize an instance of this type.
     * <p>
     * If associating the stream is not applicable during instancing, so use the
     * default constructor({@link #MutableJsonReader()}), and then call the
     * {@link #reset(java.io.Reader, boolean)} when required.
     * </p>
     *
     * @param in the underlying {@link Reader} stream needs to be associated
     * (must be non-{@code null})
     * @throws NullPointerException if the given {@code out} is {@code null}
     * @see #MutableJsonReader()
     * @see #reset(java.io.Reader, boolean)
     */
    public MutableJsonReader(Reader in) {
        super(in);
    }

    /**
     * Creates an state of this class with no-state.
     * <p>
     * After instancing, {@link #reset(java.io.Reader, boolean)} must be called
     * in order to setting the underlying {@link Reader} stream, same
     * initializing the state if needed.
     * </p>
     */
    public MutableJsonReader() {
    }

    /**
     * Returns the underlying {@link Reader} associated to this JSON writer.
     * It's {@code null}, if there is no any associated {@link Reader} yet.
     */
    public Reader getIn() {
        return in;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void reset(Reader in, boolean resetState) {
        super.reset(in, resetState); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Calls the {@code reset(argin0, true)}
     *
     * @param in the non-{@code null} reader instance to be reset for in
     * @see #reset(java.io.Reader, boolean)
     */
    public void reset(Reader in) {
        reset(in, true);
    }

    /**
     * Returns {@code true}, when underlying {@link Reader} should be closed by
     * closing this instance, {@code false} otherwise.
     */
    public boolean isCloseReaderOnClose() {
        return closeReaderOnClose;
    }

    /**
     * Setting the policy, if the underlying {@link Reader} should be closed,
     * once {@link #close()} is called, or not.
     * <p>
     * The default value is {@code true}
     * </p>
     *
     * @param closeReaderOnClose when {@code true} then underlying
     * {@link Reader} should be closed by closing this instance, {@code false}
     * otherwise
     */
    public void setCloseReaderOnClose(boolean closeReaderOnClose) {
        this.closeReaderOnClose = closeReaderOnClose;
    }

}
