/*
 * TestMapView Controller
 * Android Comapping, 2009
 * Autor: Korshakov Stepan
 * 
 * Render of map in comapping.com style
 */
package com.comapping.android.view;

import com.comapping.android.controller.R;
import com.comapping.android.model.Map;
import com.comapping.android.model.Topic;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class ComappingRender extends Render{

	//Metod is public because it MUST be removed to
	//MapView or to MapActivity.
	//if it would be private member then javac will generate
	//getter and setter for it. It's not good ;)
	public static Bitmap[] icons; 
	
	private class Item
	{
		public Item[] childs;
		public int subtreeWidth;
		public Topic topicData;
		
		
		private static final int UNDERLINE_OFFSET = 3;
		
		private static final int BORDER_SIZE = 8;
		
		private static final int ROW_LENGTH = 30;
		
		private static final int ICON_SIZE = 15;
		private static final int ICON_BORDER = 4;
		
		public String getRow(int id)
		{
			String res = topicData.getText();
			if (id < getRowCount())
			{
				return res.substring(id*ROW_LENGTH, Math.min(res.length(), (id+1)*ROW_LENGTH));
			}
			else
				return "";
		}
		
		private int lazyRowCount = -1;
		public int getRowCount() {
			if (lazyRowCount == -1)
				lazyRowCount = topicData.getText().length() / ROW_LENGTH + 1;
			return lazyRowCount;
		}

		private int lazyWidth = -1;
		public int getWidth() {
			if (lazyWidth == -1) {
				Paint p = new Paint();
				Rect bounds = new Rect();

				int linesCount = getRowCount();
				int width = 0;
				for (int i = 0; i < linesCount; i++) {
					String rowText = getRow(i);
					p.getTextBounds(rowText, 0, rowText.length(), bounds);
					width = Math.max(width, bounds.width());
					// width += bounds.width();
				}

				lazyWidth = width + BORDER_SIZE * 2;
				
				if (topicData.getPriority() != 0)
				{
					lazyWidth += ICON_SIZE + ICON_BORDER*2;
				}
			}
			return lazyWidth;
		}
		
		private int lazyHeight = -1;
		public int getHeight() {
			if (lazyWidth == -1) {
				Paint p = new Paint();
				Rect bounds = new Rect();
				int linesCount = getRowCount();
				int height = 0;
				for (int i = 0; i < linesCount; i++) {
					String rowText = getRow(i);
					p.getTextBounds(rowText, 0, rowText.length(), bounds);

					height += bounds.height();
				}
				lazyHeight = height + UNDERLINE_OFFSET + BORDER_SIZE * 2;
			}
			return lazyHeight;
		}
		
		
		private int lazyOffset = -1;
		private int getOffset()
		{
			if (lazyOffset == -1)
			{
				lazyOffset = (subtreeWidth-getHeight())/2;
			}
			
			return lazyOffset;
		}
		
		public void draw(int x, int y, Canvas c)
		{
			int vertOffset = getOffset();
			
			Paint p = new Paint();
			p.setColor(Color.BLACK);

			int underlineOffset = getUnderlineOffset();
			//Draw underline
			c.drawLine(x, y + underlineOffset, 
					x + getWidth(),y+ underlineOffset, p);

			//Draw text
			int linesCount = getRowCount();
			int textVertOffset = 0;
			for(int i = 0; i < linesCount; i++)
			{
				String rowText = getRow(i);
				
				Rect bounds = new Rect();
				p.getTextBounds(rowText, 
						0, Math.min(rowText.length(), ROW_LENGTH),
						bounds);
				
				c.drawText(rowText, x + BORDER_SIZE, y + vertOffset + bounds.height()+BORDER_SIZE + textVertOffset, p);
				
				textVertOffset+=bounds.height();
			}
			
			//Draw priority icon
			if (topicData.getPriority() != 0)
			{
				c.drawBitmap(icons[topicData.getPriority() - 1], 
						x + getWidth() - BORDER_SIZE - ICON_BORDER - ICON_SIZE, y + getUnderlineOffset() - ICON_SIZE - ICON_BORDER, 
						new Paint());
			}
		}
		
		private int lazyUnderlineOffset = -1;
		public int getUnderlineOffset()
		{
			if (lazyUnderlineOffset == -1)
				lazyUnderlineOffset = getHeight()+ getOffset() - BORDER_SIZE; 
			
			return lazyUnderlineOffset;
		}
	
	}
	
	
	public static final int FONT_SIZE_LAYER1 = 20;
	public static final int FONT_SIZE_LAYER2 = 20;
	public static final int FONT_SIZE_LAYER3 = 20;

	public static final int FONT_SIZE_LAYERX = 10;
	public static final int MAX_TEXT_LEN_IN_ROW = 10;

	public static final int PRIORITY_COUND = 4;
	Item root;

	public ComappingRender(Context context, Topic map) {
		root = buildTree(map);
		recalcData(root, 0);
		
		Resources resourceLib = context.getResources();
		
		icons = new Bitmap[PRIORITY_COUND];
		icons[0] = BitmapFactory.decodeResource(resourceLib, R.drawable.p1);
		icons[1] = BitmapFactory.decodeResource(resourceLib, R.drawable.p2);
		icons[2] = BitmapFactory.decodeResource(resourceLib, R.drawable.p3);
		icons[3] = BitmapFactory.decodeResource(resourceLib, R.drawable.p4);
		
	}
	

	private Item buildTree(Topic itm)
	{
		Item res = new Item();
		res.childs = new Item[itm.getChildrenCount()];
		res.topicData = itm;
		int index = 0;
		for (Topic i : itm) {
			res.childs[index++] = buildTree(i);
		}
		return res;
	}

	void recalcData(Item item, int layerId) {
		if (item == null)
			return;

		int w = 0;
		for (Item i : item.childs) {
			recalcData(i, layerId + 1);
			w += i.subtreeWidth;
		}
		item.subtreeWidth = Math.max(w, item.getHeight());
	}
	private void draw(int baseX, int baseY, Item itm, Canvas c) {
		itm.draw(baseX, baseY, c);
		int dataLen = itm.getWidth(); 
		
		int vertOffset = 0;
		for (Item i : itm.childs) {
			draw(baseX + dataLen, baseY + vertOffset, i, c);
			vertOffset += i.subtreeWidth;
		}
		
		if (itm.childs.length != 0)
		{
			Paint p = new Paint();
			p.setColor(Color.BLACK);
			
			Item first = itm.childs[0];
			Item last = itm.childs[itm.childs.length - 1];
			
			//Calculating offset for last child
			vertOffset-= last.subtreeWidth;
			
			//Connecting childs
			c.drawLine(baseX + dataLen, baseY + first.getUnderlineOffset() , 
					baseX + dataLen, baseY + vertOffset+ last.getUnderlineOffset(), p);
			
		}
	}
	
	public int getWidth()
	{
		return 1000;	
	}
	
	public int getHeight()
	{
		return root.subtreeWidth;	
	}


	@Override
	public void draw(int x, int y, int width, int height, Canvas c) {
		draw(-x, -y, root, c);
	}


	@Override
	public void onTouch(int x, int y) {
		// TODO Auto-generated method stub
		
	}
}
