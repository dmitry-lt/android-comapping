/*
 * Created by Fedor Burdun
 * Changed by Valery Petrov (15.11.2009)
 * Changed by Eugene Bakisov (17.11.2009)
 */
package com.comapping.android.notifier;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * This class provides few ways to generate notifications.
 *
 * @author Fedor Burdun
 * @see Notification
 */
public class AbstractNotificationGenerator {

	/**
	 * Generate list of notifications by calls {@code generateNotification}
	 * method with date in interval from {@code begin} to {@code end} with
	 * selected {@code step} and {@code probability} of generation
	 *
	 * @param begin	   - date of begining generation
	 * @param end		 - date of ending generation
	 * @param step		- time in milliseconds between notification generations
	 * @param probability - probability that notification will generate
	 * @return list of notifications
	 * @see Notification
	 */
	public static List<Notification> generateNotificationList(Date begin, Date end, long step, double probability) {
		Random rand = new Random();
		List<Notification> list = new ArrayList<Notification>();

		for (; begin.before(end); begin.setTime(begin.getTime() + step)) {
			if (rand.nextDouble() < probability) {
				list.add(generateNotification((Date) begin.clone()));
			}
		}

		return list;
	}

	/**
	 * Generate list of notifications with random size between 0 and 20
	 * by calls {@code generateNotifiction(Date,int)} method
	 *
	 * @param date - date of notification generation
	 * @return list of notification
	 * @see Notification
	 */
	public static List<Notification> generateNotificationList(Date date) {
		return generateNotificationList(date, new Random().nextInt(20));
	}

	/**
	 * Generate list of notification with selected {@code size}.
	 * All notification will generate with {@code date} as date of publishing
	 * by calls {@code generateNotification} method
	 *
	 * @param date	 - date of notification generations
	 * @param listSize - size of result list
	 * @return list of notification
	 * @see Notification
	 */
	public static List<Notification> generateNotificationList(Date date, int listSize) {
		ArrayList<Notification> list = new ArrayList<Notification>();
		for (int i = 0; i < listSize; i++) {
			list.add(generateNotification(date));
		}
		return list;
	}

	/**
	 * Generate notification with title, description and link depends of
	 * random chosen category and {@code date} as date of publishing
	 *
	 * @param date - date of notification generation
	 * @return notification
	 * @see Notification
	 */
	public static Notification generateNotification(Date date) {
		String title;
		String description;
		String link;
		Notification.Category category;

		// choose random category:
		Notification.Category[] categories = Notification.Category.values();
		Random rand = new Random();
		category = categories[rand.nextInt(categories.length)];
		// set title, link and description depends on chosen category:
		switch (category) {
			case Invitation:
				title = "The map <%MapName> has been shared with you";
				link = "http://go.comapping.com/comapping.html#email=???;mapid=???";
				description = "<%Name> <%Surname> has shared a map with you: <%MapName>";
				break;
			case MapChanges:
				title = "The map <%MapName> was changed";
				link = "http://go.comapping.com/comapping.html#mapid=???";
				description = "The map <%MapName> was just changed by <%Name> <%Surname>";
				break;
			case Subscription:
				title = "Something with you subscription on Comapping.com";
				link = "http://www.comapping.com";
				description = "Some description";
				break;
			case Tasks:
				title = "The task <%TaskName> is overdue";
				link = "http://go.comapping.com/comapping.html#mapid=???&amp;topicid=???";
				description = "Some task desctription";
				break;
			case Update:
				title = "New version of Comapping is available";
				link = "http://go.comapping.com/comapping.html?refresh=automatic";
				description = "New version of Comapping is available. Please reload the application";
				break;
			default:
				throw new RuntimeException("Unknown category");
		}

		return new Notification(title, link, description, category, date);
	}
}