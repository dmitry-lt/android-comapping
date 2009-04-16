/*
 * TestMapView Controller
 * Android Comapping, 2009
 * Autor: Korshakov Stepan
 * 
 * Render of map in comapping.com style
 * 
 * Some comments:
 * 
 * WTF is render zone?
 * 
 * Look at the acii-picture:
 * 
 *          |--------|
 *          |        |-<Child1> 
 *          |        |
 * <Topic> -|        |-<Child2>
 *          |        |
 *          |<Topic2>|
 *          |        |  .......
 *          |        |
 *          |        |
 *          |        |-<Childn>
 *          |--------|
 *              |
 *             /|\
 *              |
 * This rectangle is render zone.
 * Left upper corner is (0,0)
 * 
 * Render zone width is Item.getRenderZoneWidth(), but must be changed
 * Render zone height is Item.getRenderZoneHeight()
 */
package com.comapping.android.view;

import com.comapping.android.model.Topic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.KeyEvent;

public class ComappingRender extends MapRender {

	private static final String DEBUG_TAG = "ComappingRender";
	private static final int LINE_COLOR = Color.GRAY;

	/**
	 * Container for Topic's whith some helpful functions for drawing,
	 * calculating positions, working with childs
	 * 
	 * @author Korshakov Stepan
	 * 
	 */
	private class Item {

		private static final int HORIZONTAL_BORDER_SIZE = 10;

		/* ---------- Tree data ---------- */
		
		public Item[] children;
		public Item parent = null;

		/* ----- Comapping tree data ----- */
		public Topic topicData;

		/* --------- Item state ---------- */
		private boolean childrenVisible = true;

		/* ------ Rendering helpers ------ */
		private TopicRender render;
		private PlusMinusIcon plusMinusIcon;

		private Paint p = new Paint();

		/* ---------- Lazy buffers ---------- */

		private int lazyOffset = -1;
		private int lazyTreeWidth = -1;
		private int lazyRenderZoneHeight = -1;
		private int lazyAbsoluteX = -1;
		private int lazyAbsoluteY = -1;

		/**
		 * Constructor of Render Item
		 * 
		 * @param topic
		 *            Topic for container
		 */
		public Item(Topic topic) {
			topicData = topic;
			render = new TopicRender(topicData, context);
			render.setMaxWidth(300);
			plusMinusIcon = new PlusMinusIcon(!childrenVisible);

			p.setColor(LINE_COLOR);
		}

		/* ---------- Children code ---------- */

		/**
		 * Shows/Hides children
		 * 
		 * @param isVisible
		 *            true - show children, false - hide children
		 */
		public void setChildrenVisible(boolean isVisible) {
			childrenVisible = isVisible;
			plusMinusIcon.isPlus = !childrenVisible;
			clearLazyBuffers();
		}

		/**
		 * Returns if children is visible
		 * 
		 * @return Visible state
		 */
		public boolean isChildsVisible() {
			return childrenVisible;
		}

		/* ---------- Draw code ---------- */

		/**
		 * Drawing function
		 * 
		 * @param x
		 *            x-coord for drawing
		 * @param y
		 *            y-coord for drawing
		 * @param c
		 *            Canvas for drawing
		 */
		public void draw(int x, int y, Canvas c) {
			int vertOffset = getTopicOffset();

			// Rendering topic
			render.draw(x, y + vertOffset, render.getWidth(), render
					.getHeight(), c);

			// Drawing lines
			c.drawLine(x, y + getUnderlineOffset(), x + render.getWidth(), y
					+ getUnderlineOffset(), p);

			if (topicData.getChildrenCount() != 0) {
				// Draw +/- circle

				plusMinusIcon.draw(x + render.getWidth(), y
						+ getUnderlineOffset() - PlusMinusIcon.RADIUS,
						PlusMinusIcon.WIDTH, PlusMinusIcon.HEIGHT, c);

			}
		}

		/* ---------- Input code ---------- */

		/**
		 * Checks if point is over button
		 * 
		 * @param x
		 *            x-coord of point in Item coordinate system
		 * @param y
		 *            y-coord of point in Item coordinate system
		 * @return if point is over button
		 */
		public boolean isOverButton(int x, int y) {
			return ((x >= render.getWidth())
					&& (y >= render.getLineOffset() - PlusMinusIcon.RADIUS)
					&& (x <= render.getWidth() + PlusMinusIcon.WIDTH) && (y <= render
					.getLineOffset()
					+ PlusMinusIcon.RADIUS));
		}

