package com.comapping.android.model;

public class Task {
	private String deadline;
	private String responsible;

	public Task(String deadline, String responsible) {
		setDeadline(deadline);
		setResponsible(responsible);
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
