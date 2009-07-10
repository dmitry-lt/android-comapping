package com.comapping.android.map.model.map.builder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.comapping.android.map.model.exceptions.DateParsingException;
import com.comapping.android.map.model.exceptions.MapParsingException;
import com.comapping.android.map.model.exceptions.StringToXMLConvertionException;
import com.comapping.android.map.model.map.Map;

abstract public class MapBuilder {
	static final String METADATA_TAG = "metadata";
	static final String MAP_ID_TAG = "id";
	static final String MAP_NAME_TAG = "name";
	static final String MAP_OWNER_TAG = "owner";
	static final String OWNER_ID_TAG = "id";
	static final String OWNER_NAME_TAG = "name";
	static final String OWNER_EMAIL_TAG = "email";

	static final String TOPIC_TAG = "node";
	static final String TOPIC_ID_ATTR = "id";
	static final String TOPIC_BGCOLOR_ATTR = "bgColor";
	static final String TOPIC_FLAG_ATTR = "flag";
	static final String TOPIC_PRIORITY_ATTR = "priority";
	static final String TOPIC_SMILEY_ATTR = "smiley";
	static final String TOPIC_TASK_COMPLETION_ATTR = "taskCompletion";
	static final String TOPIC_MAP_REF_TAG = "map_ref";
	static final String TOPIC_TEXT_TAG = "text";
	static final String TOPIC_ICON_TAG = "icon";
	static final String ICON_NAME_ATTR = "name";
	static final String TOPIC_NOTE_TAG = "note";
	static final String NOTE_TEXT_ATTR = "text";
	static final String TOPIC_TASK_TAG = "task";
	static final String TASK_START_ATTR = "start";
	static final String TASK_DEADLINE_ATTR = "deadline";
	static final String TASK_RESPONSIBLE_ATTR = "responsible";
	static final String TOPIC_ATTACHMENT_TAG = "attachment";
	static final String ATTACHMENT_DATE_ATTR = "date";
	static final String ATTACHMENT_FILENAME_ATTR = "filename";
	static final String ATTACHMENT_KEY_ATTR = "key";
	static final String ATTACHMENT_SIZE_ATTR = "size";

	/**
	 * @param stringDate
	 * 			date in comapping format
	 * @return date as an object of class Date 
	 * @throws DateParsingException
	 * 			when cannot parse string with date
	 */
	 static Date parseDate(String stringDate) throws DateParsingException{
		SimpleDateFormat dateFormat = new SimpleDateFormat();
		dateFormat.applyPattern("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		try {
			date = dateFormat.parse(stringDate);
		} catch (ParseException e) {
			throw new DateParsingException();
		}
		return date;
	}
	
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