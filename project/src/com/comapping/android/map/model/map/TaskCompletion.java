package com.comapping.android.map.model.map;

import com.comapping.android.map.model.exceptions.EnumParsingException;

public enum TaskCompletion {
	TO_DO, TWENTY_FIVE, FIFTY, SEVENTY_FIVE, COMPLETE, CANCELLED;

	public static TaskCompletion parse(String s) throws EnumParsingException {
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
			throw new EnumParsingException();
		}
	}
}
