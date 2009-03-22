package com.comapping.android.model;

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
		return (mask & (1 << icon.ordinal())) == 0;
	}

	public int getCount() {
		return Integer.bitCount(mask);
	}
}
