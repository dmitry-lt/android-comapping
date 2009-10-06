package com.comapping.android.map.model.map;

public enum Flag {
	GO, FOR_DISCUSSION, POSSIBILITY, RISK, PROGRESS, CAREFULL, CAUTION;

	public static Flag parse(String s) {
		if (s.equals("go")) {
			return Flag.GO;
		} else if (s.equals("for_discussion")) {
			return Flag.FOR_DISCUSSION;
		} else if (s.equals("possibility")) {
			return Flag.POSSIBILITY;
		} else if (s.equals("risk")) {
			return Flag.RISK;
		} else if (s.equals("progress")) {
			return Flag.PROGRESS;
		} else if (s.equals("careful")) {
			return Flag.CAREFULL;
		} else if (s.equals("caution")) {
			return Flag.CAUTION;
		} else {
			return null;
		}
	}
	
	public static String write(Flag f) {
		if (f == null) {
			return null;
		} else if (f.equals(GO)) {
			return "go";
		} else if (f.equals(FOR_DISCUSSION)) {
			return "for_discussion";
		} else if (f.equals(POSSIBILITY)) {
			return "possibility";
		} else if (f.equals(RISK)) {
			return "risk";
		} else if (f.equals(PROGRESS)) {
			return "progress";
		} else if (f.equals(CAREFULL)) {
			return "careful";
		} else {
			return "caution";
		}
	}
}
