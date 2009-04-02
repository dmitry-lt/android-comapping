package com.comapping.android.model;

public enum Icon {
	QUESTION_MARK, EXCLAMATION_MARK, BOMB, THUMBS_UP, THUMBS_DOWN, MAGNIFIER, DOLLAR, HEART, CLOCK;

	public static Icon parse(String s) throws EnumParsingException {
		if (s.equals("question_mark")) {
			return Icon.QUESTION_MARK;
		} else if (s.equals("exclamation_mark")) {
			return Icon.EXCLAMATION_MARK;
		} else if (s.equals("bomb")) {
			return Icon.BOMB;
		} else if (s.equals("thumbs_up")) {
			return Icon.THUMBS_UP;
		} else if (s.equals("thumbs_down")) {
			return Icon.THUMBS_DOWN;
		} else if (s.equals("magnifier")) {
			return Icon.MAGNIFIER;
		} else if (s.equals("dollar")) {
			return Icon.DOLLAR;
		} else if (s.equals("heart")) {
			return Icon.HEART;
		} else if (s.equals("clock")) {
			return Icon.CLOCK;
		} else {
			throw new EnumParsingException();
		}
	}
}
