package com.comapping.android.model.map.builder;

import java.util.Date;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.comapping.android.Log;
import com.comapping.android.model.exceptions.EnumParsingException;
import com.comapping.android.model.exceptions.StringToXMLConvertionException;
import com.comapping.android.model.map.Attachment;
import com.comapping.android.model.map.Flag;
import com.comapping.android.model.map.Icon;
import com.comapping.android.model.map.Map;
import com.comapping.android.model.map.Smiley;
import com.comapping.android.model.map.Task;
import com.comapping.android.model.map.TaskCompletion;
import com.comapping.android.model.map.Topic;
import com.comapping.android.model.map.User;

import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;

public class AndroidSaxParser {

	private static int base = 1000;

	private static int TOPIC_ID_TAG_HASHCODE = mod(MapBuilder.TOPIC_ID_ATTR
			.hashCode());
	private static int TOPIC_BGCOLOR_TAG_HASHCODE = mod(MapBuilder.TOPIC_BGCOLOR_ATTR
			.hashCode());
	private static int TOPIC_FLAG_TAG_HASHCODE = mod(MapBuilder.TOPIC_FLAG_ATTR
			.hashCode());
	private static int TOPIC_PRIORITY_TAG_HASHCODE = mod(MapBuilder.TOPIC_PRIORITY_ATTR
			.hashCode());
	private static int TOPIC_SMILEY_TAG_HASHCODE = mod(MapBuilder.TOPIC_SMILEY_ATTR
			.hashCode());
	private static int TOPIC_TASK_COMPLETION_TAG_HASHCODE = mod(MapBuilder.TOPIC_TASK_COMPLETION_ATTR
			.hashCode());
	private static int TOPIC_MAP_REF_TAG_HASHCODE = mod(MapBuilder.TOPIC_MAP_REF_TAG
			.hashCode());

	private String[] attributesMap = new String[base]; // make it global for
	// memory and time
	// saving

	private int mapId = 0;
	private String mapName = null;
	private Element parent;
	
	private int ownerId;
	private String ownerName;
	private String ownerEmail;
	private User owner;
	private Map map;
	private String topicText;
	private String noteText;
	private Topic currentTopic = null;
	private long startTime;

	/**
	 * Method for getting module on base, with always positive result
	 * 
	 * @param value
	 * @return value mod base
	 */
	private static int mod(int value) {
		int tmp = value % base;

		return (tmp >= 0) ? tmp : tmp + base;
	}

