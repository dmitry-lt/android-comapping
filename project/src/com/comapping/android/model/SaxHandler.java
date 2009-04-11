/**This class provides method to build Map with SAX Parser. 
 * 
 * @author Dmitry Manayev
 * 
 */
package com.comapping.android.model;

import java.util.Date;
import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.comapping.android.Log;

class SaxHandler extends DefaultHandler {

	private boolean isMetaDataTag = false;
	private boolean isMapIDTag = false;
	private boolean isMapNameTag = false;
	private boolean isOwnerTag = false;
	private boolean isOwnerIDTag = false;
	private boolean isOwnerNameTag = false;
	private boolean isOwnerEmailTag = false;
	private boolean isTextTag = false;
	
	private boolean hasMeta = false;

	private int mapId = 0;
	private String mapName = null;

	private int ownerId;
	private String ownerName;
	private String ownerEmail;
	private User owner;
	private Map map;
	private String topicText;
	private Topic currentTopic = null;
	private long startTime;

	public void startDocument() {
		startTime = System.currentTimeMillis();
		Log.i(Log.modelTag, "SAX parsing started... \n");
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		try {
			if (localName.equals(MapBuilder.TOPIC_TEXT_TAG)) {
				isTextTag = true;
			} else if (localName.equals(MapBuilder.TOPIC_TAG)) {
				currentTopic = new Topic(currentTopic);
				getTopicAttributes(attributes);
				if (currentTopic.isRoot()) {
					map.setRoot(currentTopic);
				} else {
					currentTopic.getParent().addChild(currentTopic);
				}
			}  else if (localName.equals(MapBuilder.TOPIC_ICON_TAG)) {
				String iconName = attributes.getValue(MapBuilder.ICON_NAME_TAG);
				currentTopic.addIcon(Icon.parse(iconName));
			} else if (localName.equals(MapBuilder.TOPIC_NOTE_TAG)) {
				String note = attributes.getValue(MapBuilder.NOTE_TEXT_TAG);
				currentTopic.setNote(note);

			} else if (qName.equals(MapBuilder.TOPIC_TASK_TAG)) {
				String start = attributes.getValue(MapBuilder.TASK_START_TAG);
				String deadline = attributes.getValue(MapBuilder.TASK_DEADLINE_TAG);
				String responsible = attributes.getValue(MapBuilder.TASK_RESPONSIBLE_TAG);
				Task task = new Task(start, deadline, responsible);
				currentTopic.setTask(task);
			} else if (localName.equals(MapBuilder.TOPIC_ATTACHMENT_TAG)) {
				float fDate = Float.parseFloat(attributes.getValue(MapBuilder.ATTACHMENT_DATE_TAG));
				String filename = attributes.getValue(MapBuilder.ATTACHMENT_FILENAME_TAG);
				String key = attributes.getValue(MapBuilder.ATTACHMENT_KEY_TAG);
				int size = Integer.parseInt(attributes.getValue(MapBuilder.ATTACHMENT_SIZE_TAG));
				Attachment attachment = new Attachment(new Date(), filename, key, size);
				currentTopic.setAttachment(attachment);
			} else if (!hasMeta) {
				if (localName.equals(MapBuilder.METADATA_TAG)) {
					isMetaDataTag = true;
				} else if (localName.equals(MapBuilder.MAP_ID_TAG) && mapId == 0) {
					isMapIDTag = true;
				} else if (localName.equals(MapBuilder.MAP_NAME_TAG) && mapName == null) {
					isMapNameTag = true;
				} else if (localName.equals(MapBuilder.MAP_OWNER_TAG)) {
					isOwnerTag = true;
				} else if (localName.equals(MapBuilder.OWNER_ID_TAG) && isOwnerTag) {
					isOwnerIDTag = true;
				} else if (localName.equals(MapBuilder.OWNER_NAME_TAG) && isOwnerTag) {
					isOwnerNameTag = true;
				} else if (localName.equals(MapBuilder.OWNER_EMAIL_TAG) && isOwnerTag) {
					isOwnerEmailTag = true;
				}
			}
			
		} catch (NullPointerException e) {
			e.printStackTrace();
			Log.e(Log.modelTag, e.toString());
			throw new SAXException();
		} catch (NumberFormatException e) {
			e.printStackTrace();
			Log.e(Log.modelTag, e.toString());
			throw new SAXException();
		} catch (EnumParsingException e) {
			e.printStackTrace();
			Log.e(Log.modelTag, e.toString());
			throw new SAXException();
		}
	}
	
