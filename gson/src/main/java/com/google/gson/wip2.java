package com.google.gson;

import java.text.SimpleDateFormat;
import java.util.Date;

public class wip2 {

    public static void main(String[] args) {
	Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").create();
	String json = "{'date': \"2015-11-19\"}";//  "{'date': \"2015-11-19T18:33:51Z\"}"; 
	MyDateJson datDate = gson.fromJson(json, MyDateJson.class);
	Date date = datDate.getDate();
    }
    
    private class MyDateJson
    {
	private Date date;

	public Date getDate() {
	    return date;
	}

	public void setDate(Date date) {
	    this.date = date;
	}
    }

}
