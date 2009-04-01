package com.comapping.android.view;

import java.util.ArrayList;
import java.util.HashMap;

import com.comapping.android.model.Map;
import com.comapping.android.model.Topic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class ExplorerRender extends Render {

	private class touchPoint
	{
		public int x, y, id;
		
		public touchPoint(int x, int y, int id)
		{
			this.x = x;
			this.y = y;
			this.id = id;
		}
	}
	
	private static final int xShift = 30;
	private static final int yShift = 15;
	private static final int outerSize = 10;
	private static final int innerSize = 5;
	private static final int borderSize = 4;
	private static final int blockShift = 5;
	
	private Map map;
	private HashMap<Integer, Boolean> open = new HashMap<Integer, Boolean>();
	private HashMap<Integer, TextRender> renders = new HashMap<Integer, TextRender>();
	private HashMap<Integer, IconRender> renders1 = new HashMap<Integer, IconRender>();

	private boolean toUpdate = true;
	private ArrayList<touchPoint> points = new ArrayList<touchPoint>();
	private int xPlus, yPlus;
	private int height, width;
	
	public ExplorerRender (Context context, Map map)
	{
		this.map = map;		
	}
	
	private int[] drawTopicContext(Topic topic, int x, int y, Canvas c)
	{
		int[] ret = new int[2];
		ret[0] = x;
		
		Paint p = new Paint();
		
		x += outerSize;
		
		//calculate sizes
		if (renders.get(topic.getId()) == null)
			renders.put(topic.getId(), new TextRender(topic.getFormattedText()));
		if (renders1.get(topic.getId()) == null)
			renders1.put(topic.getId(), new IconRender(topic));
		TextRender render = renders.get(topic.getId());
		IconRender render1 = renders1.get(topic.getId());
		int xSize = render.getWidth(), ySize = render.getHeight();
				
		int ty = Math.max(outerSize / 2, ySize / 2 + borderSize);
		ty = Math.max(ty, render1.getHeight() / 2);
		ret[1] = ty * 2;
		y += ty;
		
		//draw circle and line
		p.setColor(Color.GRAY);
		c.drawLine(x, y, x + xShift, y, p);
		if (topic.getChildrenCount() > 0)
		{
			if (open.get(topic.getId()) == null)
				open.put(topic.getId(), true);
			c.drawCircle(x, y, outerSize, p);
			if (toUpdate)
				points.add(new touchPoint(x - xPlus, y - yPlus, topic.getId()));
			p.setColor(Color.BLACK);
			c.drawLine(x - innerSize, y, x + innerSize, y, p);
			if (!open.get(topic.getId()))
				c.drawLine(x, y - innerSize, x, y + innerSize, p);
		}
		x += xShift;
		
		//draw icons
		x += blockShift;
		render1.draw(x, y - render1.getHeight() / 2, 0, 0, c);
		x += render1.getWidth();

		//draw text		
		x += blockShift;
		p.setColor(topic.getBgColor());
		p.setAlpha(255);
		c.drawRect(x, y - ySize / 2 - borderSize, x + xSize + borderSize * 2, y + ySize / 2 + borderSize, p);
		
		x += borderSize;
		
		render.draw(x, y - ySize / 2, 0, 0, c);
		
		x += render.getWidth() + borderSize;
		ret[0] = x - ret[0];
		
		return ret;
	}
	
	private int[] drawTopic(Topic topic, int x, int y, Canvas c)
	{
		if (topic == null)
		{
			return new int[2];
		}
		
		int[] temp = drawTopicContext(topic, x, y, c);		
		int[] ret = new int[3];
		ret[0] = temp[0];
		ret[1] = y;
		ret[2] = temp[1] / 2;
		x += xShift;
		y += temp[1];
		
		if (topic.getChildrenCount() > 0 && open.get(topic.getId()))
		{
			int flag = 0;
			int py = y - ret[2];
			Paint p = new Paint();
			p.setColor(Color.GRAY);
			for (int i = 0; i < topic.getChildrenCount(); i++)
			{
				y += yShift;
				temp = drawTopic(topic.getChildByIndex(i), x, y, c);

				if (flag == 1)
					py += outerSize;
				int ny = y + temp[2];
				if (topic.getChildByIndex(i).getChildrenCount() > 0)
					ny -= outerSize;
				c.drawLine(x + outerSize, py, x + outerSize, ny, p);
				py = y + temp[2]; 
				if (topic.getChildByIndex(i).getChildrenCount() > 0)
					flag = 1;
				else
					flag = 0;
				
				ret[0] = Math.max(ret[0], temp[0] + xShift);
				y += temp[1];
			}
		}
		
		ret[1] = y - ret[1];
		
		return ret;
	}
	
	@Override
	public void draw(int x, int y, int width, int height, Canvas c) {
		x = -x;
		y = -y;
		xPlus = x;
		yPlus = y;
		if (toUpdate)
			points.clear();
		int[] temp = drawTopic(map.getRoot(), x, y, c);
		this.width = temp[0];
		this.height = temp[1];
		toUpdate = false;
		
		/*Paint p = new Paint();
		p.setColor(Color.BLACK);
		c.drawLine(x + temp[0], y, x + temp[0], y + temp[1], p);
		c.drawLine(x, y + temp[1], x + temp[0], y + temp[1], p);*/
		
		/*p.setColor(Color.RED);
		for (touchPoint point : points)
		{
			c.drawCircle(point.x + xPlus, point.y + yPlus, 2, p);
		}*/
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public void onTouch(int x, int y) {
		for (touchPoint point : points)
		{
			if (Math.hypot(point.x + xPlus - x, point.y + yPlus - y) <= outerSize)
			{
				open.put(point.id, !open.get(point.id));
				toUpdate = true;
				break;
			}
		}
	}

}
