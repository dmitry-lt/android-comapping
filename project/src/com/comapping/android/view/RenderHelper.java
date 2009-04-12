package com.comapping.android.view;

import android.graphics.Point;
import android.graphics.Rect;

public class RenderHelper {
	static boolean pointLiesOnRect(Point p, Rect rect) {
		return rect.left <= p.x && p.x <= rect.right && rect.top <= p.y && p.y <= rect.bottom;
	}
	
	
}
