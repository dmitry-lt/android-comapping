package com.comapping.android.model.map;

import java.io.Serializable;

public class User implements Serializable {
	private static final long serialVersionUID = -8881208195583855478L;

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
		return "[User: id=" + id + ", name=\"" + name + "\", " + "email=\"" + email + "\"]";
	}
}