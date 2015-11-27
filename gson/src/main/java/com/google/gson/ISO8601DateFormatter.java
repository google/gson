package com.google.gson;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;

import com.google.gson.internal.bind.util.ISO8601Utils;

final class ISO8601DateFormatter implements DateFormatter
{
	private ISO8601DateFormatter(){};
	
	private final static ISO8601DateFormatter INSTANCE = new ISO8601DateFormatter();
	
	@Override
	public String format(Date date) {
		return ISO8601Utils.format(date, true);
	}

	@Override
	public Date parse(String dateAsString) throws ParseException {
		return ISO8601Utils.parse(dateAsString, new ParsePosition(0));
	}
	
	public static ISO8601DateFormatter getInstance()
	{
		return INSTANCE;
	}
	
}
