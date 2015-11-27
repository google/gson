package com.google.gson;

import java.util.Date;

final class MillisDateFormatter implements DateFormatter
{
private MillisDateFormatter(){};
	
	private final static MillisDateFormatter INSTANCE = new MillisDateFormatter();

	@Override
	public String format(Date date) {
		return Long.toString(date.getTime());
	}

	@Override
	public Date parse(String dateAsString) {
		Long seconds = Long.parseLong(dateAsString);
		return new Date(seconds);
	}
	
	public static MillisDateFormatter getInstance()
	{
		return INSTANCE;
	}
	
}