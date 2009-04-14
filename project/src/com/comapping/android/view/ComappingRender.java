/*
 * TestMapView Controller
 * Android Comapping, 2009
 * Autor: Korshakov Stepan
 * 
 * Render of map in comapping.com style
 */
package com.comapping.android.view;

import com.comapping.android.model.Topic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class ComappingRender extends MapRender {

	private class Item {
		private static final int BORDER_SIZE = 10;
		public Item[] childs;
		public Item parent = null;
		public Topic topicData;

		private boolean childsVisible = true;

		private TopicRender render;

		public Item(Topic topic) {
			topicData = topic;
			render = new TopicRender(topicData, context);
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

		private static final int OUTER_SIZE = 15;
		private static final int CIRCLE_WIDTH = 2;
		private static final int PLUS_LENGTH = 7;
		private static final int PLUS_WIDTH = 2;

		Paint p = new Paint();

		public void draw(int x, int y, Canvas c) {
			int vertOffset = getOffset();

			// Rendering topic
			render.draw(x, y + vertOffset, render.getWidth(), render
					.getHeight(), c);

			// Drawing lines
			c.drawLine(x, y + getUnderlineOffset(), x + render.getWidth(), y
					+ getUnderlineOffset(), new Paint());

			if (topicData.getChildrenCount() != 0) {
				// Draw +/- circle
				drawCircle(x + render.getWidth() + OUTER_SIZE, y
						+ getUnderlineOffset(), c);
			}
		}

		private void drawCircle(int x, int y, Canvas c) {
			c.drawCircle(x, y, OUTER_SIZE, p);
			p.setColor(Color.WHITE);
			c.drawCircle(x, y, OUTER_SIZE - CIRCLE_WIDTH, p);
			p.setColor(Color.GRAY);
			c.drawRect(x - PLUS_LENGTH, y - PLUS_WIDTH, x + PLUS_LENGTH, y
					+ PLUS_WIDTH, p);

			if (!this.isChildsVisible())
				c.drawRect(x - PLUS_WIDTH, y - PLUS_LENGTH, x + PLUS_WIDTH, y
						+ PLUS_LENGTH, p);
		}

		public boolean isOverButton(int x, int y) {
			return ((x >= render.getWidth())
					&& (y >= render.getLineOffset() - OUTER_SIZE)
					&& (x <= render.getWidth() + OUTER_SIZE * 2) && (y <= render
					.getLineOffset()
					+ OUTER_SIZE));
		}

		public int getWidth() {
			return render.getWidth() + OUTER_SIZE * 2;
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
	ScrollController scrollController;
	
	private Context context;

	public ComappingRender(Context context, Topic map) {
		this.context = context;
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

	private boolean isOnScreen(int x, int y, Item itm, int width, int height) {
		y += itm.getOffset();
		if (x + itm.getWidth() < 0)
			return false;

		if (y + itm.getHeight() < 0)
			return false;

		if (x > width)
			return false;

		if (y > height)
			return false;

		return true;
	}

	private void draw(int baseX, int baseY, Item itm, int width, int height,
			Canvas c) {

		if (isOnScreen(baseX, baseY, itm, width, height)) {
			itm.draw(baseX, baseY, c);
		}

		if (itm.isChildsVisible()) {
			int dataLen = itm.getWidth();

			int vertOffset = 0;
			for (Item i : itm.childs) {
				draw(baseX + dataLen, baseY + vertOffset, i, width, height, c);
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

				// Connecting base and first
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

	private int renderZoneHeight = 0;

	private final int getVertOffset() {
		if (getHeight() >= renderZoneHeight)
			return 0;
		else
			return (renderZoneHeight - getHeight()) / 2;
	}

	@Override
	public void draw(int x, int y, int width, int height, Canvas c) {
		renderZoneHeight = height;
		draw(-x, -y + getVertOffset(), root, width, height, c);
	}

	@Override
	public void onTouch(int x, int y) {
		onTouch(0, getVertOffset(), root, x, y);
	}

	private boolean onTouch(int baseX, int baseY, Item itm, int destX, int destY) {
		int yStart = itm.getOffset() + baseY;
		int xStart = baseX;

		if (itm.isOverButton(destX - xStart, destY - yStart)) {

			if (itm.isChildsVisible())
				itm.hideChilds();
			else
				itm.showChilds();

			itm.render.setSelected(true);

			scrollController.smoothScroll(baseX, itm.getOffset() + baseY);

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

	@Override
	public void setScrollController(ScrollController scroll) {
		scrollController = scroll;
	}

	@Override
	public void onKeyDown(int keyCode) {
		// TODO Auto-generated method stub
		
	}
}