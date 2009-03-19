package com.comapping.android.model;

public class Map extends Topic {

	private int maxDeepRecursive(Topic currentTopic) {
		if (currentTopic == null) {
			return 0;
		}
		int result = 0;
		for (Topic child : currentTopic.getChildTopics())
			result = Math.max(result, maxDeepRecursive(child));
		return result + 1;
	}

	public Map(String text) {
		super(text);
	}

	public int maxDeep() {
		return maxDeepRecursive((Topic) this);
	}

	public String toString() {
		return "[Map : " + this.getText() + "]";
	}
}
