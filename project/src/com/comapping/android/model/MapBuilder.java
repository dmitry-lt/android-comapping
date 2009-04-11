package com.comapping.android.model;

abstract public class MapBuilder {
	static final String METADATA_TAG = "metadata";
	static final String MAP_ID_TAG = "id";
	static final String MAP_NAME_TAG = "name";
	static final String MAP_OWNER_TAG = "owner";
	static final String OWNER_ID_TAG = "id";
	static final String OWNER_NAME_TAG = "name";
	static final String OWNER_EMAIL_TAG = "email";

	static final String TOPIC_TAG = "node";
	static final String TOPIC_ID_TAG = "id";
	static final String TOPIC_LAST_MODIFICATION_DATE_TAG = "LastModificationData";
	static final String TOPIC_BGCOLOR_TAG = "bgColor";
	static final String TOPIC_FLAG_TAG = "flag";
	static final String TOPIC_PRIORITY_TAG = "priority";
	static final String TOPIC_SMILEY_TAG = "smiley";
	static final String TOPIC_TASK_COMPLETION_TAG = "taskCompletion";
	static final String TOPIC_TEXT_TAG = "text";
	static final String TOPIC_ICON_TAG = "icon";
	static final String ICON_NAME_TAG = "name";
	static final String TOPIC_NOTE_TAG = "note";
	static final String NOTE_TEXT_TAG = "text";
	static final String TOPIC_TASK_TAG = "task";
	static final String TASK_START_TAG = "start";
	static final String TASK_DEADLINE_TAG = "deadline";
	static final String TASK_RESPONSIBLE_TAG = "responsible";
	static final String TOPIC_ATTACHMENT_TAG = "attachment";
	static final String ATTACHMENT_DATE_TAG = "date";
	static final String ATTACHMENT_FILENAME_TAG = "filename";
	static final String ATTACHMENT_KEY_TAG = "key";
	static final String ATTACHMENT_SIZE_TAG = "size";

	static final String TOPIC_MAP_REF_TAG = "map_ref";

	/**
	 * Method that builds Map from comap XML file.
	 * 
	 * @param xmlDocument
	 *            text from comap XML file
	 * @return built map
	 * @throws StringToXMLConvertionException
	 *             when cannot convert given string to XML
	 * @throws MapParsingException
	 *             when given XML document has wrong format
	 */
	abstract public Map buildMap(String xmlDocument) throws StringToXMLConvertionException, MapParsingException;
}