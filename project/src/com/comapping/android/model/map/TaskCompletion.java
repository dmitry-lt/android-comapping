package com.comapping.android.model.map;

import com.comapping.android.model.exceptions.EnumParsingException;

public enum TaskCompletion {
	TO_DO, TWENTY_FIVE, FIFTY, SEVENTY_FIVE, COMPLETE;

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
		} else {
			throw new EnumParsingException();
		}
	}
}
