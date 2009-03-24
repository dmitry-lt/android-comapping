/*
 * TestMapView Controller
 * Android Comapping, 2009
 * Last change: Korshakov Stepan
 * 
 * Render of map in comapping.com style
 */
package com.comapping.android.model;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class MapRender {

	private class Item
	{
		public Item[] childs;
		public int subtreeWidth;
		public Topic topicData;
		
		
		private static final int UNDERLINE_OFFSET = 3;
		
		private static final int BORDER_SIZE = 8;
		
		private static final int ROW_LENGTH = 30;
		
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
		
		public int getRowCount()
		{
			return  topicData.getText().length()/ROW_LENGTH + 1;
		}
		
		public int getWidth()
		{
			Paint p = new Paint();
			Rect bounds = new Rect();
			
			int linesCount = getRowCount();
			int width = 0;
			for(int i = 0; i < linesCount; i++)
			{
				String rowText = getRow(i);
				p.getTextBounds(rowText, 0, rowText.length(), bounds);
				width =Math.max(width, bounds.width());
				//width += bounds.width();
			}
			return width + BORDER_SIZE*2;
		}
		
		public int getHeight()
		{
			Paint p = new Paint();
			Rect bounds = new Rect();
			int linesCount = getRowCount();
			int height = 0;
			for(int i = 0; i < linesCount; i++)
			{
				String rowText = getRow(i);
				p.getTextBounds(rowText, 0, rowText.length(), bounds);

				height+=bounds.height();
			}
			return height + UNDERLINE_OFFSET + BORDER_SIZE*2;
		}
		
		private int getOffset()
		{
			return (subtreeWidth-getHeight())/2; 
		}
		
		public void draw(int x, int y, Canvas c)
		{
			String text = topicData.getText();
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
				
				//c.drawText(text, x + BORDER_SIZE, y + vertOffset + bounds.height()+BORDER_SIZE, p);
				
				textVertOffset+=bounds.height();
			}
			//c.drawText(text, x + BORDER_SIZE, y + vertOffset + bounds.height()+BORDER_SIZE, p);
		}
		
		public int getUnderlineOffset()
		{
			return getHeight()+ getOffset() - BORDER_SIZE;
		}
	}
	
	
	public static final int FONT_SIZE_LAYER1 = 20;
	public static final int FONT_SIZE_LAYER2 = 20;
	public static final int FONT_SIZE_LAYER3 = 20;

	public static final int FONT_SIZE_LAYERX = 10;
	
	public static final int MAX_TEXT_LEN_IN_ROW = 10;

	Item root;

	public MapRender(Topic mapItem) {
		root = buildTree(mapItem);
		recalcData(root, 0);
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
	
	public void draw(Canvas c) {
		draw(0, 0, root, c);
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
}
