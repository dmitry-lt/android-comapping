package com.comapping.android.map.model.map;

public enum Star {
	BLUE, PURPLE, BLACK, RED, ORANGE, YELLOW, GREEN;

	public static Star parse(String s) {
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
			return null;
		}
	}
	
	public static String write(Star a) {
		if (a == null) {
			return null;
		} else if (a.equals(BLUE)) {
			return "blue";
		} else if (a.equals(PURPLE)) {
			return "purple";
		} else if (a.equals(BLACK)) {
			return "black";
		} else if (a.equals(RED)) {
			return "red";
		} else if (a.equals(ORANGE)) {
			return "orange";
		} else if (a.equals(YELLOW)) {
			return "yellow";
		} else {
			return "green";
		}
	}	
}
