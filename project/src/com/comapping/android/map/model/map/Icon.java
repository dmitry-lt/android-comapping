package com.comapping.android.map.model.map;

public enum Icon {
	QUESTION_MARK, EXCLAMATION_MARK, LOCK, TEA_TIME, PUZZLE, 
	MAGNIFIER, THUMBS_UP, THUMBS_DOWN, IDEA, DOLLAR, HEART,
	NEEDS_CHAT, CLOCK, REMINDER, BOMB, TEST, HOMEWORK, NEEDS_FEEDBACK;

	public static Icon parse(String s) {
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
			return null;
		}
	}
	
	public static String write(Icon s) {
		if (s == null) {
			return null;
		} else if (s.equals(QUESTION_MARK)) {
			return "question_mark";
		} else if (s.equals(EXCLAMATION_MARK)) {
			return "exclamation_mark";
		} else if (s.equals(LOCK)) {
			return "lock";
		} else if (s.equals(TEA_TIME)) {
			return "tea_time";
		} else if (s.equals(PUZZLE)) {
			return "puzzle";
		} else if (s.equals(MAGNIFIER)) {
			return "magnifier";
		} else if (s.equals(THUMBS_UP)) {
			return "thumbs_up";
		} else if (s.equals(THUMBS_DOWN)) {
			return "thumbs_down";
		} else if (s.equals(IDEA)) {
			return "idea";
		} else if (s.equals(DOLLAR)) {
			return "dollar";
		} else if (s.equals(HEART)) {
			return "heart";
		} else if (s.equals(NEEDS_CHAT)) {
			return "needs_chat";
		} else if (s.equals(CLOCK)) {
			return "clock";
		} else if (s.equals(REMINDER)) {
			return "reminder";
		} else if (s.equals(BOMB)) {
			return "bomb";
		} else if (s.equals(TEST)) {
			return "test";
		} else if (s.equals(HOMEWORK)) {
			return "homework";
		} else {
			return "needs_feedback";
		}
	}
}
