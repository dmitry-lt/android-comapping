package com.lanit_tercom.comapping.android.map.model.map;

public enum Smiley {
	HAPPY, NEUTRAL, SAD, FURIOUS;

	public static Smiley parse(String s) {
		if (s.equals("happy")) {
			return Smiley.HAPPY;
		} else if (s.equals("neutral")) {
			return Smiley.NEUTRAL;
		} else if (s.equals("sad")) {
			return Smiley.SAD;
		} else if (s.equals("furious")) {
			return Smiley.FURIOUS;
		} else {
			return null;
		}
	}
	
	public static String write(Smiley s) {
		if (s == null) {
			return null;
		} else if (s.equals(HAPPY)) {
			return "happy";
		} else if (s.equals(NEUTRAL)) {
			return "neutral";
		} else if (s.equals(SAD)) {
			return "sad";
		} else {
			return "furious";
		}
	}	
}
