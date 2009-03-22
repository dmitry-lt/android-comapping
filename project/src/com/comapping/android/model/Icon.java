package com.comapping.android.model;

public enum Icon {
	QUESTION, LOOK_HERE, CRITICAL, GOOD_YES, BAD_NO, CHECK, MONEY, HEART, TIME;

	public static Icon parse(String s) throws EnumParsingException {
		if (s.equals("question_mark")) {
			return Icon.QUESTION;
		} else if (s.equals("exclamation_mark")) {
			return Icon.LOOK_HERE;
		} else if (s.equals("bomb")) {
			return Icon.CRITICAL;
		} else if (s.equals("thumbs_up")) {
			return Icon.GOOD_YES;
		} else if (s.equals("thumbs_down")) {
			return Icon.BAD_NO;
		} else if (s.equals("magnifier")) {
			return Icon.CHECK;
		} else if (s.equals("dollar")) {
			return Icon.MONEY;
		} else if (s.equals("heart")) {
			return Icon.HEART;
		} else if (s.equals("clock")) {
			return Icon.TIME;
		} else {
			throw new EnumParsingException();
		}
	}
}