	public ContentHandler newContentHandler() {

		RootElement root = new RootElement("mindmap");

		root.setStartElementListener(new StartElementListener() {
			public void start(Attributes attributes) {
				startTime = System.currentTimeMillis();
				Log.i(Log.MODEL_TAG, "AndroidSAX parsing started... \n");
			}
		});

		Element metadata = root.getChild(MapBuilder.METADATA_TAG);

		metadata.getChild(MapBuilder.MAP_ID_TAG).setEndTextElementListener(
				new EndTextElementListener() {
					public void end(String body) {
						mapId = Integer.parseInt(body);
					}
				});

		metadata.getChild(MapBuilder.MAP_NAME_TAG).setEndTextElementListener(
				new EndTextElementListener() {
					public void end(String body) {
						mapName = body;
					}
				});

		Element mapOwner = root.getChild(MapBuilder.MAP_OWNER_TAG);

		mapOwner.getChild(MapBuilder.OWNER_ID_TAG).setEndTextElementListener(
				new EndTextElementListener() {
					public void end(String body) {
						ownerId = Integer.parseInt(body);
					}
				});

		mapOwner.getChild(MapBuilder.OWNER_NAME_TAG).setEndTextElementListener(
				new EndTextElementListener() {
					public void end(String body) {
						ownerName = body;
					}
				});

		mapOwner.getChild(MapBuilder.OWNER_EMAIL_TAG)
				.setEndTextElementListener(new EndTextElementListener() {
					public void end(String body) {
						ownerEmail = body;
					}
				});

		mapOwner.setEndElementListener(new EndElementListener() {
			public void end() {
				owner = new User(ownerId, ownerName, ownerEmail);
			}
		});

		metadata.setEndElementListener(new EndElementListener() {
			public void end() {
				map = new Map(mapId);
				map.setName(mapName);
				map.setOwner(owner);
			}
		});

		// Watch TOPIC_TAG starts
		Element topic = root.getChild(MapBuilder.TOPIC_TAG);
		BuildTopic(topic);
		BuildTopic(topic.getChild(MapBuilder.TOPIC_TAG));
		
		
		root.setEndElementListener(new EndElementListener() {
			public void end() {
				long parsingTime = System.currentTimeMillis() - startTime;
				Log.i(Log.MODEL_TAG,
						"map was built with AndroidSAX successfully, parsing time: "
								+ parsingTime);
			}
		});

		return root.getContentHandler();
	}

	
	private  void BuildTopic(Element topic){
		
		topic.setStartElementListener(new StartElementListener() {
			public void start(Attributes attributes) {
				currentTopic = new Topic(currentTopic);
				try {
					getTopicAttributes(attributes);
				} catch (SAXException e) {
					e.printStackTrace();
					Log.e(Log.MODEL_TAG, e.toString());
				}
				if (currentTopic.isRoot()) {
					map.setRoot(currentTopic);
				} else {
					currentTopic.getParent().addChild(currentTopic);
				}
			}
		});
		
		topic.getChild(MapBuilder.TOPIC_ICON_TAG).setStartElementListener(
				new StartElementListener() {
					public void start(Attributes attributes) {
						String iconName = attributes
								.getValue(MapBuilder.ICON_NAME_ATTR);
						try {
							currentTopic.addIcon(Icon.parse(iconName));
						} catch (EnumParsingException e) {
							e.printStackTrace();
							Log.e(Log.MODEL_TAG, e.toString());
						}
					}
				});

		// Watch NOTE_TAG starts
		Element note = topic.getChild(MapBuilder.TOPIC_NOTE_TAG);

		note.setEndTextElementListener(new EndTextElementListener() {
			public void end(String Body) {
				noteText += Body;
			}
		});

		note.setStartElementListener(new StartElementListener() {
			public void start(Attributes attributes) {
				String note = attributes.getValue(MapBuilder.NOTE_TEXT_ATTR);
				if (note != null) {
					currentTopic.setNote(note);
					noteText = null;
				} else {
					noteText = "";
				}
			}
		});

		note.setEndElementListener(new EndElementListener() {
			public void end() {
				if (noteText != null) {
					currentTopic.setNote(noteText);
				}
			}
		});
		// Watch NOTE_TAG ends.

		topic.getChild(MapBuilder.TOPIC_TASK_TAG).setStartElementListener(
				new StartElementListener() {
					public void start(Attributes attributes) {
						String start = attributes
								.getValue(MapBuilder.TASK_START_ATTR);
						String deadline = attributes
								.getValue(MapBuilder.TASK_DEADLINE_ATTR);
						String responsible = attributes
								.getValue(MapBuilder.TASK_RESPONSIBLE_ATTR);
						Task task = new Task(start, deadline, responsible);
						currentTopic.setTask(task);
					}
				});

		topic.getChild(MapBuilder.TOPIC_ATTACHMENT_TAG)
				.setStartElementListener(new StartElementListener() {
					public void start(Attributes attributes) {
						Date date = new Date((long) Float.parseFloat(attributes
								.getValue(MapBuilder.ATTACHMENT_DATE_ATTR)));
						String filename = attributes
								.getValue(MapBuilder.ATTACHMENT_FILENAME_ATTR);
						String key = attributes
								.getValue(MapBuilder.ATTACHMENT_KEY_ATTR);
						int size = Integer.parseInt(attributes
								.getValue(MapBuilder.ATTACHMENT_SIZE_ATTR));
						Attachment attachment = new Attachment(date, filename,
								key, size);
						currentTopic.setAttachment(attachment);
					}
				});

		topic.getChild(MapBuilder.TOPIC_TEXT_TAG).setEndTextElementListener(
				new EndTextElementListener() {
					public void end(String body) {
						topicText = body;
						try {
							currentTopic.setHtmlText(topicText);
						} catch (StringToXMLConvertionException e) {
							e.printStackTrace();
							Log.e(Log.MODEL_TAG, e.toString());
						}
					}
				});
		
		topic.setEndElementListener(new EndElementListener() {
			public void end() {
				if (currentTopic.getParent() != null) {
					currentTopic = currentTopic.getParent();
				}
			}
		});
	}
	
	
	private void getTopicAttributes(Attributes attributes) throws SAXException {
		try {
			int length = attributes.getLength();
			for (int i = 0; i < length; i++) {
				attributesMap[mod(attributes.getLocalName(i).hashCode())] = attributes
						.getValue(i);
			}

			if (attributesMap[TOPIC_ID_TAG_HASHCODE] != null) {
				currentTopic.setId(Integer
						.parseInt(attributesMap[TOPIC_ID_TAG_HASHCODE]));
				attributesMap[TOPIC_ID_TAG_HASHCODE] = null;
			}

			if (attributesMap[TOPIC_BGCOLOR_TAG_HASHCODE] != null) {
				currentTopic.setBgColor(Integer
						.parseInt(attributesMap[TOPIC_BGCOLOR_TAG_HASHCODE]));
				attributesMap[TOPIC_BGCOLOR_TAG_HASHCODE] = null;
			}

			if (attributesMap[TOPIC_FLAG_TAG_HASHCODE] != null) {
				currentTopic.setFlag(Flag
						.parse(attributesMap[TOPIC_FLAG_TAG_HASHCODE]));
				attributesMap[TOPIC_FLAG_TAG_HASHCODE] = null;
			}

			if (attributesMap[TOPIC_PRIORITY_TAG_HASHCODE] != null) {
				currentTopic.setPriority(Integer
						.parseInt(attributesMap[TOPIC_PRIORITY_TAG_HASHCODE]));
				attributesMap[TOPIC_PRIORITY_TAG_HASHCODE] = null;
			}

			if (attributesMap[TOPIC_SMILEY_TAG_HASHCODE] != null) {
				currentTopic.setSmiley(Smiley
						.parse(attributesMap[TOPIC_SMILEY_TAG_HASHCODE]));
				attributesMap[TOPIC_SMILEY_TAG_HASHCODE] = null;
			}

			if (attributesMap[TOPIC_TASK_COMPLETION_TAG_HASHCODE] != null) {
				currentTopic
						.setTaskCompletion(TaskCompletion
								.parse(attributesMap[TOPIC_TASK_COMPLETION_TAG_HASHCODE]));
				attributesMap[TOPIC_TASK_COMPLETION_TAG_HASHCODE] = null;
			}

			if (attributesMap[TOPIC_MAP_REF_TAG_HASHCODE] != null) {
				currentTopic
						.setMapRef(attributesMap[TOPIC_MAP_REF_TAG_HASHCODE]);
				attributesMap[TOPIC_MAP_REF_TAG_HASHCODE] = null;
			}
		} catch (EnumParsingException e) {
			e.printStackTrace();
			Log.e(Log.MODEL_TAG, e.toString());
			throw new SAXException();
		}

	}

	public Map getMap() {
		return map;
	}

}
