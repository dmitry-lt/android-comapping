package com.comapping.android.notifier.provider;

import android.util.Log;
import com.comapping.android.notifier.Notification;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Eugene Bakisov
 * Date: 09.12.2009
 * Time: 15:28:14
 */
public class RssParser {
	private static String LOG_TAG = "RssParser";

	public static List<Notification> parse(InputStream source) {
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			RssParserHandler handler = new RssParserHandler();
			parser.parse(source, handler);
			return handler.getNotification();
		} catch (ParserConfigurationException e) {
			Log.e(LOG_TAG, "Exception: ", e);
		} catch (SAXException e) {
			Log.e(LOG_TAG, "Exception: ", e);
		} catch (IOException e) {
			Log.e(LOG_TAG, "Exception: ", e);
		}
		return null;
	}

	private static class RssParserHandler extends DefaultHandler {
		private StringBuilder buffer;
		private List<Notification> parsedNotifications;
		private Tag parsedTag;
		private boolean endParsing;
		private boolean notificationInfoParsing;
		private String title;
		private String link;
		private String desc;
		private Notification.Category category;
		private Date date;

		private enum Tag {
			item, title, link, description, category, pubDate
		}

		public List<Notification> getNotification() {
			if (!endParsing) {
				throw new IllegalStateException("XML document parsing process wasn't finished");
			} else {
				return parsedNotifications;
			}
		}

		@Override
		public void startDocument() throws SAXException {
			buffer = null;
			parsedNotifications = new ArrayList<Notification>();
			endParsing = false;
			notificationInfoParsing = false;
			parsedTag = null;
		}

		@Override
		public void endDocument() throws SAXException {
			endParsing = true;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			String tagName = (qName.equals("") ? localName : qName);
			try {
				Tag currentTag = Tag.valueOf(tagName);
				if (currentTag.equals(Tag.item)) {
					// we find "Item" tag and now we will parse notification info
					notificationInfoParsing = true;
				} else {
					if (notificationInfoParsing) {
						parsedTag = currentTag;
					}
				}
			} catch (IllegalArgumentException e) {
				// continue parsing if we aren't interesting in current tag
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (parsedTag != null) {
				String content = buffer.toString();
				switch (parsedTag) {
					case title:
						title = content;
						break;
					case link:
						link = content;
						break;
					case description:
						desc = content;
						break;
					case category:
						if (content.equals("invitation")) {
							category = Notification.Category.Invitation;
						} else if (content.equals("map changes")) {
							category = Notification.Category.MapChanges;
						} else if (content.equals("tasks")) {
							category = Notification.Category.Tasks;
						} else if (content.equals("subscription")) {
							category = Notification.Category.Subscription;
						} else if (content.equals("update")) {
							category = Notification.Category.Update;
						}
						break;
					case pubDate:
						// TODO find right way to do this:
						date = new Date(content);
						break;
				}
				buffer = null;
				parsedTag = null;
			} else if (notificationInfoParsing) {
				// that's mean we meet "</item>" tag
				Notification parsedNotification =
						new Notification(title, link, desc, category, date);
				parsedNotifications.add(parsedNotification);
				title = link = desc = null;
				date = null;
				category = null;
				notificationInfoParsing = false;
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if (parsedTag != null) {
				if (buffer == null) {
					buffer = new StringBuilder();
				}
				buffer.append(ch, start, length);
			}
		}
	}
}
