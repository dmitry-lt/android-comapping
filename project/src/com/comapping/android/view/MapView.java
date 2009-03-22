package com.comapping.android.view;

import java.util.HashMap;

import com.comapping.android.model.Map;
import com.comapping.android.model.Topic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

public class MapView extends View {

	private static final int xShift = 30;
	private static final int yShift = 30;
	private static final int oSize = 10;
	private static final int iSize = 5;
	
	private Map map;
	private HashMap<Integer, Boolean> open = new HashMap<Integer, Boolean>();
	
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
		int id = topic.getId();		
		if (topic.getChildrenCount() > 0)
		{
			c.drawCircle(x, y, oSize, p);
			if (open.get(id) == null)
				open.put(id, true);
		}
		x += xShift;
		p.setColor(Color.BLACK);
		Rect r = new Rect();
		String text = topic.getText();
		p.getTextBounds(text, 0, text.length(), r);
		c.drawText(text, x + oSize + 5, y + r.height() / 2, p);
		int ty = y;
		if (topic.getChildrenCount() > 0 && open.get(id))
		{
			int yp = y;
			boolean flag = false;
			for (Topic i : topic.getChildTopics())
			{
				p.setColor(Color.GRAY);
				y += yShift;
				if (flag)
					yp += oSize;
				c.drawLine(x, yp, x, y, p);
				yp = y;
				drawTopic(i, c);
				flag = !(i == null || i.getChildTopics().length == 0);
			}
		}
		x -= xShift;
		if (topic.getChildrenCount() > 0)
		{
			p.setColor(Color.BLACK);
			c.drawLine(x - iSize, ty, x + iSize, ty, p);
			if (!open.get(id))
				c.drawLine(x, ty - iSize, x, ty + iSize, p);
		}
	}
		
	@Override
	protected void onDraw(Canvas c)
	{
		c.drawARGB(255, 255, 255, 255);
		x = 15;
		y = 15;
		drawTopic(map.getRoot(), c);
	}
	
}
