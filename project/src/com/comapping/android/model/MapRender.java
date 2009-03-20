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

	private class MapItemData {
		public MapItemData(int w) {
			subtreeWidth = w;
		}

		public int subtreeWidth;
	}
	
	private static class ItemDrawer
	{
		private static int UNDERLINE_OFFSET = 3;
		
		private static int BORDER_SIZE = 8;
		
		public static int getWidth(Topic item)
		{
			String text = item.getText();
			Paint p = new Paint();
			Rect bounds = new Rect();
			p.getTextBounds(text, 0, text.length(), bounds);
			return bounds.width() + BORDER_SIZE*2;
		}
		
		public static int getHeight(Topic item)
		{
			String text = item.getText();
			Paint p = new Paint();
			Rect bounds = new Rect();
			p.getTextBounds(text, 0, text.length(), bounds);
			return bounds.height() + UNDERLINE_OFFSET + BORDER_SIZE*2;
		}
		
		private static int getOffset(Topic item)
		{
			return ((((MapItemData) item.renderData).subtreeWidth)-getHeight(item))/2; 
		}
		
		public static void draw(Topic item, int x, int y, Canvas c)
		{
			String text = item.getText();
			int vertOffset = getOffset(item);
			
			Paint p = new Paint();
			p.setColor(Color.BLACK);

			//For underline length
			Rect bounds = new Rect();
			p.getTextBounds(text, 0, text.length(), bounds);

			//Draw underline
			c.drawLine(x, y + vertOffset+ bounds.height() + UNDERLINE_OFFSET + BORDER_SIZE, 
					x + bounds.width() + BORDER_SIZE*2, y + vertOffset + bounds.height() + UNDERLINE_OFFSET + BORDER_SIZE, p);

			//Draw text
			c.drawText(text, x + BORDER_SIZE, y + vertOffset + bounds.height()+BORDER_SIZE, p);
		}
		
		public static int getUnderlineOffset(Topic item)
		{
			return getHeight(item)+ getOffset(item) - BORDER_SIZE;
		}
	}

	public static final int FONT_SIZE_LAYER1 = 20;
	public static final int FONT_SIZE_LAYER2 = 20;
	public static final int FONT_SIZE_LAYER3 = 20;

	public static final int FONT_SIZE_LAYERX = 10;
	
	public static final int MAX_TEXT_LEN_IN_ROW = 10;

	Topic root;

	public MapRender(Topic mapItem) {
		root = mapItem;
		recalcData(root, 0);
	}

	void recalcData(Topic item, int layerId) {
		if (item == null)
			return;

		int w = 0;
		for (Topic i : item.getChildTopics()) {
			recalcData(i, layerId + 1);
			w += ((MapItemData) i.renderData).subtreeWidth;
		}
		item.renderData = new MapItemData(Math.max(w, ItemDrawer.getHeight(item)));
	}
	
	public void draw(Canvas c) {
		draw(0, 0, root, c);
	}

	private void draw(int baseX, int baseY, Topic itm, Canvas c) {

		//MapItemData data = (MapItemData) itm.renderData;
		
		ItemDrawer.draw(itm, baseX, baseY, c);
		int dataLen = ItemDrawer.getWidth(itm); 
		
		int vertOffset = 0;
		for (Topic i : itm.getChildTopics()) {
			draw(baseX + dataLen, baseY + vertOffset, i, c);
			vertOffset += (((MapItemData) i.renderData).subtreeWidth);
		}
		
		if (itm.getChildrenCount() != 0)
		{
			Paint p = new Paint();
			p.setColor(Color.BLACK);
			
			Topic first = itm.getChildByIndex(0);
			Topic last = itm.getChildByIndex(itm.getChildrenCount() - 1);
			
			//Calculating offset for last child
			vertOffset-= (((MapItemData) last.renderData).subtreeWidth);
			
			//Connecting childs
			c.drawLine(baseX + dataLen, baseY + ItemDrawer.getUnderlineOffset(first) , 
					baseX + dataLen, baseY + vertOffset+ ItemDrawer.getUnderlineOffset(last), p);
			
//			
//			c.drawLine(baseX, baseY + offset + 3, baseX + dataLen, baseY + offset + 3, p);
		}
	}
}
