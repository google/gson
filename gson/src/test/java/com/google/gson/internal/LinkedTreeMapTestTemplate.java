package com.google.gson.internal;

import com.google.gson.common.MoreAsserts;

import java.util.AbstractMap;
import java.util.Random;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;

public abstract class LinkedTreeMapTestTemplate {

    public static <TLinkedTreeMapStringInteger extends AbstractMap<String, Integer>> void testEqualsAndHashCode(
            Class<TLinkedTreeMapStringInteger> clazzTLinkedTreeMapStringInteger) throws Exception {
        TLinkedTreeMapStringInteger map1 = clazzTLinkedTreeMapStringInteger.newInstance();
        map1.put("A", 1);
        map1.put("B", 2);
        map1.put("C", 3);
        map1.put("D", 4);

        TLinkedTreeMapStringInteger map2 = clazzTLinkedTreeMapStringInteger.newInstance();
        map2.put("C", 3);
        map2.put("B", 2);
        map2.put("D", 4);
        map2.put("A", 1);

        MoreAsserts.assertEqualsAndHashCode(map1, map2);
    }

    public static <TLinkedTreeMapStringString extends AbstractMap<String,String>>void testPutOverrides(
            Class<TLinkedTreeMapStringString> clazzTLinkedTreeMapStringString) throws Exception {
        TLinkedTreeMapStringString map=clazzTLinkedTreeMapStringString.newInstance();
        assertNull(map.put("d","donut"));
        assertNull(map.put("e","eclair"));
        assertNull(map.put("f","froyo"));
        assertEquals(3,map.size());

        assertEquals("donut",map.get("d"));
        assertEquals("donut",map.put("d","done"));
        assertEquals(3,map.size());
    }

    public static <TLinkedTreeMapStringString extends AbstractMap<String,String>>void testRehashWithLargeSetOfRandomKeys(
            Class<TLinkedTreeMapStringString> clazzTLinkedTreeMapStringString) throws Exception {
        Random random=new Random(1367593214724L);
        TLinkedTreeMapStringString map=clazzTLinkedTreeMapStringString.newInstance();
        String[] keys=new String[1000];
        for (int i=0; i < keys.length; i++) {
            keys[i]=Integer.toString(Math.abs(random.nextInt()),36) + "-" + i;
            map.put(keys[i],"" + i);
        }

        for (int i=0; i < keys.length; i++) {
            String key=keys[i];
            assertTrue(map.containsKey(key));
            assertEquals("" + i,map.get(key));
        }
    }
}
