package com.comapping.android.model;

public class Task {
	private String start;
	private String deadline;
	private String responsible;

	public Task(String start, String deadline, String responsible) {
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

	public String getDeadline() {
		return deadline;
	}

	public void setDeadline(String deadline) {
		this.deadline = (deadline != null) ? deadline : "";
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
