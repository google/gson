package com.google.gson;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;

import com.google.gson.internal.bind.util.ISO8601Utils;

final class ISO8601DateFormater implements DateFormatter
{
	private ISO8601DateFormater(){};
	
	private final static ISO8601DateFormater INSTANCE = new ISO8601DateFormater();
	
	@Override
	public String format(Date date) {
		return ISO8601Utils.format(date, true);
	}

	@Override
	public Date parse(String dateAsString) throws ParseException {
		return ISO8601Utils.parse(dateAsString, new ParsePosition(0));
	}
	
	public static ISO8601DateFormater getInstance()
	{
		return INSTANCE;
	}
	
}
