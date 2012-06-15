package com.lanit_tercom.comapping.android.map;

public abstract class ScrollController {
	/**
	 * Start smooth scrolling by providing the distance to travel.
	 *
	 * @param dx Horizontal distance to travel. Positive numbers will scroll the content to the left.
	 * @param dy Vertical distance to travel. Positive numbers will scroll the content up.
	 */
	public abstract void smoothScroll(int dx, int dy);

	/**
	 * Intermediate scroll by the distance to travel.
	 *
	 * @param dx Horizontal distance to travel. Positive numbers will scroll the content to the left.
	 * @param dy Vertical distance to travel. Positive numbers will scroll the content up.
	 */
	public abstract void intermediateScroll(int dx, int dy);
}
