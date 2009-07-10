package com.comapping.android.map.model.map;

import com.comapping.android.map.model.exceptions.EnumParsingException;

public enum Icon {
	QUESTION_MARK, EXCLAMATION_MARK, LOCK, TEA_TIME, PUZZLE, 
	MAGNIFIER, THUMBS_UP, THUMBS_DOWN, IDEA, DOLLAR, HEART,
	NEEDS_CHAT, CLOCK, REMINDER, BOMB, TEST, HOMEWORK, NEEDS_FEEDBACK;

	public static Icon parse(String s) throws EnumParsingException {
		if (s.equals("question_mark")) {
			return Icon.QUESTION_MARK;
		} else if (s.equals("exclamation_mark")) {
			return Icon.EXCLAMATION_MARK;
		} else if (s.equals("lock")) {
			return Icon.LOCK;
		} else if (s.equals("tea_time")) {
			return Icon.TEA_TIME;
		} else if (s.equals("puzzle")) {
			return Icon.PUZZLE;
		} else if (s.equals("magnifier")) {
			return Icon.MAGNIFIER;
		} else if (s.equals("thumbs_up")) {
			return Icon.THUMBS_UP;
		} else if (s.equals("thumbs_down")) {
			return Icon.THUMBS_DOWN;
		} else if (s.equals("idea")) {
			return Icon.IDEA;
		} else if (s.equals("dollar")) {
			return Icon.DOLLAR;
		} else if (s.equals("heart")) {
			return Icon.HEART;
		} else if (s.equals("needs_chat")) {
			return Icon.NEEDS_CHAT;
		} else if (s.equals("clock")) {
			return Icon.CLOCK;
		} else if (s.equals("reminder")) {
			return Icon.REMINDER;
		} else if (s.equals("bomb")) {
			return Icon.BOMB;
		} else if (s.equals("test")) {
			return Icon.TEST;
		} else if (s.equals("homework")) {
			return Icon.HOMEWORK;
		} else if (s.equals("needs_feedback")) {
			return Icon.NEEDS_FEEDBACK;
		} else {
			throw new EnumParsingException();
		}
	}
}
