package com.comapping.android.map.model.map;

public enum TaskCompletion {
	TO_DO, TWENTY_FIVE, FIFTY, SEVENTY_FIVE, COMPLETE, CANCELLED;

	public static TaskCompletion parse(String s) {
		if (s.equals("0")) {
			return TaskCompletion.TO_DO;
		} else if (s.equals("25")) {
			return TaskCompletion.TWENTY_FIVE;
		} else if (s.equals("50")) {
			return TaskCompletion.FIFTY;
		} else if (s.equals("75")) {
			return TaskCompletion.SEVENTY_FIVE;
		} else if (s.equals("100")) {
			return TaskCompletion.COMPLETE;
		} else if (s.equals("-1")) {
			return TaskCompletion.CANCELLED;
		} else {
			return null;
		}
	}
	
	public static String write(TaskCompletion s) {
		if (s == null) {
			return null;
		} else if (s.equals(TO_DO)) {
			return "0";
		} else if (s.equals(TWENTY_FIVE)) {
			return "25";
		} else if (s.equals(FIFTY)) {
			return "50";
		} else if (s.equals(SEVENTY_FIVE)) {
			return "75";
		} else if (s.equals(COMPLETE)) {
			return "100";
		} else {
			return "-1";
		}
	}	
}
