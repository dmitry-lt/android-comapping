package com.comapping.android.view;

import java.util.ArrayList;
import java.util.HashMap;

import com.comapping.android.model.Map;
import com.comapping.android.model.Topic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

public class MapView extends View {

	public MapView(Context context, Map map) {
		super(context);
		this.map = map;
	}	
	
	private static final int xShift = 30;
	private static final int yShift = 30;
	private static final int oSize = 10;
	private static final int iSize = 5;
	
	private Map map;
	private HashMap<Integer, Boolean> open = new HashMap<Integer, Boolean>();
	
	private int x;
	private int y;
	private boolean toUpdate = true;
	private ArrayList<EventPoint> es = new ArrayList<EventPoint>();
	
	private class EventPoint
	{
		public int x, y, id;
		
		public EventPoint(int x, int y, int id)
		{
			this.x = x;
			this.y = y;
			this.id = id;
		}
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
			if (toUpdate)
				es.add(new EventPoint(x, y, id));
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

	int count = 0;
	long FPS = 0;
	long startTime = System.currentTimeMillis();
	boolean test = true;
	
	@Override
	protected void onDraw(Canvas c)
	{
		if (test)
			c.drawARGB(255, 255, 255, 255);
		else
			c.drawARGB(0, 255, 255, 255);
		x = 15;
		y = 25;
		if (toUpdate)
			es.clear();
		drawTopic(map.getRoot(), c);
		toUpdate = false;
		
		Paint p = new Paint();
		p.setColor(Color.GREEN);
		c.drawText("FPS: " + FPS, 10, 10, p);
		if (System.currentTimeMillis() - startTime > 1000) {
			FPS = (1000 * count) / (System.currentTimeMillis() - startTime);
			startTime = System.currentTimeMillis();
			count = 0;
		}
		count++;
		invalidate();
		
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (event.getAction() == MotionEvent.ACTION_DOWN)
		{
			int x = (int)event.getX();
			int y = (int)event.getY();
			for (EventPoint e : es)
				if (Math.hypot(x - e.x, y - e.y) < oSize)
				{
					open.put(e.id, !open.get(e.id));
					toUpdate = true;
				}
		}
			
		return true;
	}
	
}
