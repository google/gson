package com.google.gson;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

final class SimpleDateFormatter implements DateFormatter
{
	private final DateFormat dateFormat;
	
	SimpleDateFormatter(DateFormat dateFormat) {
    this.dateFormat = dateFormat;
  }

	@Override
	public String format(Date date) {
		synchronized (dateFormat) {
      return dateFormat.format(date);
    }
	}

	@Override
	public Date parse(String dateAsString) throws ParseException {
		synchronized (dateFormat) {
			return dateFormat.parse(dateAsString);
		}
	}
}