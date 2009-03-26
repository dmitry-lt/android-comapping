package com.comapping.android.view;

import java.util.ArrayList;
import java.util.HashMap;

import com.comapping.android.controller.MainController;
import com.comapping.android.controller.R;
import com.comapping.android.model.FormattedText;
import com.comapping.android.model.Map;
import com.comapping.android.model.Smiley;
import com.comapping.android.model.TaskCompletion;
import com.comapping.android.model.TextBlock;
import com.comapping.android.model.Topic;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

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
	
	private static final int iconSize = 26;
	private static final int xShift = 30;
	private static final int yShift = 15;
	private static final int outerSize = 10;
	private static final int innerSize = 5;
	private static final int borderSize = 3;
	private static final int blockShift = 5;
	private static final int textShift = 3;
	
	private Map map;
	private HashMap<Integer, Boolean> open = new HashMap();
	private Bitmap[] priorityIcon = new Bitmap[10];
	private Bitmap happyIcon;
	private Bitmap neutralIcon;
	private Bitmap sadIcon;
	private Bitmap toDoIcon;
	private boolean toUpdate = true;
	private ArrayList<touchPoint> points = new ArrayList();
	
	public ExplorerRender (Context context, Map map)
	{
		this.map = map;
		Resources r = MainController.getInstance().getResources();
		priorityIcon[1] = getBitmap(r.getDrawable(R.drawable.p1));
		priorityIcon[2] = getBitmap(r.getDrawable(R.drawable.p2));
		priorityIcon[3] = getBitmap(r.getDrawable(R.drawable.p3));
		priorityIcon[4] = getBitmap(r.getDrawable(R.drawable.p4));
/*		priorityIcon[5] = getBitmap(r.getDrawable(R.drawable.p5));
		priorityIcon[6] = getBitmap(r.getDrawable(R.drawable.p6));
		priorityIcon[7] = getBitmap(r.getDrawable(R.drawable.p7));
		priorityIcon[8] = getBitmap(r.getDrawable(R.drawable.p8));
		priorityIcon[9] = getBitmap(r.getDrawable(R.drawable.p9));*/
		happyIcon = getBitmap(r.getDrawable(R.drawable.happy));
		neutralIcon = getBitmap(r.getDrawable(R.drawable.happy));
		sadIcon = getBitmap(r.getDrawable(R.drawable.happy));
		toDoIcon = getBitmap(r.getDrawable(R.drawable.to_do));
	}

	private Bitmap getBitmap(Drawable image)
	{
		Bitmap bitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        image.setBounds(0, 0, iconSize, iconSize);
        image.draw(canvas);        
        return bitmap;
	}
	
	private int[] drawTopicContext(Topic topic, int x, int y, Canvas c)
	{
		int[] ret = new int[2];
		ret[0] = x;
		
		FormattedText text = topic.getFormattedText();
		Paint p = new Paint();
		Rect r = new Rect();
		
		x += outerSize;
		
		//calculate sizes
		int xSize = -textShift, ySize = 0;
		
		for (TextBlock cur : text.getTextBlocks())
		{
			p.setTextSize(cur.getFormat().getFontSize());
			p.getTextBounds(cur.getText(), 0, cur.getText().length(), r);
			xSize += r.width() + textShift;
			p.getTextBounds("1", 0, 1, r);
			ySize = Math.max(ySize, r.height());
		}
		
		int ty = Math.max(iconSize / 2, ySize / 2 + borderSize);
		ty = Math.max(ty, iconSize / 2);
		ret[1] = ty * 2;
		y += ty;
		
		p.setColor(Color.GRAY);
		c.drawLine(x, y, x + xShift, y, p);
		if (topic.getChildrenCount() > 0)
		{
			if (open.get(topic.getId()) == null)
				open.put(topic.getId(), true);
			c.drawCircle(x, y, outerSize, p);
			if (toUpdate)
				points.add(new touchPoint(x, y, topic.getId()));
			p.setColor(Color.BLACK);
			c.drawLine(x - innerSize, y, x + innerSize, y, p);
			if (!open.get(topic.getId()))
				c.drawLine(x, y - innerSize, x, y + innerSize, p);
		}
		x += xShift;
		
		//draw icons
		if (topic.getPriority() != 0)
		{
			x += blockShift;
			c.drawBitmap(priorityIcon[topic.getPriority()], x, y - iconSize / 2, null);
			x += iconSize;
		}
		
		if (topic.getSmiley() != null)
		{
			Bitmap bitmap;
			if (topic.getSmiley() == Smiley.HAPPY)
				bitmap = happyIcon;
			else if (topic.getSmiley() == Smiley.NEUTRAL)
				bitmap = neutralIcon;
			else
				bitmap = sadIcon;
			x += blockShift;
			c.drawBitmap(bitmap, x, y - iconSize / 2, null);
			x += iconSize;
		}
		
		if (topic.getTaskCompletion() != null)
		{
			Bitmap bitmap;
			if (topic.getTaskCompletion() == TaskCompletion.TO_DO)
				bitmap = toDoIcon;
			else
				bitmap = toDoIcon;

			x += blockShift;
			c.drawBitmap(bitmap, x, y - iconSize / 2, null);
			x += iconSize;
		}		

		//draw text		
		x += blockShift;
		p.setColor(topic.getBgColor());
		p.setAlpha(255);
		c.drawRect(x, y - ySize / 2 - borderSize, x + xSize + borderSize * 2, y + ySize / 2 + borderSize, p);
		
		x += borderSize - textShift;
		
		for (TextBlock cur : text.getTextBlocks())
		{
			p.setTextSize(cur.getFormat().getFontSize());
			p.setColor(cur.getFormat().getFontColor());
			p.getTextBounds(cur.getText(), 0, cur.getText().length(), r);
			x += textShift;
			c.drawText(cur.getText(), x, y + ySize / 2, p);
			x += r.width();
		}
		
		x += borderSize;
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
		if (toUpdate)
			points.clear();
		int[] temp = drawTopic(map.getRoot(), x, y, c);
		toUpdate = false;
		Paint p = new Paint();
		p.setColor(Color.BLACK);
		c.drawLine(x + temp[0], y, x + temp[0], y + temp[1], p);
		c.drawLine(x, y + temp[1], x + temp[0], y + temp[1], p);
		p.setColor(Color.RED);
		for (touchPoint point : points)
		{
			c.drawCircle(point.x, point.y, 2, p);
		}
	}

	@Override
	public int getHeight() {
		return 1000;
	}

	@Override
	public int getWidth() {
		return 1000;
	}

	@Override
	public void onTouch(int x, int y) {
		if (x == 10000)
		{
			toUpdate = true;
			return;
		}
		for (touchPoint point : points)
		{
			if (Math.hypot(point.x - x, point.y - y) <= outerSize)
			{
				open.put(point.id, !open.get(point.id));
				toUpdate = true;
				break;
			}
		}
	}

}
