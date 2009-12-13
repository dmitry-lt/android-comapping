/*
 * Created by Fedor Burdun
 * Changed by Eugene Bakisov (17.11.2009)
 */
package com.comapping.android.notifier;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains some information about notification
 *
 * @author Fedor Burdun
 * @author Eugene Bakisov
 */
public class Notification {
	private String title;
	private String link;
	private String description;
	private Category category;
	private Date date;
	private String author;
	private long guid;
	private boolean permanentLink;

	public enum Category {
		Invitation, MapChanges, Tasks, Subscription, Update
	}

	private static Map<String, Category> categories;

	static {
		categories = new HashMap<String, Category>();
		categories.put("map changes", Category.MapChanges);
		categories.put("invitation", Category.Invitation);
		categories.put("tasks", Category.Tasks);
		categories.put("subscription", Category.Subscription);
		categories.put("update", Category.Update);
	}

	public Notification(String title, String link, String description,
						Category category, Date date, String author,
						long guid) {
		this.title = title;
		this.link = link;
		this.description = description;
		this.category = category;
		this.date = date;
		this.author = author;
		this.guid = guid;
	}

	public Notification(String title, String link, String description,
						String category, long date, String author, long guid) {
		if (!categories.containsKey(category)) {
			throw new IllegalArgumentException("Illegal category name");
		}
		this.title = title;
		this.link = link;
		this.description = description;
		this.category = categories.get(category);
		this.date = new Date(date);
		this.author = author;
		this.guid = guid;
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

	public String getTitle() {
		return title;
	}

	public String getLink() {
		return link;
	}

	public String getDescription() {
		return description;
	}

	public Category getCategory() {
		return category;
	}

	public Date getDate() {
		return date;
	}

	public String getAuthor() {
		return author;
	}

	public long getGuid() {
		return guid;
	}

	public boolean isPermanentLink() {
		return permanentLink;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public void setGuid(long guid) {
		this.guid = guid;
	}

	public void setPermanentLink(boolean permanentLink) {
		this.permanentLink = permanentLink;
	}
}

