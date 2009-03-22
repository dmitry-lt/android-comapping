package com.comapping.android.model;

public enum Flag {
	GO, FOR_DISCUSSION, POSSIBILITY;
	
	public static Flag parse(String s) throws EnumParsingException {
		if (s.equals("go")) {
			return Flag.GO;
		} else if (s.equals("for_discussion")) {
			return Flag.FOR_DISCUSSION;
		} else if (s.equals("possibility")) {
			return Flag.POSSIBILITY;
		} else {
			throw new EnumParsingException();
		}

	}
}
