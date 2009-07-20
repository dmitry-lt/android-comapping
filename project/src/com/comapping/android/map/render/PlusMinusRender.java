package com.comapping.android.map.render;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class PlusMinusRender  extends Render {
	
	private static final int OUTER_SIZE = 15;
	private static final int CIRCLE_WIDTH = 2;
	private static final int PLUS_LENGTH = 7;
	private static final int PLUS_WIDTH = 2;
	
	private static final int WIDTH = OUTER_SIZE*2;
	private static final int HEIGHT = OUTER_SIZE*2;
	
	public boolean isPlus = true;
	Paint p = new Paint();
	public PlusMinusRender(boolean plus)
	{
		isPlus = plus;
		p.setAntiAlias(true);
	}
	
	
	public void draw(int x, int y, int width, int height, Canvas c) {
		drawCircle(x + OUTER_SIZE, y + OUTER_SIZE, c);
	}
	
	private void drawCircle(int x, int y, Canvas c) {
		p.setColor(Color.GRAY);
		c.drawCircle(x, y, OUTER_SIZE, p);
		p.setColor(Color.WHITE);
		c.drawCircle(x, y, OUTER_SIZE - CIRCLE_WIDTH, p);
		p.setColor(Color.GRAY);
		c.drawRect(x - PLUS_LENGTH, y - PLUS_WIDTH, x + PLUS_LENGTH, y
				+ PLUS_WIDTH, p);

		if (isPlus)
			c.drawRect(x - PLUS_WIDTH, y - PLUS_LENGTH, x + PLUS_WIDTH, y
					+ PLUS_LENGTH, p);
	}

	
	public int getHeight() {
		return HEIGHT;
	}

	
	public int getWidth() {
		return WIDTH;
	}

	
	public boolean onTouch(int x, int y) {
		return false;
	}

}
