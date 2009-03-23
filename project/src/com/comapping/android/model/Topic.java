package com.comapping.android.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import android.graphics.Color;

import com.comapping.android.Log;

/**
 * @author Passichenko Victor and Yuri Zemlyanskiy
 * 
 */
public class Topic implements Iterable<Topic> {

	public Object renderData;

	private int id;
	private String lastModificationDate;
	private int bgColor = Color.WHITE;
	private Flag flag;
	private int priority;
	private Smiley smiley;
	private TaskCompletion taskCompletion;

	private String text;
	private ArrayList<Topic> children = new ArrayList<Topic>();

	private Icons icons = new Icons();
	private String note;
	private Task task;

	Topic() {
	}

	public Topic(int id) {
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

	public void setText(String text) {
		text = unescape(text);
		Log.d(Log.modelTag, "set text=\"" + text + "\" in " + this);

		this.text = text;
	}

	public String getText() {
		return this.text;
	}

	public String getLastModificationDate() {
		return lastModificationDate;
	}

	public void setLastModificationDate(String lastModificationDate) {
		this.lastModificationDate = lastModificationDate;

		Log.d(Log.modelTag, "set setLastModificationDate=\""
				+ lastModificationDate + "\" in " + this);
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

		Log.d(Log.modelTag, "set taskCompletion=\"" + taskCompletion + "\" in "
				+ this);
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
		Log.d(Log.modelTag, "remove " + getChildByIndex(index) + " from "
				+ this);

		children.remove(index);
	}

	@Override
	public String toString() {
		return "[Topic: id=" + this.getId() + ", text=\"" + this.getText()
				+ "\"]";
	}

	public TopicIterator iterator() {
		return new TopicIterator(this);
	}

	private String unescape(String str) {
		str = str.replace("&amp;", "&");
		str = str.replace("&lt;", "<");
		str = str.replace("&gt;", ">");
		str = str.replace("&#039;", "\\");
		str = str.replace("&#39;", "'");
		str = str.replace("&quot;", "\"");
		str = str.replace("&lt;", "<");
		return str;
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
