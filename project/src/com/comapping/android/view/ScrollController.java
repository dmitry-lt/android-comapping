package com.comapping.android.view;

public abstract class ScrollController {
	public abstract void smoothScroll(int destX, int destY);
	public abstract void intermediateScroll(int destX, int destY);
}