	private void getTopicAttributes(Attributes attributes) throws SAXException {
		try {
			String topicId = attributes.getValue("", MapBuilder.TOPIC_ID_TAG);
			if (topicId != null) {
				currentTopic.setId(Integer.parseInt(topicId));
			}
			
			String topicBgcolor = attributes.getValue("", MapBuilder.TOPIC_BGCOLOR_TAG);
			if (topicBgcolor != null) {
				currentTopic.setBgColor(Integer.parseInt(topicBgcolor));
			}
			
			String topicFlag = attributes.getValue("", MapBuilder.TOPIC_FLAG_TAG);
			if (topicFlag != null) {
				currentTopic.setFlag(Flag.parse(topicFlag));
			}
			
			String topicPriority = attributes.getValue("", MapBuilder.TOPIC_PRIORITY_TAG);
			if (topicPriority != null) {
				currentTopic.setPriority(Integer.parseInt(topicPriority));
			}
			
			String topicSmiley = attributes.getValue("", MapBuilder.TOPIC_SMILEY_TAG);
			if (topicSmiley != null) {
				currentTopic.setSmiley(Smiley.parse(topicSmiley));
			}
			
			String topicTaskCompletion = attributes.getValue("", MapBuilder.TOPIC_TASK_COMPLETION_TAG);
			if (topicTaskCompletion != null) {
				currentTopic.setTaskCompletion(TaskCompletion.parse(topicTaskCompletion));
			}
			
			String topicMapRef = attributes.getValue("", MapBuilder.TOPIC_MAP_REF_TAG);
			if (topicMapRef != null) {
				currentTopic.setMapRef(topicMapRef);
			}
		} catch (EnumParsingException e) {
			e.printStackTrace();
			Log.e(Log.modelTag, e.toString());
			throw new SAXException();
		}

	}

	public void characters(char[] ch, int start, int len) throws SAXException {
		String string = new String(ch, start, len);
		if (isTextTag) {
			topicText = string;
			isTextTag = false;
		} else if (!hasMeta) {
			if (isMapIDTag) {
				mapId = Integer.parseInt(string);
				isMapIDTag = false;
			} else if (isMapNameTag) {
				mapName = string;
				isMapNameTag = false;
			} else if (isOwnerIDTag) {
				ownerId = Integer.parseInt(string);
				isOwnerIDTag = false;
			} else if (isOwnerNameTag) {
				ownerName = string;
				isOwnerNameTag = false;
			} else if (isOwnerEmailTag) {
				ownerEmail = string;
				isOwnerEmailTag = false;
			}
		}
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		try {
			if (localName.equals(MapBuilder.TOPIC_TEXT_TAG)) {
				currentTopic.setText(topicText);
			} else if (localName.equals(MapBuilder.TOPIC_TAG)) {
				if (currentTopic.getParent() != null) {
					currentTopic = currentTopic.getParent();
				}
			} else if (!hasMeta) {
				if (localName.equals(MapBuilder.MAP_OWNER_TAG)) {
					owner = new User(ownerId, ownerName, ownerEmail);
					isOwnerTag = false;
				} else if (localName.equals(MapBuilder.METADATA_TAG)) {
					map = new Map(mapId);
					map.setName(mapName);
					map.setOwner(owner);
					isMetaDataTag = false;
					
					hasMeta = true;
				}
			}
		} catch (StringToXMLConvertionException e) {
			e.printStackTrace();
			Log.e(Log.modelTag, e.toString());
			throw new SAXException();
		}
	}

	public Map getMap() {
		return map;
	}

	public void endDocument() {
		long parsingTime = System.currentTimeMillis() - startTime;
		Log.w(Log.modelTag, "map was built successfully, parsing time: " + parsingTime);
	}
}