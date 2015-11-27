package com.google.gson;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

final class SimpleDateFormatter implements DateFormatter
{
	private final DateFormat dateFormat;
	
	SimpleDateFormatter(DateFormat dateFormat) {
		// Clone to prevent object from change
    this.dateFormat = (DateFormat) dateFormat.clone();
  }

//These methods need to be synchronized since JDK DateFormat classes are not thread-safe
 // See issue 162
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