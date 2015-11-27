package com.google.gson;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.TimeZone;

public class wip2 {

	public static void main(String[] args) {
		/*Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").create();
		String json = "{'date': \"2015-11-19\"}";// "{'date':
																						 // \"2015-11-19T18:33:51Z\"}";
		MyDateJson datDate = gson.fromJson(json, MyDateJson.class);
		Date date = datDate.getDate();*/
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		SimpleDateFormatter formatter = new SimpleDateFormatter(format);
		String formattedDate = formatter.format(date);
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		String formattedDate2 = formatter.format(date);
		
		boolean equal = formattedDate.equals(formattedDate2);
		
		String[] str = {"a", "b"};
		String[] str2 = {"aa", "bb"};
		
		EnumMap<Letters, String> emap = new EnumMap<Letters, String>(Letters.class);
		emap.put(Letters.A, "yo");
		String yo = emap.get(Letters.A);
	}
	
	public enum Letters
	{
		A,
		B;
	}

	private class MyDateJson {
		private Date date;

		public Date getDate() {
			return date;
		}

		public void setDate(Date date) {
			this.date = date;
		}
	}

}
