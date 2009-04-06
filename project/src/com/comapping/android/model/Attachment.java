package com.comapping.android.model;

import java.util.Date;

public class Attachment {
	private Date date;
	private String filename;
	private String key;
	private int size;
	
	public Attachment(Date date, String filename, String key, int size) {
		this.date = date;
		this.filename = filename;
		this.key = key;
		this.size = size;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}		
	
	@Override
	public String toString() {
		return "[Attachment: date=" + date + " filename=\"" + "\" key=\"" + key + "\" size=" + size + "]";
	}
}
