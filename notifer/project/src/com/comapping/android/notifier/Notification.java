/*
 * Created by Fedor Burdun
 * Changed by Eugene Bakisov (17.11.2009)
 */
package com.comapping.android.notifier;

import java.lang.reflect.Field;
import java.util.Date;

/**
 * Contains some information about notification
 *
 * @author Fedor Burdun
 * @author Eugene Bakisov
 */
public class Notification {
	public final String title;
	public final String link;
	public final String description;

	public enum Category {
		Invitation, MapChanges, Tasks, Subscription, Update
	}

	public final Category category;

	public final Date date;

	public Notification(String title, String link, String description, Category category, Date date) {
		this.title = title;
		this.link = link;
		this.description = description;
		this.category = category;
		this.date = date;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Notification:[");
		for (Field f : Notification.class.getFields()) {
			try {
				sb.append(f.getName()).append(": \"").append(f.get(this)).append("\", ");
			} catch (IllegalAccessException e) {
				// would never happen
			}
		}
		sb.replace(sb.length() - 2, sb.length() - 1, "]");
		return sb.toString();
	}
}

