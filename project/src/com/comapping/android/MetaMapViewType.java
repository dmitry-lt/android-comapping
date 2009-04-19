package com.comapping.android;

public enum MetaMapViewType {
	SDCARD_VIEW, INTERNET_VIEW;

	final static private String SDCARD_VIEW_STRING = "sdcard";
	final static private String INTERNET_VIEW_STRING = "internet";

	public static MetaMapViewType getViewTypeFromString(String viewType) {
		if (viewType.equals(SDCARD_VIEW_STRING)) {
			return SDCARD_VIEW;
		} else {
			if (viewType.equals(INTERNET_VIEW_STRING)) {
				return INTERNET_VIEW;
			}
		}

		// INTERNET_VIEW for default
		return INTERNET_VIEW;
	}

	public String toString() {
		switch (this) {
		case SDCARD_VIEW:
			return SDCARD_VIEW_STRING;
		case INTERNET_VIEW:
			return INTERNET_VIEW_STRING;
		}

		// unreachable code
		return "";
	}
}