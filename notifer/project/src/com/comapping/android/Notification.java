//Created by Fedor Burdun
//Example of Notification

package com.comapping.android;

public class Notification {
	//must be changed
	public String date;
	public String text;
	public String link;
	public String user;
	public String flag; //must be enum
	public boolean beWatched;
	
	Notification(String date, String text, String link, String user, String flag) {
		this.date = date;
		this.text = text;
		this.link = link;
		this.user = user;
		this.flag = flag;
		this.beWatched = false;
	}
}
