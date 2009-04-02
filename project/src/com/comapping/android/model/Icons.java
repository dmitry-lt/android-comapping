package com.comapping.android.model;

import java.util.ArrayList;
import java.util.List;

public class Icons {
	private int mask;

	public void addIcon(Icon icon) {
		mask |= 1 << icon.ordinal();
	}

	public void removeIcon(Icon icon) {
		if (this.hasIcon(icon)) {
			mask ^= 1 << icon.ordinal();
		}
	}

	public boolean hasIcon(Icon icon) {
		return (mask & (1 << icon.ordinal())) != 0;
	}

	public int getCount() {
		return Integer.bitCount(mask);
	}

	public List<Icon> getIcons() {
		ArrayList<Icon> result = new ArrayList<Icon>();

		for (Icon icon : Icon.values()) {
			if (this.hasIcon(icon)) {
				result.add(icon);
			}
		}

		return result;
	}
}
