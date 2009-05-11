package com.comapping.android.view.explorer;

import java.util.ArrayList;

import android.content.Context;

import com.comapping.android.model.Topic;
import com.comapping.android.view.topic.TopicRender;

public class TopicView implements Comparable<TopicView> {
	public boolean isOpen;
	public TopicRender topicRender;
	public int topicRenderX, topicRenderY;
	public ArrayList<TopicView> children;
	public int x1, y1, x2, y2;
	public TopicView parent, nextSibling = null, prevSibling = null;

	public TopicView(Topic topic, TopicView parent, Context context) {
		isOpen = true;
		topicRender = new TopicRender(topic, context);
		children = new ArrayList<TopicView>();
		for (int i = 0; i < topic.getChildrenCount(); i++) {
			children.add(new TopicView(topic.getChildByIndex(i), this, context));
		}
		this.parent = parent;
		for (int i = 0; i < topic.getChildrenCount(); i++) {
			if (i - 1 >= 0) {
				children.get(i).prevSibling = children.get(i - 1);
			}
			if (i + 1 < topic.getChildrenCount()) {
				children.get(i).nextSibling = children.get(i + 1);
			}
		}
	}

	public TopicView getNextTopic() {
		if (children.size() > 0 && isOpen) {
			return children.get(0);
		}
		TopicView topic = this;
		while (true) {
			if (topic.nextSibling != null) {
				return topic.nextSibling;
			}
			if (topic.parent != null) {
				topic = topic.parent;
			} else {
				break;
			}
		}
		return null;
	}

	private TopicView getLastTopic() {
		if (children.size() > 0 && isOpen) {
			return children.get(children.size() - 1).getLastTopic();
		}
		return this;
	}

	public TopicView getPrevTopic() {
		if (prevSibling != null) {
			return prevSibling.getLastTopic();
		}
		return parent;
	}

	public TopicView(int y) {
		topicRenderY = y;
	}

	@Override
	public int compareTo(TopicView another) {
		int y1 = this.topicRenderY + this.topicRender.getHeight();
		int y2 = another.topicRenderY;
		if (y1 < y2) {
			return -1;
		} else if (y1 == y2) {
			return 0;
		} else {
			return 1;
		}
	}
}