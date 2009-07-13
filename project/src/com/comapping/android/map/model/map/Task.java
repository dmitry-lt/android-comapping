package com.comapping.android.map.model.map;

import java.io.Serializable;

public class Task implements Serializable {
	private static final long serialVersionUID = 4156893986325113199L;

	private String start;
	private String deadline;
	private String responsible;
	private String estimate;

	public Task(String start, String deadline, String responsible, String estimate) {
		setStart(start);
		setDeadline(deadline);
		setResponsible(responsible);
		setEstimate(estimate);
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
		this.deadline = deadline;
	}

	public String getResponsible() {
		return responsible;
	}

	public void setResponsible(String responsible) {
		this.responsible = (responsible != null) ? responsible : "";
	}

	
	public String getEstimate() {
		return estimate;
	}
	
	public void setEstimate(String estimate) {
		this.estimate = estimate;
	}
	
	public String toString() {
		return "[Task: deadline=" + deadline + ", responsible=\"" + "\"]";
	}
}
