package com.google.gson;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Gson type adapter for serialization/deserialization between [Instant] and ISO 8601 date format.
 * <p>
 * [Original class sources](https://github.com/hashgraph/did-sdk-java/blob/master/src/main/java/com/hedera/hashgraph/identity/utils/Iso8601InstantTypeAdapter.java)
 */
public class Iso8601InstantTypeAdapter extends InstantTypeAdapter {
    private static final DateTimeFormatter DEFAULT_OUTPUT_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    private static final List<DateTimeFormatter> DEFAULT_INPUT_PARSERS = Collections.unmodifiableList(Arrays.asList(
            DateTimeFormatter.ISO_INSTANT,
            DateTimeFormatter.ISO_OFFSET_DATE_TIME,
            DateTimeFormatter.ISO_DATE_TIME,
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
    ));

    public static final Iso8601InstantTypeAdapter INSTANCE = new Iso8601InstantTypeAdapter();

    private Iso8601InstantTypeAdapter() {
        super(DEFAULT_OUTPUT_FORMATTER, DEFAULT_INPUT_PARSERS);
    }
}
