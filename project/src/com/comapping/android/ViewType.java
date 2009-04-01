package com.comapping.android;

public enum ViewType {
	EXPLORER_VIEW, TREE_VIEW;

	final static private String EXPLORER_VIEW_STRING = "EXPLORER_VIEW";
	final static private String TREE_VIEW_STRING = "TREE_VIEW";

	public static ViewType getViewTypeFromString(String viewType) {
		if (viewType.equals(EXPLORER_VIEW_STRING)) {
			return EXPLORER_VIEW;
		} else {
			if (viewType.equals(TREE_VIEW_STRING)) {
				return TREE_VIEW;
			}
		}

		// EXPLORER_VIEW for default
		return EXPLORER_VIEW;
	}

	public String toString() {
		switch (this) {
		case EXPLORER_VIEW:
			return EXPLORER_VIEW_STRING;
		case TREE_VIEW:
			return TREE_VIEW_STRING;
		}

		// unreachable code
		return "";
	}
}