		/**
		 * Checks if point is over topic
		 * 
		 * @param x
		 *            x-coord of point in Item coordinate system
		 * @param y
		 *            y-coord of point in Item coordinate system
		 * @return if point is over topic
		 */
		public boolean isOverTopic(int x, int y) {
			return (!isOverButton(x, y))
					&& ((x >= 0) && (y >= 0) && (x <= render.getWidth()) && (y <= render
							.getLineOffset()));
		}

		/* -- Topic sizes and positions code -- */

		/**
		 * Return width of topic (without children)
		 * 
		 * @return Width of a rendering topic
		 */
		public int getTopicWidth() {
			return render.getWidth() + PlusMinusIcon.WIDTH;
		}

		/**
		 * Return height of topic (without children)
		 * 
		 * @return Height of a rendering topic
		 */
		public int getTopicHeight() {
			return render.getHeight() + HORIZONTAL_BORDER_SIZE;
		}

		/**
		 * Returns vertical offset from (0,0) (in Item coord system) to draw
		 * topic centered in render zone
		 * 
		 * @return
		 */
		public int getTopicOffset() {
			if (lazyOffset == -1) {
				lazyOffset = (getRenderZoneHeight() - getTopicHeight()) / 2;
			}

			return lazyOffset;
		}

		/**
		 * Returns offset for underline in render zone coord system
		 * 
		 * @return offset to line from (0,0) of render zone
		 */
		public int getUnderlineOffset() {
			return getTopicOffset() + render.getLineOffset();
		}

		/* ---------- Tree and zone sizes code ---------- */

		/**
		 * Returns render zone width
		 * 
		 * @return Width of the zone
		 */
		public int getRenderZoneWidth() {
			return this.getTopicWidth();
		}

		/**
		 * Returns render zone height
		 * 
		 * @return Height of the zone
		 */
		public int getRenderZoneHeight() {
			if (lazyRenderZoneHeight == -1) {
				int w = 0;
				if (childrenVisible)
					for (Item i : children) {
						w += i.getRenderZoneHeight();
					}
				lazyRenderZoneHeight = Math.max(w, getTopicHeight());
			}
			return lazyRenderZoneHeight;
		}

		/**
		 * Calculates width of a whole tree
		 * 
		 * @return width in pixels
		 */
		public int getTreeWidth() {
			if (lazyTreeWidth == -1) {
				int w = 0;
				if (childrenVisible)
					for (Item i : children) {
						w = Math.max(i.getTreeWidth(), w);
					}
				lazyTreeWidth = this.getRenderZoneWidth() + w;
			}
			return lazyTreeWidth;
		}

		/* ---------- Global positions code ---------- */

		/**
		 * Calculates absolute positions of Item
		 */
		private void calcAbsPositions() {

			// TODO: Must to do it in parent Item. Work time - O(n^2). Must be
			// O(n).

			if (parent == null) {
				lazyAbsoluteX = 0;
				lazyAbsoluteY = 0;
				return;
			}

			int baseX = this.parent.getAbsoluteX();
			int baseY = this.parent.getAbsoluteY();
			int dataLen = this.parent.getTopicWidth();
			int vertOffset = 0;
			for (Item i : this.parent.children) {

				if (i == this) {
					lazyAbsoluteX = baseX + dataLen;
					lazyAbsoluteY = baseY + vertOffset;
					return;
				}

				vertOffset += i.getRenderZoneHeight();
			}
		}

		/**
		 * Calculates absolute X-coordinate of render zone
		 * 
		 * @return X-coord for render zone
		 */
		public int getAbsoluteX() {
			if (lazyAbsoluteX == -1) {
				calcAbsPositions();
			}
			return lazyAbsoluteX;
		}

		/**
		 * Calculates absolute Y-coordinate of render zone
		 * 
		 * @return Y-coord for render zone
		 */
		public int getAbsoluteY() {
			if (lazyAbsoluteY == -1) {
				calcAbsPositions();
			}
			return lazyAbsoluteY;
		}

		/* ---------- Clear buffers code ---------- */

		/**
		 * Clears buffers for absolute coodinates
		 */
		public void clearLazyAbsPosBuffers() {
			lazyAbsoluteY = -1;
			lazyAbsoluteX = -1;
			for (int i = 0; i < children.length; i++)
				children[i].clearLazyAbsPosBuffers();
		}

		/**
		 * Clears all buffers
		 */
		private void clearLazyBuffers() {
			if (parent != null)
				parent.clearLazyBuffers();

			lazyAbsoluteY = -1;
			lazyAbsoluteX = -1;

			lazyOffset = -1;
			lazyRenderZoneHeight = -1;
			lazyTreeWidth = -1;
		}

