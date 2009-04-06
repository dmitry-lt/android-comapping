/**This class provides method to build Map with SAX Parser. 
 * 
 * @author Dmitry Manayev
 * 
 */
package com.comapping.android.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
	private boolean isNodeTag = false;
	private boolean isTextTag = false;

	private int MapId = 0;
	private String MapName;
	private int ownerId;
	private String ownerName;
	private String ownerEmail;
	private User owner;
	private Map map;
	private String topicText;
	private Topic currentTopic;
	private Topic parent;
	private long startTime;

	public void startDocument() {
		startTime = System.currentTimeMillis();
		Log.i(Log.modelTag, "SAX parsing started... \n");
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		int length = attributes.getLength();
		try {
			if (qName.equals(MapBuilder.METADATA_TAG)) {
				isMetaDataTag = true;
			} else if (qName.equals(MapBuilder.MAP_ID_TAG)) {
				isMapIDTag = true;
			} else if (qName.equals(MapBuilder.MAP_NAME_TAG)) {
				isMapNameTag = true;
			} else if (qName.equals(MapBuilder.MAP_OWNER_TAG)) {
				isOwnerTag = true;
			} else if (qName.equals(MapBuilder.OWNER_ID_TAG) && isOwnerTag) {
				isOwnerIDTag = true;
			} else if (qName.equals(MapBuilder.OWNER_NAME_TAG) && isOwnerTag) {
				isOwnerNameTag = true;
			} else if (qName.equals(MapBuilder.OWNER_EMAIL_TAG) && isOwnerTag) {
				isOwnerEmailTag = true;
			} else if (qName.equals(MapBuilder.TOPIC_TAG)) {
				isNodeTag = true;
				currentTopic = new Topic(currentTopic);
				parent = currentTopic.getParent();
				parent.addChild(currentTopic);
				boolean hasId = false;
				if (map.getRoot() == null)
					map.setRoot(currentTopic);
				if (attributes != null) {
					for (int i = 0; i < length; i++) {
						if (attributes.equals(MapBuilder.TOPIC_ID_TAG)) {
							currentTopic.setId(Integer.parseInt(attributes.getValue(i)));
							hasId = true;
						} else if (attributes.equals(MapBuilder.TOPIC_LAST_MODIFICATION_DATE_TAG)) {
							String strDate = attributes.getValue(i);
							SimpleDateFormat dateFormat = new SimpleDateFormat();
							dateFormat.applyPattern("yyyy-MM-dd HH:mm:ss");
							Date date = new Date();
							try {
								date = dateFormat.parse(strDate);
							} catch (ParseException e) {
								throw new DateParsingException();
							}
							currentTopic.setLastModificationDate(date);
						} else if (attributes.equals(MapBuilder.TOPIC_BGCOLOR_TAG)) {
							currentTopic.setBgColor(Integer.parseInt(attributes.getValue(i)));
						} else if (attributes.equals(MapBuilder.TOPIC_FLAG_TAG)) {
							String strFlag = attributes.getValue(i);
							currentTopic.setFlag(Flag.parse(strFlag));
						} else if (attributes.equals(MapBuilder.TOPIC_PRIORITY_TAG)) {
							currentTopic.setPriority(Integer.parseInt(attributes.getValue(i)));
						} else if (attributes.equals(MapBuilder.TOPIC_SMILEY_TAG)) {
							String strSmiley = attributes.getValue(i);
							currentTopic.setSmiley(Smiley.parse(strSmiley));
						} else if (attributes.equals(MapBuilder.TOPIC_TASK_COMPLETION_TAG)) {
							String strTaskCompletion = attributes.getValue(i);
							currentTopic.setTaskCompletion(TaskCompletion.parse(strTaskCompletion));
						} else if (attributes.equals(MapBuilder.TOPIC_MAP_REF_TAG)) {
							String mapRef = attributes.getValue(i);
							currentTopic.setMapRef(mapRef);
						} else if (!hasId) {
							throw new SAXException();
						}
					}
				}
			} else if (qName.equals(MapBuilder.TOPIC_TEXT_TAG)) {
				isTextTag = true;
			} else if (qName.equals(MapBuilder.TOPIC_ICON_TAG)) {
				String iconName = attributes.getValue(MapBuilder.ICON_NAME_TAG);
				currentTopic.addIcon(Icon.parse(iconName));
			} else if (qName.equals(MapBuilder.TOPIC_NOTE_TAG)) {
				String note = attributes.getValue(MapBuilder.NOTE_TEXT_TAG);
				currentTopic.setNote(note);
			} else if (qName.equals(MapBuilder.TOPIC_TASK_TAG)) {
				String deadline = attributes.getValue(MapBuilder.TASK_DEADLINE_TAG);
				String responsible = attributes.getValue(MapBuilder.TASK_RESPONSIBLE_TAG);
				Task task = new Task(deadline, responsible);
				currentTopic.setTask(task);
			} else if (qName.equals(MapBuilder.TOPIC_ATTACHMENT_TAG)) {
				float fDate = Float.parseFloat(attributes.getValue(MapBuilder.ATTACHMENT_DATE_TAG));
				String filename = attributes.getValue(MapBuilder.ATTACHMENT_FILENAME_TAG);
				String key = attributes.getValue(MapBuilder.ATTACHMENT_KEY_TAG);
				int size = Integer.parseInt(attributes.getValue(MapBuilder.ATTACHMENT_SIZE_TAG));
				Attachment attachment = new Attachment(new Date(), filename, key, size);
				currentTopic.setAttachment(attachment);
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
		} catch (DateParsingException e) {
			e.printStackTrace();
			Log.e(Log.modelTag, e.toString());
			throw new SAXException();
		}

	}

	public void characters(char[] ch, int start, int len) throws SAXException {
		String string = new String(ch, start, len);
		if (isMapIDTag) {
			MapId = Integer.parseInt(string);
			isMapIDTag = false;
		} else if (isMapNameTag) {
			MapName = string;
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
		} else if (isNodeTag) {
			if (isTextTag) {
				topicText = string;
			}
		}
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		try {
			if (qName.equals(MapBuilder.OWNER_ID_TAG)) {
				owner = new User(ownerId, ownerName, ownerEmail);
				isOwnerTag = false;
			} else if (qName.equals(MapBuilder.METADATA_TAG)) {
				map = new Map(MapId);
				map.setName(MapName);
				map.setOwner(owner);
				isMetaDataTag = false;
			} else if (qName.equals(MapBuilder.TOPIC_TAG)) {
				isNodeTag = false;
			} else if (qName.equals(MapBuilder.TOPIC_TEXT_TAG)) {
				currentTopic.setText(topicText);
				isTextTag = false;
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