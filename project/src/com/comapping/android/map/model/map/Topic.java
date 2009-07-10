package com.comapping.android.map.model.map;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.graphics.Color;

import com.comapping.android.Log;
import com.comapping.android.map.model.exceptions.StringToXMLConvertionException;
import com.comapping.android.map.model.text.FormattedText;
import com.comapping.android.map.model.text.TextFormat;
import com.comapping.android.map.model.text.builder.FormattedTextSaxBuilder;

/**
 * @author Passichenko Victor and Yuri Zemlyanskiy
 * 
 */
public class Topic implements Serializable {
	private static final long serialVersionUID = 9028218616007509606L;

	private int id;
	private Topic parent;

	private int bgColor = Color.WHITE;
	private Flag flag;
	private int priority;
	private Smiley smiley;
	private TaskCompletion taskCompletion;

	private String text;
	private FormattedText formattedText;
	private ArrayList<Topic> children = new ArrayList<Topic>();

	private Icons icons = new Icons();
	private String note;
	private Task task;
	private Attachment attachment;

	private String mapRef;

	public Topic(Topic parent) {
		this.parent = parent;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public boolean isRoot() {
		return parent == null;
	}

	public Topic getParent() {
		return parent;
	}

	public String getMapRef() {
		return mapRef;
	}

	public void setMapRef(String mapRef) {
		this.mapRef = mapRef;
	}

	public boolean isFolder() {
		return (mapRef == null);
	}

	public void setHtmlText(String htmlText)
			throws StringToXMLConvertionException {
		setFormattedText(FormattedTextSaxBuilder.buildFormattedText(htmlText));
		String unescText = getFormattedText().getSimpleText();

		Log.d(Log.MODEL_TAG, "set text=\"" + unescText + "\" in " + this);

		this.text = unescText;
	}

	public void setText(String text) {
		setFormattedText(new FormattedText(text, new TextFormat()));
		this.text = text;
	}

	public String getText() {
		return this.text;
	}

	public FormattedText getFormattedText() {
		return formattedText;
	}

	public void setFormattedText(FormattedText formattedText) {
		this.formattedText = formattedText;
	}

	public int getBgColor() {
		return bgColor;
	}

	public void setBgColor(int bgColor) {
		this.bgColor = bgColor;

		Log.d(Log.MODEL_TAG, "set bgColor=\"" + bgColor + "\" in " + this);
	}

	public Flag getFlag() {
		return flag;
	}

	public void setFlag(Flag flag) {
		this.flag = flag;

		Log.d(Log.MODEL_TAG, "set flag=\"" + flag + "\" in " + this);
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;

		Log.d(Log.MODEL_TAG, "set priority=\"" + priority + "\" in " + this);
	}

	public Smiley getSmiley() {
		return smiley;
	}

	public void setSmiley(Smiley smiley) {
		this.smiley = smiley;

		Log.d(Log.MODEL_TAG, "set smiley=\"" + smiley + "\" in " + this);
	}

	public TaskCompletion getTaskCompletion() {
		return taskCompletion;
	}

	public void setTaskCompletion(TaskCompletion taskCompletion) {
		this.taskCompletion = taskCompletion;

		Log.d(Log.MODEL_TAG, "set taskCompletion=\"" + taskCompletion
				+ "\" in " + this);
	}

	public void addIcon(Icon icon) {
		icons.addIcon(icon);

		Log.d(Log.MODEL_TAG, "add icon " + icon + " in " + this);
	}

	public void removeIcon(Icon icon) {
		icons.removeIcon(icon);

		Log.d(Log.MODEL_TAG, "remove icon " + icon + " from " + this);
	}

	public boolean hasIcon(Icon icon) {
		return icons.hasIcon(icon);
	}

	public int getIconCount() {
		return icons.getCount();
	}

	public List<Icon> getIcons() {
		return icons.getIcons();
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;

		Log.d(Log.MODEL_TAG, "set note=\"" + note + "\" in " + this);
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;

		Log.d(Log.MODEL_TAG, "set task=\"" + task + "\" in " + this);
	}

	public void setAttachment(Attachment attachment) {
		this.attachment = attachment;

		Log.d(Log.MODEL_TAG, "set attachment=" + attachment + " in " + this);
	}

	public Attachment getAttachment() {
		return attachment;
	}

	public int getChildrenCount() {
		return children.size();
	}

	public Topic getChildByIndex(int index) throws IndexOutOfBoundsException {
		return children.get(index);
	}

	public Topic[] getChildTopics() {
		Topic[] result = new Topic[children.size()];
		children.toArray(result);
		return result;
	}

	public void addChild(Topic child) {
		children.add(child);

		Log.d(Log.MODEL_TAG, "add " + child + " in " + this);
	}

	public void removeChildByIndex(int index) throws IndexOutOfBoundsException {
		Log.d(Log.MODEL_TAG, "remove " + getChildByIndex(index) + " from "
				+ this);

		children.remove(index);
	}

	public void removeAllChildTopics() {
		children = new ArrayList<Topic>();
	}

	public String toString() {
		return "[Topic: id=" + this.getId() + ", text=\"" + this.getText()
				+ "\"]";
	}

}