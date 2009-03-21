package com.comapping.android.model;

import java.util.ArrayList;
import java.util.Iterator;

import com.comapping.android.Log;

/**
 * @author Passichenko Victor and Yuri Zemlyanskiy
 * 
 */
public class Topic implements Iterable<Topic> {

	public Object renderData;

	private final int id;
	private String text;
	private ArrayList<Topic> children;

	public Topic(int id) {
		this.id = id;
		this.children = new ArrayList<Topic>();
		this.text = "";

		Log.d(Log.modelTag, "created " + this);
	}

	public Topic(int id, String text) {
		this.id = id;
		this.children = new ArrayList<Topic>();
		this.text = text;

		Log.d(Log.modelTag, "created " + this);
	}

	public int getId() {
		return id;
	}

	public void setText(String text) {
		Log.d(Log.modelTag, "set text=\"" + text + "\" in " + this);

		this.text = text;
	}

	public String getText() {
		return this.text;
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
