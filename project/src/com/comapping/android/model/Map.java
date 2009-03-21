package com.comapping.android.model;

import com.comapping.android.Log;

/**
 * @author Passichenko Victor and Yuri Zemlyanskiy
 * 
 */

public class Map {
	private final int id;
	private String name;
	private User owner;
	private Topic root;

	public Map(int id) {
		this.id = id;

		Log.d(Log.modelTag, "create " + this);
	}
	
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		Log.d(Log.modelTag, "set name=\"" + name + "\" in " + this);

		this.name = name;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;

		Log.d(Log.modelTag, "set owner=" + owner + " in " + this);
	}

	public Topic getRoot() {
		return root;
	}
	
	public void setRoot(Topic root) {
		Log.d(Log.modelTag, "set root=" + root + " in " + this);
		
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

	@Override
	public String toString() {
		return "[Map: id=" + this.getId() + ", name=\"" + this.getName()
				+ "\"]";
	}

	public static class User {
		private final int id;
		private String name;
		private String email;

		public User(int id, String name, String email) {
			this.id = id;
			this.name = name;
			this.email = email;
		}

		public int getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		@Override
		public String toString() {
			return "[User: id=" + id + ", name=\"" + name + "\", " + "email=\""
					+ email + "\"]";
		}
	}
}
