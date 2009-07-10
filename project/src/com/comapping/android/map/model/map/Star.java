package com.comapping.android.map.model.map;

import com.comapping.android.map.model.exceptions.EnumParsingException;

public enum Star {
	BLUE, PURPLE, BLACK, RED, ORANGE, YELLOW, GREEN;

	public static Star parse(String s) throws EnumParsingException {
		if (s.equals("blue")) {
			return Star.BLUE;
		} else if (s.equals("purple")) {
			return Star.PURPLE;
		} else if (s.equals("black")) {
			return Star.BLACK;
		} else if (s.equals("red")) {
			return Star.RED;
		} else if (s.equals("orange")) {
			return Star.ORANGE;
		} else if (s.equals("yellow")) {
			return Star.YELLOW;
		} else if (s.equals("green")) {
			return Star.GREEN;
		} else {
			throw new EnumParsingException();
		}

	}
}
