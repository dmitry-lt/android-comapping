package com.comapping.android.test;

import java.util.ArrayList;

public class AbstractNotificationGenerator {
	public ArrayList<String> getListNotification(int count) {
		ArrayList<String> al = new ArrayList<String>();
		for (Integer i=0;i<count;++i)
			al.add("Notification №"+i.toString());
		return al;
	}
}
