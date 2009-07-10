package com.comapping.android.map.model.map;

import com.comapping.android.map.model.exceptions.EnumParsingException;

public enum Arrow {
	BLUE, PURPLE, BLACK, RED, ORANGE, YELLOW, GREEN;

	public static Arrow parse(String s) throws EnumParsingException {
		if (s.equals("blue")) {
			return Arrow.BLUE;
		} else if (s.equals("purple")) {
			return Arrow.PURPLE;
		} else if (s.equals("black")) {
			return Arrow.BLACK;
		} else if (s.equals("red")) {
			return Arrow.RED;
		} else if (s.equals("orange")) {
			return Arrow.ORANGE;
		} else if (s.equals("yellow")) {
			return Arrow.YELLOW;
		} else if (s.equals("green")) {
			return Arrow.GREEN;
		} else {
			throw new EnumParsingException();
		}

	}
}
