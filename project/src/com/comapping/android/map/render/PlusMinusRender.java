package com.comapping.android.map.render;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class PlusMinusRender extends Render {
	
	private static final int RADIUS = 15;
	private static final int BORDER = 2;
	private static final int PLUS_SIZE = 7;
	private static final int PLUS_THICKNESS = 2;
	
	public boolean isPlus = true;
	private Paint p = new Paint();
	public PlusMinusRender(boolean collpased) {
		isPlus = collpased;
		p.setAntiAlias(true);
	}
	
	public void draw(int x, int y, int width, int height, Canvas c) {
		drawCircle(x + RADIUS, y + RADIUS, c);
	}
	
	private void drawCircle(int x, int y, Canvas c) {
		p.setColor(Color.GRAY);
		c.drawCircle(x, y, RADIUS, p);
		p.setColor(Color.WHITE);
		c.drawCircle(x, y, RADIUS - BORDER, p);
		p.setColor(Color.GRAY);
		c.drawRect(x - PLUS_SIZE, y - PLUS_THICKNESS, x + PLUS_SIZE, y
				+ PLUS_THICKNESS, p);

		if (isPlus) {
			c.drawRect(x - PLUS_THICKNESS, y - PLUS_SIZE, x + PLUS_THICKNESS, y
					+ PLUS_SIZE, p);
		}
	}
	
	public int getHeight() {
		return RADIUS * 2;
	}

	
	public int getWidth() {
		return RADIUS * 2;
	}
	
	public boolean onTouch(int x, int y) {
		return false;
	}

}
