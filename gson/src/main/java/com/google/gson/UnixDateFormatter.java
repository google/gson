package com.google.gson;

import java.util.Date;

final class UnixDateFormat implements DateFormatter
{

	@Override
	public String format(Date date) {
		return Long.toString(date.getTime() / 1000);
	}

	@Override
	public Date parse(String dateAsString) {
		Long seconds = Long.parseLong(dateAsString);
		return new Date(seconds * 1000);
	}
	
}