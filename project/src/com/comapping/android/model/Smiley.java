package com.comapping.android.model;

public enum Smiley {
	HAPPY, NEUTRAL, SAD;

	public static Smiley parse(String s) throws EnumParsingException {
		if (s.equals("happy")) {
			return Smiley.HAPPY;
		} else if (s.equals("neutral")) {
			return Smiley.NEUTRAL;
		} else if (s.equals("sad")) {
			return Smiley.SAD;
		} else {
			throw new EnumParsingException();
		}

	}
}
