package com.google.gson;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Gson type adapter for [Instant] type and configurable date/time string format.
 *
 * [Original class sources](https://github.com/hashgraph/did-sdk-java/blob/master/src/main/java/com/hedera/hashgraph/identity/utils/InstantTypeAdapter.java)
 */
public class InstantTypeAdapter extends TypeAdapter<Instant> {
    private final DateTimeFormatter outputDateTimeFormatter;

    private final DateTimeFormatter firstParser;
    private final List<DateTimeFormatter> otherParsers;

    public InstantTypeAdapter(DateTimeFormatter dateTimeFormatter, Iterable<DateTimeFormatter> inputDateTimeParsers) {
        outputDateTimeFormatter = Objects.requireNonNull(dateTimeFormatter);
        Iterator<DateTimeFormatter> parsers = inputDateTimeParsers.iterator();
        if (!parsers.hasNext()) {
            throw new IllegalArgumentException("input parsers list must be nonempty");
        }
        firstParser = parsers.next();
        List<DateTimeFormatter> otherParsers = new ArrayList<DateTimeFormatter>();
        while (parsers.hasNext()) {
            otherParsers.add(parsers.next());
        }
        this.otherParsers = Collections.unmodifiableList(otherParsers);
    }

    @Override
    public void write(JsonWriter out, Instant value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            String instantStr = outputDateTimeFormatter.format(value);
            out.value(instantStr);
        }
    }

    @Override
    public Instant read(JsonReader in) throws IOException {
        JsonToken token = in.peek();
        if (token != JsonToken.STRING) {
            in.skipValue();
            return null;
        }
        return parse(in.nextString());
    }

    /**
     * Parses the given string into [Instant] object.
     *
     * @param  instantStr               Instant as string.
     * @return                          [Instant] object.
     * @throws IllegalArgumentException In case parsing fails.
     */
    private Instant parse(String instantStr) {
        if (instantStr == null) {
            return null;
        }
        TemporalAccessor accessor = null;
        try {
            accessor = firstParser.parse(instantStr);
        } catch (DateTimeParseException ignore) {
            for (DateTimeFormatter parser : otherParsers) {
                try {
                    accessor = parser.parse(instantStr);
                } catch (DateTimeParseException ignoreToo) {
                    // ignore the error and move to the next parser
                    continue;
                }
            }
        }
        Objects.requireNonNull(accessor, "Input string does not match any parsing formats used by this adapter");
        return Instant.from(accessor);
    }
}
