package com.lanit_tercom.comapping.android.map.render;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class PlusMinusRender extends Render {
	
	private static final int RADIUS = 15;
	private static final int BORDER = 2;
	private static final int PLUS_SIZE = 7;
	private static final int PLUS_THICKNESS = 2;
	
	public boolean collapsed = true;
	private Paint p = new Paint();
	
	public PlusMinusRender(boolean collpased) {
		this.collapsed = collpased;
		p.setAntiAlias(true);
	}
	
	@Override
	public void draw(int x, int y, int width, int height, Canvas c) {
		p.setColor(Color.GRAY);
		c.drawCircle(x, y, RADIUS, p);
		p.setColor(Color.WHITE);
		c.drawCircle(x, y, RADIUS - BORDER, p);
		p.setColor(Color.GRAY);
		c.drawRect(x - PLUS_SIZE, y - PLUS_THICKNESS, x + PLUS_SIZE, y
				+ PLUS_THICKNESS, p);

		if (collapsed) {
			c.drawRect(x - PLUS_THICKNESS, y - PLUS_SIZE, x + PLUS_THICKNESS, y
					+ PLUS_SIZE, p);
		}
	}
	
	@Override
	public int getHeight() {
		return RADIUS * 2;
	}

	@Override
	public int getWidth() {
		return RADIUS * 2;
	}
	
	@Override
	public boolean onTouch(int x, int y) {
		return false;
	}

}
