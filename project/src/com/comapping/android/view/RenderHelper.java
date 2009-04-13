package com.comapping.android.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class RenderHelper {
	static boolean pointLiesOnRect(Point p, Rect rect) {
		return rect.left <= p.x && p.x <= rect.right && rect.top <= p.y && p.y <= rect.bottom;
	}

	static Bitmap getBitmap(Drawable image, int size) {
		Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		image.setBounds(0, 0, size, size);
		image.draw(canvas);
		return bitmap;
	}
}
