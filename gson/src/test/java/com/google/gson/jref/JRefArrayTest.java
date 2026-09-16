package com.google.gson.jref;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import com.google.gson.Gson;

public class JRefArrayTest extends JRefAbstractTest {

	@Test
	public void testStringArrayRef() throws Exception {
		Gson mapper = buildGsonJRef();
		String o1 = new String("one");
		String o2 = o1;
		String[] arr = new String[] { o1, o2 };
		String out = mapper.toJson(arr);
		assertJRefCount(out, 1);
		trace("testStringArrayRef jrefserialized=", out);
		String[] oa = mapper.fromJson(out, String[].class);
		assertTrue(oa[0] instanceof String);
		assertTrue(oa[1] instanceof String);
		assertEquals(oa[0], oa[1]);
	}

	@Test
	public void test2DStringArrayRef() throws Exception {
		Gson mapper = buildGsonJRef();
		String o1 = new String("one");
		String o2 = o1;
		String[] arr1 = new String[] { o1, o2 };
		String[] arr2 = arr1;
		String out = mapper.toJson(new String[][] { arr1, arr2 });
		assertJRefCount(out, 2);
		trace("test2DStringArrayRef jrefserialized=", out);
		String[][] oa = mapper.fromJson(out, String[][].class);
		assertTrue(oa[0][0] instanceof String);
		assertTrue(oa[0][1] instanceof String);
		assertArrayEquals(oa[0], oa[1]);
		assertArrayEquals(oa[0], oa[1]);
	}

	@Test
	public void testIntegerArrayRef() throws Exception {
		Gson mapper = buildGsonJRef();
		Integer o1 = Integer.valueOf(100);
		Integer o2 = o1;
		Integer[] arr = new Integer[] { o1, o2 };
		String out = mapper.toJson(arr);
		assertJRefCount(out, 1);
		trace("testIntegerArrayRef jrefserialized=", out);
		Integer[] oa = mapper.fromJson(out, Integer[].class);
		assertTrue(oa[0] instanceof Integer);
		assertTrue(oa[1] instanceof Integer);
		assertEquals(oa[0], oa[1]);
	}

	@Test
	public void test2DIntegerArrayRef() throws Exception {
		Gson mapper = buildGsonJRef();
		Integer o1 = Integer.valueOf(5);
		Integer o2 = o1;
		Integer[] arr1 = new Integer[] { o1, o2 };
		Integer[] arr2 = arr1;
		String out = mapper.toJson(new Integer[][] { arr1, arr2 });
		assertJRefCount(out, 2);
		trace("test2DStringArrayRef jrefserialized=", out);
		Integer[][] oa = mapper.fromJson(out, Integer[][].class);
		assertTrue(oa[0][0] instanceof Integer);
		assertTrue(oa[0][1] instanceof Integer);
		assertArrayEquals(oa[0], oa[1]);
		assertArrayEquals(oa[0], oa[1]);
	}

	@Test
	public void test3DObjectArrayRef() throws Exception {
		Gson mapper = buildGsonJRef();
		Object o1 = Integer.valueOf(0);
		Object o2 = Integer.valueOf(1);
		Object[] arr1 = new Object[] { o1, o2 };
		Object[][] arr2d = new Object[][] { arr1, arr1 };
		Object[][][] arr3d = new Object[][][] { arr2d, arr2d };

		String out = mapper.toJson(arr3d);
		trace("test3DObjectArrayRef jrefserialized=", out);
		assertJRefCount(out, 2);

		Object[][][] oa = mapper.fromJson(out, Object[][][].class);
		assertArrayEquals(oa[0], oa[1]);
		assertArrayEquals(oa[0][0], oa[0][1]);
	}

	@Test
	public void test3DStringArrayRef() throws Exception {
		Gson mapper = buildGsonJRef();
		String s1 = new String("test");
		String[] arr1 = new String[] { s1, s1 };
		String[][] arr2d = new String[][] { arr1, arr1 };
		String[][][] arr3d = new String[][][] { arr2d, arr2d };

		String out = mapper.toJson(arr3d);
		assertJRefCount(out, 3);
		trace("test3DStringArrayRef jrefserialized=", out);

		String[][][] oa = mapper.fromJson(out, String[][][].class);
		assertArrayEquals(oa[0], oa[1]);
		assertArrayEquals(oa[0][0], oa[0][1]);
		assertEquals(oa[0][0][0], oa[0][0][1]);
		assertTrue(oa[0][0][0] instanceof String);
	}

	@Test
	public void test4DObjectArrayRef() throws Exception {
		Gson mapper = buildGsonJRef();
		Object o1 = new Object();
		Object[] arr1 = new Object[] { o1 };
		Object[][] arr2 = new Object[][] { arr1 };
		Object[][][] arr3 = new Object[][][] { arr2 };
		Object[][][][] arr4 = new Object[][][][] { arr3, arr3 };

		String out = mapper.toJson(arr4);
		assertJRefCount(out, 1);
		trace("test4DObjectArrayRef jrefserialized=", out);

		Object[][][][] oa = mapper.fromJson(out, Object[][][][].class);
		assertArrayEquals(oa[0], oa[1]);
		assertArrayEquals(oa[0][0], oa[1][0]);
		assertArrayEquals(oa[0][0][0], oa[1][0][0]);
		assertTrue(oa[0][0][0][0] instanceof Map);
	}

	@Test
	public void test4DStringArrayRef() throws Exception {
		Gson mapper = buildGsonJRef();
		String s1 = new String("deep");
		String s2 = new String("thoughts");
		String[] arr1 = new String[] { s1, s2, s2 };
		String[] arr1a = arr1;
		String[][] arr2 = new String[][] { arr1, arr1a };
		String[][] arr2a = arr2;
		String[][][] arr3 = new String[][][] { arr2, arr2a };
		String[][][] arr3a = arr3;
		String[][][][] arr4 = new String[][][][] { arr3, arr3a, arr3 };

		String out = mapper.toJson(arr4);
		assertJRefCount(out, 5);
		trace("test4DStringArrayRef jrefserialized=", out);

		String[][][][] oa = mapper.fromJson(out, String[][][][].class);
		assertArrayEquals(oa[0], oa[1]);
		assertArrayEquals(oa[0], oa[2]);
		assertArrayEquals(oa[0][0], oa[0][1]);
		assertArrayEquals(oa[0][0][0], oa[0][0][1]);
		assertEquals(oa[0][0][0][0], "deep");
		assertEquals(oa[0][0][0][1], "thoughts");

	}
}
