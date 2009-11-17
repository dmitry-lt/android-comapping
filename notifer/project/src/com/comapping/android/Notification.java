//Created by Fedor Burdun
//Example of Notification

package com.comapping.android;

import java.util.Date;

public class Notification {
	//must be changed
	public String date; //must be other type
	public String text;
	public String link;
	public String user;
	public String flag; //must be enum OR NOT?
	//public Date date;
	
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
