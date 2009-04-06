package com.comapping.android.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import android.graphics.Color;

import com.comapping.android.Log;

/**
 * @author Passichenko Victor and Yuri Zemlyanskiy
 * 
 */
public class Topic implements Iterable<Topic> {

	private int id;
	private Topic parent;
	
	private Date lastModificationDate;
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

	Topic(Topic parent) {
		this.parent = parent;
	}

	public Topic(int id, Topic parent) {
		this(parent);
		
		this.id = id;
		this.text = "";

		Log.d(Log.modelTag, "created " + this);
	}

	void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
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

	public void setText(String text) throws StringToXMLConvertionException {
		setFormattedText(FormattedTextBuilder.buildFormattedText(text));
		String unescText = getFormattedText().getSimpleText();

		Log.d(Log.modelTag, "set text=\"" + unescText + "\" in " + this);

		this.text = unescText;
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

	public Date getLastModificationDate() {
		return lastModificationDate;
	}

	public void setLastModificationDate(Date lastModificationDate) {
		// this.lastModificationDate = lastModificationDate;

		Log.d(Log.modelTag, "set LastModificationDate=\"" + lastModificationDate.toLocaleString() + "\" in " + this);
	}

	public int getBgColor() {
		return bgColor;
	}

	public void setBgColor(int bgColor) {
		this.bgColor = bgColor;

		Log.d(Log.modelTag, "set bgColor=\"" + bgColor + "\" in " + this);
	}

	public Flag getFlag() {
		return flag;
	}

	public void setFlag(Flag flag) {
		this.flag = flag;

		Log.d(Log.modelTag, "set flag=\"" + flag + "\" in " + this);
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;

		Log.d(Log.modelTag, "set priority=\"" + priority + "\" in " + this);
	}

	public Smiley getSmiley() {
		return smiley;
	}

	public void setSmiley(Smiley smiley) {
		this.smiley = smiley;

		Log.d(Log.modelTag, "set smiley=\"" + smiley + "\" in " + this);
	}

	public TaskCompletion getTaskCompletion() {
		return taskCompletion;
	}

	public void setTaskCompletion(TaskCompletion taskCompletion) {
		this.taskCompletion = taskCompletion;

		Log.d(Log.modelTag, "set taskCompletion=\"" + taskCompletion + "\" in " + this);
	}

	public void addIcon(Icon icon) {
		icons.addIcon(icon);

		Log.d(Log.modelTag, "add icon " + icon + " in " + this);
	}

	public void removeIcon(Icon icon) {
		icons.removeIcon(icon);

		Log.d(Log.modelTag, "remove icon " + icon + " from " + this);
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

		Log.d(Log.modelTag, "set note=\"" + note + "\" in " + this);
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;

		Log.d(Log.modelTag, "set task=\"" + task + "\" in " + this);
	}

	public void setAttachment(Attachment attachment) {
		this.attachment = attachment;
		
		Log.d(Log.modelTag, "set attachment=" + attachment + " in " + this);
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

		Log.d(Log.modelTag, "add " + child + " in " + this);
	}

	public void removeChildByIndex(int index) throws IndexOutOfBoundsException {
		Log.d(Log.modelTag, "remove " + getChildByIndex(index) + " from " + this);

		children.remove(index);
	}

	@Override
	public String toString() {
		return "[Topic: id=" + this.getId() + ", text=\"" + this.getText() + "\"]";
	}

	public TopicIterator iterator() {
		return new TopicIterator(this);
	}
}

class TopicIterator implements Iterator<Topic> {

	private int index = 0;
	private Topic topic;

	public TopicIterator(Topic topic) {
		this.topic = topic;
	}

	@Override
	public boolean hasNext() {
		return index < topic.getChildrenCount();
	}

	@Override
	public Topic next() throws IndexOutOfBoundsException {
		return topic.getChildByIndex(index++);
	}

	@Override
	public void remove() throws IndexOutOfBoundsException {
		topic.removeChildByIndex(index);
	}
}
