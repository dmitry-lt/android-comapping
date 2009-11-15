// Created by Fedor Burdun
// Changed by Valery Petrov (15.11.2009)

package com.comapping.android;

import java.util.ArrayList;

public class AbstractNotificationGenerator {
	public ArrayList<Notification> getListNotification(String date) {
		ArrayList<Notification> al = new ArrayList<Notification>();
		al.add(new Notification("10.10.10","Notification 10.10.10","t3","t4","t5"));
		al.add(new Notification("11.10.10","Notification 11.10.10","t3","t4","t5"));
		al.add(new Notification("12.10.10","Notification 12.10.10","t3","t4","t5"));
		al.add(new Notification("15.10.10","Notification 15.10.10","t3","t4","t5"));
		al.add(new Notification("18.10.10","Notification 18.10.10","t3","t4","t5"));
		al.add(new Notification("19.10.10","Notification 19.10.10","t3","t4","t5"));
		return al;
	}
}
