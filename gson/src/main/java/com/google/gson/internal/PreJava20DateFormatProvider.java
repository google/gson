package com.google.gson.internal;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class PreJava20DateFormatProvider {
    /**
     * Compatibility fix for bug: <a href="https://bugs.openjdk.org/browse/JDK-8304925">JDK-8304925</a>
     * Space used instead NBSP in format pattern.
     *
     * @return Returns the same DateFormat as {@code DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US)}
     * in Java 19 or below.
     */
    public static DateFormat getUSDateFormat() {
        return new SimpleDateFormat("MMM d, y, h:mm:ss a", Locale.US);
    }
}
