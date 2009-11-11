// Created by Fedor Burdun

package com.comapping.android;

import java.util.ArrayList;

public class AbstractNotificationGenerator {
	public ArrayList<Notification> getListNotification(String date) {
		ArrayList<Notification> al = new ArrayList<Notification>();
		al.add(new Notification("t1","t2","t3","t4","t5"));
		return al;
	}
}
