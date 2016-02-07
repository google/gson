package com.google.gson;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.assertEquals;

public class FieldNamingPolicyTest {
	

	@Test
	public void testUpperCaseFirstLetterNonUpperCaseLetter() {
		verifyUpperCaseFirstLetter("someFieldName", "SomeFieldName");
	}
	
	@Test
	public void testUpperCaseFirstLetterWithUpperCaseLetter() {
		verifyUpperCaseFirstLetter("SomeFieldName", "SomeFieldName");
	}
	
	@Test
	public void testUpperCaseFirstLetterWithUnderscoreAndNonUpperCaseLetter() {
		verifyUpperCaseFirstLetter("_someFieldName", "_SomeFieldName");
	}
	
	@Test
	public void testUpperCaseFirstLetterWithUnderScoreAndWithUpperCaseLetter() {
		verifyUpperCaseFirstLetter("_SomeFieldName", "_SomeFieldName");
	}
	
	@Test
	public void testUpperCaseFirstLetterWithUnderscoreAndNumberAndNonUpperCaseLetter() {
		verifyUpperCaseFirstLetter("_999someFieldName", "_999SomeFieldName");
	}
	
	@Test
	public void testUpperCaseFirstLetterWithUnderScoreAndNumberWithUpperCaseLetter() {
		verifyUpperCaseFirstLetter("_333SomeFieldName", "_333SomeFieldName");
	}

	@Test
	public void testUpperCaseFirstLetter1LowercaseLetter() {
		verifyUpperCaseFirstLetter("x", "X");
	}

	@Test
	public void testUpperCaseFirstLetter1UppercaseLetter() {
		verifyUpperCaseFirstLetter("X", "X");
	}

	@Test
	public void testUpperCaseFirstAllNumbers() {
		verifyUpperCaseFirstLetter("555", "555");
	}

	@Test
	public void testUpperCaseFirstJustUnderscore() {
		verifyUpperCaseFirstLetter("_", "_");
	}

	@Test
	public void testUpperCaseBlank() {
		verifyUpperCaseFirstLetter(" ", " ");
	}

	/**
	 * Verifies the upperCaseFirstLetter is working as expected
	 * @param fieldName
	 * @param expectedFieldName
	 */
	private void verifyUpperCaseFirstLetter(String fieldName, String expectedFieldName) {		
		String actualFieldName = FieldNamingPolicy.upperCaseFirstLetter(fieldName);
		assertEquals("Verify fieldName was uppercased properly", expectedFieldName, actualFieldName);
	}
	

}

