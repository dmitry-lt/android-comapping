package com.comapping.android.map.model.map;

public enum Arrow {
	BLUE, PURPLE, BLACK, RED, ORANGE, YELLOW, GREEN;

	public static Arrow parse(String s) {
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
			return null;
		}
	}
	
	public static String write(Arrow a) {
		if (a == null) {
			return null;
		}
		if (a.equals(BLUE)) {
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
