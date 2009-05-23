package com.comapping.android.model.map;

import com.comapping.android.model.exceptions.EnumParsingException;

public enum Flag {
	GO, FOR_DISCUSSION, POSSIBILITY, RISK, PROGRESS, CAREFULL, CAUTION;

	public static Flag parse(String s) throws EnumParsingException {
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
			throw new EnumParsingException();
		}

	}
}
