package com.comapping.android.model.map;

import com.comapping.android.model.exceptions.EnumParsingException;

public enum Smiley {
	HAPPY, NEUTRAL, SAD, FURIOUS;

	public static Smiley parse(String s) throws EnumParsingException {
		if (s.equals("happy")) {
			return Smiley.HAPPY;
		} else if (s.equals("neutral")) {
			return Smiley.NEUTRAL;
		} else if (s.equals("sad")) {
			return Smiley.SAD;
		} else if (s.equals("furious")) {
			return Smiley.FURIOUS;
		} else {
			throw new EnumParsingException();
		}

	}
}
