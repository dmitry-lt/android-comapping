package com.comapping.android.view;

import java.util.ArrayList;
import java.util.HashMap;

import com.comapping.android.controller.MainController;
import com.comapping.android.model.Map;
import com.comapping.android.model.Topic;
import com.comapping.android.controller.LoginController;
import com.comapping.android.controller.MainController;
import com.comapping.android.controller.MapController;
import com.comapping.android.controller.R;
import com.comapping.android.model.Map;
import com.comapping.android.model.Topic;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class MapView extends View {

	public MapView(Context context, Map map) {
		super(context);
		this.map = map;
		loadImages();
	}	
	
	private static final int imageSize = 15;
	private static final int xShift = 30;
	private static final int yShift = 30;
	private static final int oSize = 10;
	private static final int iSize = 5;
	private static final int bSize = 3;
	
	private Map map;
	private HashMap<Integer, Boolean> open = new HashMap<Integer, Boolean>();
	
	private int x;
	private int y;
	private boolean toUpdate = true;
	private ArrayList<EventPoint> es = new ArrayList<EventPoint>();
	private Bitmap[] prs = new Bitmap[9];
	
	private Bitmap getBitmap(Drawable image)
	{
		Bitmap bitmap = Bitmap.createBitmap(imageSize, imageSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        image.setBounds(0, 0, imageSize, imageSize);
        image.draw(canvas);
        
        return bitmap;
	}
	
	private void loadImages()
	{
		Resources r = MainController.getInstance().getResources();
		prs[1] = getBitmap(r.getDrawable(R.drawable.p1));
		prs[2] = getBitmap(r.getDrawable(R.drawable.p2));
		prs[3] = getBitmap(r.getDrawable(R.drawable.p3));
		prs[4] = getBitmap(r.getDrawable(R.drawable.p4));
/*		prs[5] = getBitmap(r.getDrawable(R.drawable.p5));
		prs[6] = getBitmap(r.getDrawable(R.drawable.p6));
		prs[7] = getBitmap(r.getDrawable(R.drawable.p7));
		prs[8] = getBitmap(r.getDrawable(R.drawable.p8));
		prs[9] = getBitmap(r.getDrawable(R.drawable.p9));*/
	}
	
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
	
	private class RGBColor
	{
		public int[] rgb = new int[3];
		
		public RGBColor(int color)
		{
			rgb[2] = color % 256; color /= 256;
			rgb[1] = color % 256; color /= 256;
			rgb[0] = color % 256; color /= 256;
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
				open.put(id, false);
		}
		x += xShift;
		int[] rgb = (new RGBColor(topic.getBgColor())).rgb;
		p.setARGB(255, rgb[0], rgb[1], rgb[2]);
		Rect r = new Rect();
		String text = topic.getText();
		p.getTextBounds(text, 0, text.length(), r);
		x += 5;
		if (topic.getPriority() != 0)
		{
			c.drawBitmap(prs[topic.getPriority()], x, y - imageSize / 2, null);
			x += imageSize + 5;
		}
		c.drawRect(x, y - r.height() / 2 - bSize, x + r.width() + 2 * bSize, y + r.height() / 2 + bSize, p);
		p.setColor(Color.BLACK);
		c.drawText(text, x + bSize, y + r.height() / 2, p);
		if (topic.getPriority() != 0)
			x -= imageSize + 5;
		x -= 5;
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
