package com.comapping.android.view;

import com.comapping.android.controller.MainController;
import com.comapping.android.controller.R;
import com.comapping.android.model.FormattedText;
import com.comapping.android.model.Map;
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

	private static final int iconSize = 26;
	private static final int xShift = 30;
	private static final int yShift = 30;
	private static final int outerSize = 10;
	private static final int innerSize = 5;
	private static final int borderSize = 3;
	private static final int blockShift = 5;
	private static final int textShift = 3;
	
	private Map map;
	private Bitmap[] priorityIcon = new Bitmap[10];
	private Bitmap happyIcon;
	
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
	}

	private Bitmap getBitmap(Drawable image)
	{
		Bitmap bitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        image.setBounds(0, 0, iconSize, iconSize);
        image.draw(canvas);        
        return bitmap;
	}
	
	private int[] drawTopic(Topic topic, int x, int y, Canvas c)
	{
		if (topic == null)
		{
			return new int[2];
		}
		
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
		y += ty;
		
		p.setColor(Color.GRAY);
		c.drawLine(x, y, x + xShift, y, p);
		c.drawCircle(x, y, outerSize, p);
		x += xShift;
		
		//draw icons
		if (topic.getPriority() != 0)
		{
			x += blockShift;
			c.drawBitmap(priorityIcon[topic.getPriority()], x, y - iconSize / 2, null);
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
		return new int[2];
	}
	
	@Override
	public void draw(int x, int y, int width, int height, Canvas c) {
		drawTopic(map.getRoot(), x, y, c);
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
		// TODO Auto-generated method stub
		
	}

}