		/* ---------- Misc code ---------- */

		/**
		 * Returns index in parent.children array of this Item
		 * 
		 * @return index in parent.children array
		 */
		public int getIndex() {
			// May be I should add buffering?
			int index = -1;
			ComappingRender.Item[] parentChildren = parent.children;
			for (int i = 0; i < parentChildren.length; i++) {
				if (parentChildren[i] == selected) {
					index = i;
					break;
				}
			}
			return index;
		}
	}

	/* ------------------------------
	 * Variables
	 * ------------------------------
	 */
	
	//Offsets to render
	private int xOffset = 0, yOffset = 0;
	
	//Tree root
	private Item root = null;
	
	//Selected Item
	private Item selected = null;
	
	//Controller of scrolling
	private ScrollController scrollController = null;
	
	//Execution context
	private Context context;

	/**
	 * Render constructor
	 * 
	 * @param context
	 *            Execution context
	 * @param map
	 *            Root element
	 */
	public ComappingRender(Context context, Topic map) {
		this.context = context;
		root = buildTree(map, null);
	}

	/**
	 * Building custom tree with some helpful information
	 * 
	 * @param itm
	 *            Root item for (sub)tree
	 * @param parent
	 *            Parent item (null for root elements)
	 * @return Tree or subtree
	 */
	private Item buildTree(Topic itm, Item parent) {
		Item res = new Item(itm);
		res.children = new Item[itm.getChildrenCount()];
		res.parent = parent;

		int index = 0;
		for (Topic i : itm) {
			res.children[index++] = buildTree(i, res);
		}
		return res;
	}

	/**
	 * Checks is Item is on screen. (MUST BE CHANGED FOR BETTER DESIGN)
	 * 
	 * @param x
	 *            Offset to x0 (Remove this)
	 * @param y
	 *            Offset to y0 (Remove this)
	 * @param itm
	 *            Item for checking
	 * @param width
	 *            Screen width (Remove this)
	 * @param height
	 *            Screen height (Remove this)
	 * @return
	 */
	private boolean isOnScreen(int x, int y, Item itm, int width, int height) {
		y += itm.getTopicOffset();
		if (x + itm.getTopicWidth() < 0)
			return false;

		if (y + itm.getTopicHeight() < 0)
			return false;

		if (x > width)
			return false;

		if (y > height)
			return false;

		return true;
	}

