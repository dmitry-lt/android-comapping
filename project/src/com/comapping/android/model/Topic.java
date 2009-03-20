package com.comapping.android.model;

import java.util.ArrayList;
import java.util.Iterator;

import com.comapping.android.Log;

/**
 * @author Passichenko Victor and Yuri Zemlyanskiy
 *
 */
public class Topic implements Iterable<Topic> {
	private String text;
	private ArrayList<Topic> children;

	public Topic() {
		this.children = new ArrayList<Topic>();
		this.text = "";
	}

	public Topic(String text) {
		this.children = new ArrayList<Topic>();
		this.text = text;

		Log.d("Map Model" , "Created " + this);
	}

	public void setText(String text) {
		this.text = text;

		Log.i("Map Model", "Set text " + this);
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
		int i = 0;
		for (Topic child : children)
			result[i++] = child;
		return result;
	}

	public void addChild(Topic child) {
		children.add(child);

		Log.i("Map Model", "Add " + child + " in " + this);
	}

	public void removeChildByIndex(int index) throws IndexOutOfBoundsException {
		Log.i("Map Model", "Remove " + getChildByIndex(index));

		children.remove(index);
	}

	public String toString() {
		return "[Topic : " + this.getText() + "]";
	}

	@Override
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
		return index <= topic.getChildrenCount();
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
