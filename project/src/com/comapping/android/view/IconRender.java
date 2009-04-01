package com.comapping.android.view;

import com.comapping.android.controller.MetaMapActivity;
import com.comapping.android.controller.R;
import com.comapping.android.model.Smiley;
import com.comapping.android.model.TaskCompletion;
import com.comapping.android.model.Topic;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

public class IconRender extends Render {

	private static final int ICON_SIZE = 26;
	private static final int HORISONTAL_MERGING = 5;
	
	private static Bitmap[] priorityIcon = new Bitmap[10];
	
	private static boolean toLoad = true;
	
	private static Bitmap happyIcon;
	private static Bitmap neutralIcon;
	private static Bitmap sadIcon;
	private static Bitmap furiousIcon;
	
	private static Bitmap toDoIcon;
	private static Bitmap twentyFiveIcon;
	private static Bitmap fiftyIcon;
	private static Bitmap seventyFiveIcon;
	private static Bitmap completeIcon;

	private static Bitmap getBitmap(Drawable image)
	{
		Bitmap bitmap = Bitmap.createBitmap(ICON_SIZE, ICON_SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        image.setBounds(0, 0, ICON_SIZE, ICON_SIZE);
        image.draw(canvas);        
        return bitmap;
	}	
	
	private void loadIcons()
	{
		Resources r = MetaMapActivity.getInstance().getResources();
		
		priorityIcon[1] = getBitmap(r.getDrawable(R.drawable.p1));
		priorityIcon[2] = getBitmap(r.getDrawable(R.drawable.p2));
		priorityIcon[3] = getBitmap(r.getDrawable(R.drawable.p3));
		priorityIcon[4] = getBitmap(r.getDrawable(R.drawable.p4));
		priorityIcon[5] = getBitmap(r.getDrawable(R.drawable.p1));
		priorityIcon[6] = getBitmap(r.getDrawable(R.drawable.p1));
		priorityIcon[7] = getBitmap(r.getDrawable(R.drawable.p1));
		priorityIcon[8] = getBitmap(r.getDrawable(R.drawable.p1));
		priorityIcon[9] = getBitmap(r.getDrawable(R.drawable.p1));
		
		happyIcon = getBitmap(r.getDrawable(R.drawable.happy));
		neutralIcon = getBitmap(r.getDrawable(R.drawable.neutral));
		sadIcon = getBitmap(r.getDrawable(R.drawable.sad));
		furiousIcon = getBitmap(r.getDrawable(R.drawable.furious));
		
		toDoIcon = getBitmap(r.getDrawable(R.drawable.to_do));
		twentyFiveIcon = getBitmap(r.getDrawable(R.drawable.to_do));
		fiftyIcon = getBitmap(r.getDrawable(R.drawable.to_do));
		seventyFiveIcon = getBitmap(r.getDrawable(R.drawable.to_do));
		completeIcon = getBitmap(r.getDrawable(R.drawable.to_do));
	}
	
	private int width, height;
	private Topic topic;
	
	public IconRender(Topic topic)
	{
		if (toLoad)
		{
			loadIcons();
			toLoad = false;
		}
		this.topic = topic;
		int c = 0;
		if (topic.getPriority() != 0)			
			c++;
		if (topic.getSmiley() != null)			
			c++;		
		if (topic.getTaskCompletion() != null)
			c++;		
		if (c != 0)
		{
			width = ICON_SIZE * c + HORISONTAL_MERGING * (c - 1);
			height = ICON_SIZE;
		}
	}
	
	@Override
	public void draw(int x, int y, int width, int height, Canvas c) {
		
		if (topic.getPriority() != 0)
		{
			c.drawBitmap(priorityIcon[topic.getPriority()], x, y, null);
			x += ICON_SIZE + HORISONTAL_MERGING;
		}
		
		if (topic.getSmiley() != null)
		{
			if (topic.getSmiley() == Smiley.HAPPY)
				c.drawBitmap(happyIcon, x, y, null);
			else if (topic.getSmiley() == Smiley.NEUTRAL)
				c.drawBitmap(neutralIcon, x, y, null);
			else if (topic.getSmiley() == Smiley.SAD)
				c.drawBitmap(sadIcon, x, y, null);
			else
				c.drawBitmap(furiousIcon, x, y, null);
			x += ICON_SIZE + HORISONTAL_MERGING;
		}
		
		if (topic.getTaskCompletion() != null)
		{
			if (topic.getTaskCompletion() == TaskCompletion.TO_DO)
				c.drawBitmap(toDoIcon, x, y, null);
			else if (topic.getTaskCompletion() == TaskCompletion.TWENTY_FIVE)
				c.drawBitmap(twentyFiveIcon, x, y, null);
			else if (topic.getTaskCompletion() == TaskCompletion.FIFTY)
				c.drawBitmap(fiftyIcon, x, y, null);
			else if (topic.getTaskCompletion() == TaskCompletion.SEVENTY_FIVE)
				c.drawBitmap(seventyFiveIcon, x, y, null);
			else
				c.drawBitmap(completeIcon, x, y, null);
			x += ICON_SIZE + HORISONTAL_MERGING;
		}
		
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
		// TODO Auto-generated method stub
		
	}

}
