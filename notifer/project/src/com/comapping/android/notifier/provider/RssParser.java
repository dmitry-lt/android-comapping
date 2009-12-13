package com.comapping.android.notifier.provider;

import android.os.Bundle;
import android.util.Log;
import com.comapping.android.notifier.Notification;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

	public static List<Notification> parse(InputStream source) throws IOException, SAXException, ParserConfigurationException {
		//printInputStreamContent(source);
		//TODO realize RssParser;
		// return AbstractNotificationGenerator.generateNotificationList(new Date(), 1);
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		RssParserHandler handler = new RssParserHandler();
		parser.parse(source, handler);
		return handler.getNotification();
	}

	private static void printInputStreamContent(InputStream input) {
		Log.d(LOG_TAG, "InputStream reading.");
		StringBuffer content = new StringBuffer();
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(input), 8 * 1024
		);

		try {
			String line = reader.readLine();
			if (line != null) {
				Log.d(LOG_TAG, "Yay! InputStream isn't empty.");
				content.append(line);
				String separator = System.getProperty("line.separator");
				while ((line = reader.readLine()) != null) {
					content.append(separator).append(line);
				}
			} else {
				Log.d(LOG_TAG, "InputStream is empty.");
			}
		} catch (IOException e) {
			Log.e(LOG_TAG, e.toString());
		} finally {
			try {
				input.close();
			} catch (IOException e) {
				Log.e(LOG_TAG, e.toString());
			}
		}
		Log.d(LOG_TAG, content.toString());
	}

	private static class RssParserHandler extends DefaultHandler {
		private StringBuilder buffer;
		private List<Notification> parsedNotifications;
		private Tag lastInterestingTag;
		private boolean notificationInfoParsing;
		private boolean endParsing;
		private Bundle notification;

		private enum Tag {
			item, title, link, description, category, pubDate, author, guid
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
			lastInterestingTag = null;
			notification = new Bundle();
		}

		@Override
		public void endDocument() throws SAXException {
			endParsing = true;
		}

		@Override
		public void startElement(String uri, String localName, String qName,
								 Attributes attributes) throws SAXException {
			String tagName = (qName.equals("") ? localName : qName);
			try {
				Tag currentTag = Tag.valueOf(tagName);
				if (currentTag.equals(Tag.item)) {
					// we find "Item" tag and now we will parse notification info
					notificationInfoParsing = true;
				} else {
					if (notificationInfoParsing) {
						lastInterestingTag = currentTag;
					}
				}
			} catch (IllegalArgumentException e) {
				// continue parsing if we aren't interesting in current tag
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (lastInterestingTag != null) {
				String content = buffer.toString();
				switch (lastInterestingTag) {
					case title:
						notification.putString("title", content);
						break;
					case link:
						notification.putString("link", content);
						break;
					case description:
						notification.putString("desc", content);
						break;
					case category:
						notification.putString("category", content);
						break;
					case pubDate:
						notification.putLong("time", (new Date(content)).getTime());
						break;
					case author:
						notification.putString("author", content);
						break;
					case guid:
						notification.putLong("guid", Long.valueOf(content));
						break;
				}
				buffer = null;
				lastInterestingTag = null;
			} else if (notificationInfoParsing) {
				// that's mean we meet "</item>" tag
				String[] fieldNames = {"title", "link", "desc", "category",
						"time", "author", "guid"};
				for (String field : fieldNames) {
					if (!notification.containsKey(field)) {
						throw new RuntimeException("Wrong XML File");
					}
				}
				Notification parsedNotification =
						new Notification(
								notification.getString("title"),
								notification.getString("link"),
								notification.getString("desc"),
								notification.getString("category"),
								notification.getLong("time"),
								notification.getString("author"),
								notification.getLong("guid")
						);

				parsedNotifications.add(parsedNotification);
				notification.clear();
				notificationInfoParsing = false;
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if (lastInterestingTag != null) {
				if (buffer == null) {
					buffer = new StringBuilder();
				}
				buffer.append(ch, start, length);
			}
		}
	}
}
