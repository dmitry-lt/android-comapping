package com.comapping.android.model;

import java.util.Date;

public class Task {
	private String deadline;
	private String responsible;

	public Task(String deadline, String responsible) {
		this.deadline = deadline;
		this.responsible = responsible;
	}

	public String getDeadline() {
		return deadline;
	}

	public void setDeadline(String deadline) {
		this.deadline = deadline;
	}

	public String getResponsible() {
		return responsible;
	}

	public void setResponsible(String responsible) {
		this.responsible = responsible;
	}

	@Override
	public String toString() {
		return "[Task: deadline=" + deadline + ", responsible=\"" + "\"]";
	}
}
