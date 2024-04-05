package com.google.gson.metrics;

import com.google.caliper.BeforeExperiment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DeserializationBenchmark {
    private static final TypeToken<List<BagOfPrimitives>> LIST_TYPE_TOKEN =
        new TypeToken<List<BagOfPrimitives>>() {};
    private static final Type LIST_TYPE = LIST_TYPE_TOKEN.getType();
    private Gson gson;
    private String jsonSingle;
    private String jsonCollection;

    public static void main(String[] args) {
        NonUploadingCaliperRunner.run(DeserializationBenchmark.class, args);
    }
    @BeforeExperiment
    void setUp() throws Exception {
        this.gson = new Gson();
        //setup for single object
        BagOfPrimitives singleBag = new BagOfPrimitives(10L, 1, false, "foo");
        this.jsonSingle = gson.toJson(singleBag);
        //setup for collection
        List<BagOfPrimitives> bags = new ArrayList<>();
        for (int i = 0; i < 100; ++i) {
            bags.add(new BagOfPrimitives(10L, 1, false, "foo"));
        }
        this.jsonCollection = gson.toJson(bags, LIST_TYPE);
    }

    public void timeSingleObjectBagOfPrimitivesDefault(int reps) {
        for (int i = 0; i < reps; ++i) {
            gson.fromJson(jsonSingle, BagOfPrimitives.class);
        }
    }

    public void timeCollectionsDefault(int reps) {
        for (int i = 0; i < reps; ++i) {
            gson.fromJson(jsonCollection, LIST_TYPE_TOKEN);
        }
    }

    private BagOfPrimitives parseAndAssignFields(JsonReader jr)throws IOException{
        jr.beginObject();
        long longValue = 0;
        int intValue = 0;
        boolean booleanValue = false;
        String stringValue = null;
        while (jr.hasNext()) {
            String name = jr.nextName();
            if (name.equals("longValue")) {
                longValue = jr.nextLong();
            } else if (name.equals("intValue")) {
                intValue = jr.nextInt();
            } else if (name.equals("booleanValue")) {
                booleanValue = jr.nextBoolean();
            } else if (name.equals("stringValue")) {
                stringValue = jr.nextString();
            } else {
                throw new IOException("Unexpected name: " + name);
            }
        }
        jr.endObject();
        return new BagOfPrimitives(longValue, intValue, booleanValue, stringValue);

    }


    /** Benchmark to measure deserializing objects by hand */
    public void timeBagOfPrimitivesStreaming(int reps) throws IOException {
        for (int i = 0; i < reps; ++i) {
            StringReader reader = new StringReader(jsonSingle);
            JsonReader jr = new JsonReader(reader);
            parseAndAssignFields(jr);
        }
    }


    /** Benchmark to measure deserializing objects by hand */
    @SuppressWarnings("ModifiedButNotUsed")
    public void timeCollectionsStreaming(int reps) throws IOException {
        for (int i = 0; i < reps; ++i) {
            StringReader reader = new StringReader(jsonCollection);
            JsonReader jr = new JsonReader(reader);
            jr.beginArray();
            List<BagOfPrimitives> bags = new ArrayList<>();
            while (jr.hasNext()) {
                BagOfPrimitives bagOfPrimitives = parseAndAssignFields(jr);
                bags.add(bagOfPrimitives);
            }
            jr.endArray();
        }
    }


    private BagOfPrimitives parseAndSetFieldFromJson(JsonReader jr) throws IOException, IllegalAccessException{
        jr.beginObject();
        BagOfPrimitives bag = new BagOfPrimitives();
        while (jr.hasNext()) {
            String name = jr.nextName();
            for (Field field : BagOfPrimitives.class.getDeclaredFields()) {
                if (field.getName().equals(name)) {
                    Class<?> fieldType = field.getType();
                    if (fieldType.equals(long.class)) {
                        field.setLong(bag, jr.nextLong());
                    } else if (fieldType.equals(int.class)) {
                        field.setInt(bag, jr.nextInt());
                    } else if (fieldType.equals(boolean.class)) {
                        field.setBoolean(bag, jr.nextBoolean());
                    } else if (fieldType.equals(String.class)) {
                        field.set(bag, jr.nextString());
                    } else {
                        throw new RuntimeException("Unexpected: type: " + fieldType + ", name: " + name);
                    }
                }
            }
        }
        jr.endObject();
        return bag;
    }


    /**
     * This benchmark measures the ideal Gson performance: the cost of parsing a JSON stream and
     * setting object values by reflection. We should strive to reduce the discrepancy between this
     * and {@link #timeSingleObjectBagOfPrimitivesDefault(int)} .
     */
    public void timeBagOfPrimitivesReflectionStreaming(int reps) throws IOException, IllegalAccessException  {
        for (int i = 0; i < reps; ++i) {
            StringReader reader = new StringReader(jsonSingle);
            JsonReader jr = new JsonReader(reader);
            parseAndSetFieldFromJson(jr);
        }
    }


    /**
     * This benchmark measures the ideal Gson performance: the cost of parsing a JSON stream and
     * setting object values by reflection. We should strive to reduce the discrepancy between this
     * and {@link #timeCollectionsDefault(int)} .
     */
    @SuppressWarnings("ModifiedButNotUsed")
    public void timeCollectionsReflectionStreaming(int reps) throws Exception {
        for (int i = 0; i < reps; ++i) {
            StringReader reader = new StringReader(jsonCollection);
            JsonReader jr = new JsonReader(reader);
            jr.beginArray();
            List<BagOfPrimitives> bags = new ArrayList<>();
            while (jr.hasNext()) {
                BagOfPrimitives bag = parseAndSetFieldFromJson(jr);
                bags.add(bag);
            }
            jr.endArray();
        }
    }

}