package com.google.gson;

import com.google.gson.annotations.SerializedName;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;

public class GsonMutableCollectionsTest extends TestCase {

    public void testSerializationOfMutableCollectionWhenOtherThreadsAreMutatingIt() throws InterruptedException {
        final List<ConcurrentModificationException> exceptionsBag = new ArrayList<ConcurrentModificationException>();

        final Model model = new Model(4, "Model $it");
        final List<Model> items = Collections.nCopies(1000, model);

        final List<Model> mutableList = new ArrayList<Model>(items);
        final Gson gson = new GsonBuilder().create();

        final List<Thread> writers = new ArrayList<Thread>();
        for (int i=0; i<1000; ++i) {
            writers.add(new Thread(){
                @Override
                public void run() {
                    mutableList.addAll(items);
                }
            });
        }
        final List<Thread> readers= new ArrayList<Thread>();
        for (int i=0; i<10000; ++i) {
            readers.add(new Thread() {
                @Override
                public void run() {
                    try{
                        gson.toJson(new ModelList(mutableList));
                    }catch(ConcurrentModificationException ce){
                        exceptionsBag.add(ce);
                    }

                }
            });
        }
        for (Thread reader : readers) {
            reader.start();
        }
        for (Thread writer : writers) {
            writer.start();
        }

        // Wait threads to complete
        for (Thread reader : readers) {
            reader.join();
        }
        for (Thread writer : writers) {
            writer.join();
        }
        assertEquals(0, exceptionsBag.size());
    }

    static class ModelList{
        @SerializedName("list")
        public List<Model> list;

        public ModelList(List<Model> list) {
            this.list = list;
        }
    }

    static class Model {
        @SerializedName("a")
        public int a;
        @SerializedName("b")
        public String b;

        public Model(int a, String b) {
            this.a = a;
            this.b = b;
        }
    }
}
