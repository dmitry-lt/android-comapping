package com.comapping.android;

public enum ViewType {
	EXPLORER_VIEW, COMAPPING_VIEW;

	final static private String EXPLORER_VIEW_STRING = "Explorer view";
	final static private String COMAPPING_VIEW_STRING = "Comapping view";

	public static ViewType getViewTypeFromString(String viewType) {
		if (viewType.equals(EXPLORER_VIEW_STRING)) {
			return EXPLORER_VIEW;
		} else {
			if (viewType.equals(COMAPPING_VIEW_STRING)) {
				return COMAPPING_VIEW;
			}
		}

		// EXPLORER_VIEW for default
		return EXPLORER_VIEW;
	}

	public String toString() {
		switch (this) {
		case EXPLORER_VIEW:
			return EXPLORER_VIEW_STRING;
		case COMAPPING_VIEW:
			return COMAPPING_VIEW_STRING;
		}

		// unreachable code
		return "";
	}
}
