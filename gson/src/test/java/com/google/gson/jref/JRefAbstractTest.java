package com.google.gson.jref;

import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JRefTypeAdapter;
import com.google.gson.JRefTypeAdapterFactory;

import org.junit.After;
import org.junit.Assert;

public class JRefAbstractTest {

	public static boolean TRACE = true;

	@After
	public void tearDown() {
		JRefTypeAdapter.ptrToValue.remove();
		JRefTypeAdapter.readTSC.remove();
		JRefTypeAdapter.writeTSC.remove();
		JRefTypeAdapter.valueToPtr.remove();
	}

	public static GsonBuilder jsonMapperBuilder() {
		return new GsonBuilder();
	}

	protected Gson buildGsonJRef() {
		GsonBuilder builder = jsonMapperBuilder();
		builder.registerTypeAdapterFactory(new JRefTypeAdapterFactory());
		return builder.create();
	}

	protected Gson buildGsonNoJRef() {
		GsonBuilder builder = jsonMapperBuilder();
		return builder.create();
	}

	static long countMatches(String text, String target) {
		if (text == null || target == null || target.isEmpty())
			return 0;
		String quotedTarget = Pattern.quote(target);

		return Pattern.compile(quotedTarget).matcher(text).results().count();
	}

	protected void assertJRefCount(String input, long expectedJRefs) {
		Assert.assertEquals(expectedJRefs, countMatches(input, "$ref"));
	}

	void trace(String method, String s) {
		if (TRACE) {
			System.out.println(method + "." + s);
		}
	}
}
