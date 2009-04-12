package com.comapping.android.model;

import java.util.Date;

public class Task {
	private String start;
	private Date deadline;
	private String responsible;

	public Task(String start, Date deadline, String responsible) {
		setStart(start);
		setDeadline(deadline);
		setResponsible(responsible);
	}

	public void setStart(String start) {
		this.start = start;
	}

	public String getStart() {
		return start;
	}

	public Date getDeadline() {
		return deadline;
	}

	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}

	public String getResponsible() {
		return responsible;
	}

	public void setResponsible(String responsible) {
		this.responsible = (responsible != null) ? responsible : "";
	}

	@Override
	public String toString() {
		return "[Task: deadline=" + deadline + ", responsible=\"" + "\"]";
	}
}
