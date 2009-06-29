package com.comapping.android.model.map;

import java.io.Serializable;

import com.comapping.android.Log;

/**
 * @author Passichenko Victor and Yuri Zemlyanskiy
 * 
 */

public class Map implements Serializable {
	private static final long serialVersionUID = 1735863291545926897L;

	private final int id;
	private String name;
	private User owner;
	private Topic root;

	public Map(int id) {
		this.id = id;

		Log.d(Log.MODEL_TAG, "create " + this);
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		Log.d(Log.MODEL_TAG, "set name=\"" + name + "\" in " + this);

		this.name = name;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;

		Log.d(Log.MODEL_TAG, "set owner=" + owner + " in " + this);
	}

	public Topic getRoot() {
		return root;
	}

	public void setRoot(Topic root) {
		Log.d(Log.MODEL_TAG, "set root=" + root + " in " + this);

		this.root = root;
	}

	public int maxDeep() {
		return maxDeepRecursive(root);
	}

	private int maxDeepRecursive(Topic currentTopic) {
		if (currentTopic == null) {
			return 0;
		}
		int result = 0;
		for (Topic child : currentTopic.getChildTopics())
			result = Math.max(result, maxDeepRecursive(child));
		return result + 1;
	}

	
	public String toString() {
		return "[Map: id=" + this.getId() + ", name=\"" + this.getName() + "\"]";
	}
}
