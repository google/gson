package com.google.gson.functional;

import com.google.gson.FieldNameDerivePolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import junit.framework.TestCase;

/**
 * Created by rqg on 28/04/2018.
 */
public class NameDerivePolicyTest extends TestCase {
    public static class Data {
        private int a;
        private boolean b;
        private Long cA;
        @SerializedName(value = "what_name_is")
        private String whatNameIs;

        @Override
        public String toString() {
            return "a = [" + a + "], b = [" + b + "], cA = [" + cA + "], whatNameIs = [" + whatNameIs + "]";
        }
    }

    public void testCamelAndUnderscoresPolicy() {
        Data data = new Data();
        data.a = 1;
        data.b = false;
        data.cA = 14213L;
        data.whatNameIs = "helloNmae";
        Gson gson = new GsonBuilder()
                .setFieldNameDeriveStrategy(FieldNameDerivePolicy.CAMEL_AND_UNDERSCORES)
                .create();

        String mjson = "{\"a\":1,\"b\":false,\"c_a\":14213,\"whatNameIs\":\"helloNmae\"}";
        Data mdata = gson.fromJson(mjson, Data.class);

        assertTrue(dataEqual(mdata, data));
    }


    public static boolean dataEqual(Data a, Data b) {
        return a.a == b.a &&
                a.b == b.b &&
                a.cA.equals(b.cA) &&
                a.whatNameIs.equals(b.whatNameIs);
    }
}
