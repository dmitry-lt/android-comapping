/*
 * TestMapView Controller
 * Android Comapping, 2009
 * Autor: Korshakov Stepan
 * 
 * Render of map in comapping.com style
 */
package com.comapping.android.view;

import com.comapping.android.controller.R;
import com.comapping.android.model.Topic;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class ComappingRender extends Render {

	private class Item {
		private static final int BORDER_SIZE = 10;
		public Item[] childs;
		public Item parent = null;
		public Topic topicData;

		private boolean childsVisible = true;

		private TopicRender render;

		public Item(Topic topic) {
			topicData = topic;
			render = new TopicRender(topicData);
		}

		public void showChilds() {
			childsVisible = true;
			clearLazyBuffers();
		}

		public void hideChilds() {
			childsVisible = false;
			clearLazyBuffers();
		}

		public boolean isChildsVisible() {
			return childsVisible;
		}

		public void draw(int x, int y, Canvas c) {
			int vertOffset = getOffset();

			render.draw(x, vertOffset + y, render.getWidth(), render.getHeight(), c);
			c.drawLine(x, y + getUnderlineOffset(), x + getWidth(), y
					+ getUnderlineOffset(), new Paint());

		}

		public int getWidth() {
			return render.getWidth();
		}

		public int getHeight() {
			return render.getHeight() + BORDER_SIZE;
		}

		private int lazyOffset = -1;

		private int getOffset() {
			if (lazyOffset == -1) {
				lazyOffset = (getSubtreeHeight() - getHeight()) / 2;
			}

			return lazyOffset;
		}

		public int getUnderlineOffset() {
			return getOffset() + render.getLineOffset();
		}

		private int lazySubtreeHeight = -1;

		public int getSubtreeHeight() {
			if (lazySubtreeHeight == -1) {
				int w = 0;
				if (childsVisible)
					for (Item i : childs) {
						w += i.getSubtreeHeight();
					}
				lazySubtreeHeight = Math.max(w, getHeight());
			}
			return lazySubtreeHeight;
		}

		private int lazySubtreeWidth = -1;

		public int getSubtreeWidth() {
			if (lazySubtreeWidth == -1) {
				int w = 0;
				if (childsVisible)
					for (Item i : childs) {
						w = Math.max(i.getSubtreeWidth(), w);
					}
				lazySubtreeWidth = this.getWidth() + w;
			}
			return lazySubtreeWidth;
		}

		private void clearLazyBuffers() {
			if (parent != null)
				parent.clearLazyBuffers();

			lazyOffset = -1;
			lazySubtreeHeight = -1;
			lazySubtreeWidth = -1;
		}
	}

	Item root;

	public ComappingRender(Context context, Topic map) {
		root = buildTree(map, null);
	}

	private Item buildTree(Topic itm, Item parent) {
		Item res = new Item(itm);
		res.childs = new Item[itm.getChildrenCount()];
		res.parent = parent;

		int index = 0;
		for (Topic i : itm) {
			res.childs[index++] = buildTree(i, res);
		}
		return res;
	}

	private void draw(int baseX, int baseY, Item itm, Canvas c) {
		itm.draw(baseX, baseY, c);

		if (itm.isChildsVisible()) {
			int dataLen = itm.getWidth();

			int vertOffset = 0;
			for (Item i : itm.childs) {
				draw(baseX + dataLen, baseY + vertOffset, i, c);
				vertOffset += i.getSubtreeHeight();
			}

			if (itm.childs.length != 0) {
				Paint p = new Paint();
				p.setColor(Color.BLACK);

				Item first = itm.childs[0];
				Item last = itm.childs[itm.childs.length - 1];

				// Calculating offset for last child
				vertOffset -= last.getSubtreeHeight();

				// Connecting childs
				c.drawLine(baseX + dataLen, baseY + first.getUnderlineOffset(),
						baseX + dataLen, baseY + vertOffset
								+ last.getUnderlineOffset(), p);
				
				//Connecting base and first
				c.drawLine(baseX + dataLen, baseY + first.getUnderlineOffset(),
						baseX + dataLen, baseY + itm.getUnderlineOffset(), p);

			}
		}
	}

	public int getWidth() {
		return root.getSubtreeWidth();
	}

	public int getHeight() {
		return root.getSubtreeHeight();
	}

	@Override
	public void draw(int x, int y, int width, int height, Canvas c) {
		draw(-x, -y, root, c);
	}

	@Override
	public void onTouch(int x, int y) {
		onTouch(0, 0, root, x, y);
	}

	private boolean onTouch(int baseX, int baseY, Item itm, int destX, int destY) {
		int yStart = itm.getOffset() + baseY;
		int yEnd = yStart + itm.getHeight();

		int xStart = baseX;
		int xEnd = xStart + itm.getWidth();

		if ((destX > xStart) & (destX < xEnd) & (destY > yStart)
				& (destY < yEnd)) {
			if (itm.isChildsVisible())
				itm.hideChilds();
			else
				itm.showChilds();
			return true;
		}

		if (itm.isChildsVisible()) {
			int dataLen = itm.getWidth();

			int vertOffset = 0;
			for (Item i : itm.childs) {
				if (onTouch(baseX + dataLen, baseY + vertOffset, i, destX,
						destY))
					return true;

				vertOffset += i.getSubtreeHeight();
			}
		}
		return false;
	}
}