	/**
	 * Draws Item and it's children (MUST BE CHANGED FOR BETTER DESIGN)
	 * 
	 * @param baseX
	 *            basic x-offset for drawing
	 * @param baseY
	 *            basic y-offset for drawing
	 * @param itm
	 *            Item to draw
	 * @param width
	 *            Screen width (Remove this)
	 * @param height
	 *            Screen height (Remove this)
	 * @param c
	 *            Canvas to drawing
	 */
	private void draw(int baseX, int baseY, Item itm, int width, int height,
			Canvas c) {

		if (isOnScreen(baseX, baseY, itm, width, height)) {

			itm.draw(itm.getAbsoluteX() - xOffset,
					itm.getAbsoluteY() - yOffset, c);
		}

		if (itm.isChildsVisible()) {
			int dataLen = itm.getTopicWidth();

			int vertOffset = 0;
			for (Item i : itm.children) {
				draw(baseX + dataLen, baseY + vertOffset, i, width, height, c);
				vertOffset += i.getRenderZoneHeight();
			}

			if (itm.children.length != 0) {
				Paint p = new Paint();
				p.setColor(LINE_COLOR);

				Item first = itm.children[0];
				Item last = itm.children[itm.children.length - 1];

				// Calculating offset for last child
				vertOffset -= last.getRenderZoneHeight();

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
		return root.getTreeWidth();
	}

	public int getHeight() {
		return root.getRenderZoneHeight();
	}


	private int renderZoneHeight = 0;

	/**
	 * Returns offset for centering small o collapsed maps
	 * 
	 * @return Offset for drawing
	 */
	private final int getVertOffset() {
		if (getHeight() >= renderZoneHeight)
			return 0;
		else
			return (renderZoneHeight - getHeight()) / 2;
	}


	@Override
	public void draw(int x, int y, int width, int height, Canvas c) {
		renderZoneHeight = height;
		xOffset = x;
		yOffset = y - getVertOffset();
		draw(-x, -y + getVertOffset(), root, width, height, c);
	}

	@Override
	public void onTouch(int x, int y) {
		onTouch(0, getVertOffset(), root, x, y);
	}


	/**
	 * Recursive processing Touch events
	 * @param baseX basic x-offset (remove it)
	 * @param baseY basic y-offset (remove it)
	 * @param itm Item to check
	 * @param destX destination x-coord
	 * @param destY destination y-coord
	 * @return if captured
	 */
	private boolean onTouch(int baseX, int baseY, Item itm, int destX, int destY) {
		int yStart = itm.getTopicOffset() + baseY;
		int xStart = baseX;

		if (itm.isOverButton(destX - xStart, destY - yStart)) {

			changeChildVisibleStatus(itm);

			return true;
		} else if (itm.isOverTopic(destX - xStart, destY - yStart)) {
			focusTopic(itm);
		}

		if (itm.isChildsVisible()) {
			int dataLen = itm.getTopicWidth();

			int vertOffset = 0;
			for (Item i : itm.children) {

				if (onTouch(baseX + dataLen, baseY + vertOffset, i, destX,
						destY))
					return true;

				vertOffset += i.getRenderZoneHeight();
			}
		}
		return false;
	}

	@Override
	public void setScrollController(ScrollController scroll) {
		scrollController = scroll;
	}

	/*
	 * Some helper functions
	 */


	private final void focusTopic(Item topic) {
		if (selected != null)
			selected.render.setSelected(false);

		topic.render.setSelected(true);
		selected = topic;

		scrollController.smoothScroll(topic.getAbsoluteX(), topic
				.getTopicOffset()
				+ topic.getAbsoluteY());
	}

	private final void changeChildVisibleStatus(Item topic) {
		int oldAbsPosX = topic.getAbsoluteX();
		int oldAbsPosY = topic.getAbsoluteY() + topic.getTopicOffset();

		topic.setChildrenVisible(!topic.isChildsVisible());

		root.clearLazyAbsPosBuffers();

		int newAbsPosX = topic.getAbsoluteX();
		int newAbsPosY = topic.getAbsoluteY() + topic.getTopicOffset();

		scrollController.intermediateScroll(
				(oldAbsPosX - xOffset) + newAbsPosX, (oldAbsPosY - yOffset)
						+ newAbsPosY);

		focusTopic(topic);
	}

	/*
	 * D-Pad functions.
	 */

	private final void moveLeft() {
		if (selected == null) {
			focusTopic(root);
			return;
		}

		if (selected.parent == null) {
			focusTopic(selected);
			return;
		}

		focusTopic(selected.parent);
	}

	private final void moveUp() {
		if (selected == null) {
			focusTopic(root);
			return;
		}
		if (selected.parent == null) {
			focusTopic(selected);
			return;
		}

		int index = selected.getIndex();

		if (index == -1) {
			Log.e(DEBUG_TAG, "Denger! Seems to be broken tree!");
			return;
		}

		if (index > 0) // Is not highest child
		{
			focusTopic(selected.parent.children[index - 1]);
		} else {
			// TODO: Focusing when itm is highest child
		}
	}

	private final void moveDown() {
		if (selected == null) {
			focusTopic(root);
			return;
		}
		if (selected.parent == null) {
			focusTopic(selected);
			return;
		}

		int index = selected.getIndex();
		;
		ComappingRender.Item[] parentChilds = selected.parent.children;

		if (index == -1) {
			Log.e(DEBUG_TAG, "Denger! Seems to be broken tree!");
			return;
		}

		if (index < parentChilds.length - 1) // Is not lowest child
		{
			focusTopic(parentChilds[index + 1]);
		} else {
			// TODO: Focusing when itm is lowest child
		}
	}

	private final void moveRight() {
		if (selected == null) {
			focusTopic(root);
			return;
		}
		if (!selected.isChildsVisible()) {
			focusTopic(selected);
			return;
		}
		for (int i = 0; i < selected.children.length; i++) {
			if (selected.children[i].getAbsoluteY()
					+ selected.children[i].getTopicOffset() > selected
					.getAbsoluteY()
					+ selected.getTopicOffset()) {
				if (i == 0)
					focusTopic(selected.children[i]);
				else
					focusTopic(selected.children[i - 1]);

				return;
			}
		}

		if (selected.children.length > 0)
			focusTopic(selected.children[0]);
	}

	@Override
	public void onKeyDown(int keyCode) {
		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			moveLeft();
		}
		if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
			moveUp();
		}
		if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			moveDown();
		}
		if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			moveRight();
		}
		if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
			if (selected != null) {
				changeChildVisibleStatus(selected);
			}
		}
	}
}