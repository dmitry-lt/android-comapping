package com.comapping.android.view;

import com.comapping.android.model.Map;
import com.comapping.android.model.Topic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

public class MapView extends View {

	private static final int xShift = 20;
	private static final int yShift = 20;
	
	private Map map;
	
	private int x;
	private int y;
	
	public MapView(Context context, Map map) {
		super(context);
		this.map = map;
	}
	
	private void drawTopic(Topic topic, Canvas c)
	{
		if (topic == null)
			return;
		Paint p = new Paint();
		p.setColor(Color.GRAY);
		c.drawLine(x, y, x + xShift, y, p);
		x += xShift;
		p.setColor(Color.BLACK);
		Rect r = new Rect();
		String text = topic.getText();
		p.getTextBounds(text, 0, text.length(), r);
		c.drawText(text, x + 10, y + r.height() / 2, p);
		int yp = y;
		for (Topic i : topic.getChildTopics())
		{
			p.setColor(Color.GRAY);
			y += yShift;
			c.drawLine(x, yp, x, y, p);
			yp = y;
			drawTopic(i, c);
		}
		x -= xShift;
	}
		
	@Override
	protected void onDraw(Canvas c)
	{
		c.drawARGB(255, 255, 255, 255);
		x = 10;
		y = 10;
		drawTopic(map.getRoot(), c);
	}
	
}
