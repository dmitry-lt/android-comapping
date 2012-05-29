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
 * Render zone width is TopicView.getRenderZoneWidth(), but must be changed
 * Render zone height is TopicView.getRenderZoneHeight()
 */
package com.comapping.android.map.render.comapping;

import java.util.ArrayList;

import com.comapping.android.map.ScrollController;
import com.comapping.android.map.model.map.Map;
import com.comapping.android.map.model.map.Topic;
import com.comapping.android.map.render.MapRender;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.KeyEvent;

public class ComappingRender extends MapRender {

	private static final String DEBUG_TAG = "ComappingRender";
	private static final int LINE_COLOR = Color.GRAY;

	/*
	 * ------------------------------ Variables ------------------------------
	 */

	// Offsets to render
	private int xOffset = 0, yOffset = 0;

	// Tree root
	private TopicView root = null;

	// Selected TopicView
	private TopicView selected = null;

	// Controller of scrolling
	private ScrollController scrollController = null;

	// All items
	private ArrayList<TopicView> items = new ArrayList<TopicView>();

	// Execution context
	private Context context;

	/**
	 * Render constructor
	 * 
	 * @param context
	 *            Execution context
	 * @param map
	 *            Root element
	 */
	public ComappingRender(Context context, Map map) {
		this.context = context;
		root = buildTree(map.getRoot(), null);
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
	private TopicView buildTree(Topic itm, TopicView parent) {
		TopicView res = new TopicView(itm, context);
		res.children = new TopicView[itm.getChildrenCount()];
		res.parent = parent;
		items.add(res);
		int index = 0;
		int childrenCount = itm.getChildrenCount();
	
		for (int i = 0; i < childrenCount; i++) {
			Topic child = itm.getChildByIndex(i);
			res.children[index++] = buildTree(child, res);
		}
		return res;
	}

	/**
	 * Checks is TopicView is on screen. (MUST BE CHANGED FOR BETTER DESIGN)
	 * 
	 * @param x
	 *            Offset to x0 (Remove this)
	 * @param y
	 *            Offset to y0 (Remove this)
	 * @param itm
	 *            TopicView for checking
	 * @param width
	 *            Screen width (Remove this)
	 * @param height
	 *            Screen height (Remove this)
	 * @return
	 */
	private boolean isOnScreen(int x, int y, TopicView itm, int width,
			int height) {
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
	 * Draws TopicView and it's children (MUST BE CHANGED FOR BETTER DESIGN)
	 * 
	 * @param baseX
	 *            basic x-offset for drawing
	 * @param baseY
	 *            basic y-offset for drawing
	 * @param itm
	 *            TopicView to draw
	 * @param width
	 *            Screen width (Remove this)
	 * @param height
	 *            Screen height (Remove this)
	 * @param c
	 *            Canvas to drawing
	 */
	private void draw(int baseX, int baseY, TopicView itm, int width,
			int height, Canvas c) {

		if (isOnScreen(baseX, baseY, itm, width, height)) {

			itm.draw(itm.getRenderZoneX() - xOffset, itm.getRenderZoneY()
					- yOffset, c);
		}

		if (itm.isChildrenVisible()) {
			int dataLen = itm.getTopicWidth();

			int vertOffset = 0;
			for (TopicView i : itm.children) {
				draw(baseX + dataLen, baseY + vertOffset, i, width, height, c);
				vertOffset += i.getRenderZoneHeight();
			}

			if (itm.children.length != 0) {
				Paint p = new Paint();
				p.setColor(LINE_COLOR);

				TopicView first = itm.children[0];
				TopicView last = itm.children[itm.children.length - 1];

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

	private void initTree(TopicView itm) {
		if (itm.getRenderZoneX() + itm.getRenderZoneWidth() > renderZoneWidth) {
			setChildrenVisible(itm, false);
		} else {
			setChildrenVisible(itm, true);
		}
		for (TopicView i : itm.children)
			initTree(i);
	}

	@Override
	public int getWidth() {
		return root.getTreeWidth();
	}

	@Override
	public int getHeight() {
		return root.getRenderZoneHeight();
	}

	private int renderZoneHeight = -1;
	private int renderZoneWidth = -1;

	/**
	 * Returns offset for centering small o collapsed maps
	 * 
	 * @return Offset for drawing
	 */
	private final int getVertOffset() {
		// return 0;
		if (getHeight() >= renderZoneHeight)
			return 0;
		else
			return (renderZoneHeight - getHeight()) / 2;
	}

	@Override
	public void draw(int x, int y, int width, int height, Canvas c) {

		if (renderZoneWidth == -1) {
			renderZoneHeight = height;
			renderZoneWidth = width;
			initTree(root);
		} else {
			renderZoneHeight = height;
			renderZoneWidth = width;
		}

		if (selected == null) {
			centerRoot();
		}

		xOffset = x;
		yOffset = y - getVertOffset();
		draw(-x, -y + getVertOffset(), root, width, height, c);

	}

	@Override
	public boolean onTouch(int x, int y) {
		onTouch(0, getVertOffset(), root, x, y);
		return false;
	}

	/**
	 * Recursive processing Touch events
	 * 
	 * @param baseX
	 *            basic x-offset (remove it)
	 * @param baseY
	 *            basic y-offset (remove it)
	 * @param itm
	 *            TopicView to check
	 * @param destX
	 *            destination x-coord
	 * @param destY
	 *            destination y-coord
	 * @return if captured
	 */
	private boolean onTouch(int baseX, int baseY, TopicView itm, int destX,
			int destY) {
		int yStart = itm.getTopicOffset() + baseY;
		int xStart = baseX;

		int localX = destX - xStart;
		int localY = destY - yStart;

		if (itm.isOverButton(localX, localY)) {

			changeChildVisibleStatus(itm);
			if (itm.isChildrenVisible()) {
				// TODO: sometime it falls
				focusTopic(itm.children[0]);
			} else {
				focusTopic(itm);
			}

			return true;
		} else if (itm.isOverTopic(localX, localY)) {
			itm.onTouch(localX, localY);
			focusTopic(itm);
		}

		if (itm.isChildrenVisible()) {
			int dataLen = itm.getTopicWidth();

			int vertOffset = 0;
			for (TopicView i : itm.children) {

				if (onTouch(baseX + dataLen, baseY + vertOffset, i, destX,
						destY))
					return true;

				vertOffset += i.getRenderZoneHeight();
			}
		}
		return false;
	}

	/*
	 * Some helper functions
	 */

	private final void centerRoot() {
		smoothScroll(root.getRenderZoneX(), root.getRenderZoneY()
				+ root.getTopicOffset() + root.getTopicHeight() / 2
				- renderZoneHeight / 2);
		selectTopic(root);
	}

	/**
	 * Selecting topic
	 * 
	 * @param topic
	 *            TopicView to select
	 */
	private final void selectTopic(TopicView topic) {
		if (selected != null)
			selected.setSelected(false);

		if (topic.parent != null)
			if (topic.parent.show())
				root.clearTree();
				
		topic.setSelected(true);
		selected = topic;
	}

	/**
	 * Focusing on topic
	 * 
	 * @param topic
	 *            TopicView to focus on
	 */
	private final void focusTopic(TopicView topic) {

		selectTopic(topic);

		int topicX = topic.getRenderZoneX();
		int topicY = topic.getTopicOffset() + topic.getRenderZoneY();

		int screenPosX = topicX - xOffset;
		int screenPosY = topicY - yOffset;

		int deltaX = xOffset;
		int deltaY = yOffset;

		if (screenPosX < 0)
			deltaX += screenPosX;
		else if (screenPosX + topic.getFocusWidth() > renderZoneWidth)
			deltaX += screenPosX + topic.getFocusWidth() - renderZoneWidth;

		if (screenPosY < 0)
			deltaY += screenPosY;
		else if (screenPosY +  topic.getFocusHeight() > renderZoneHeight)
			deltaY += screenPosY + topic.getFocusHeight() - renderZoneHeight;

		smoothScroll(deltaX, deltaY);

		// smoothScroll(topic.getRenderZoneX(), topic.getTopicOffset()
		// + topic.getRenderZoneY());
	}

	private int fixXOffset(int dx) {
		if (dx > this.getWidth() - renderZoneWidth)
			dx = this.getWidth() - renderZoneWidth;

		if (dx < 0)
			dx = 0;

		return dx;
	}

	private int fixYOffset(int dy) {
		if (dy > this.getHeight() - renderZoneHeight)
			dy = this.getHeight() - renderZoneHeight;

		if (dy < 0)
			dy = 0;

		return dy;
	}

	private void smoothScroll(int dx, int dy) {
		dx = fixXOffset(dx);
		dy = fixYOffset(dy);
		scrollController.smoothScroll(dx, dy);
	}

	private void sharpScroll(int dx, int dy) {
		dx = fixXOffset(dx);
		dy = fixYOffset(dy);
		scrollController.intermediateScroll(dx, dy);
	}

	private final void setChildrenVisible(TopicView topic, boolean isVisible) {
		int deltaY = topic.getRenderZoneY() + topic.getTopicOffset()
				+ getVertOffset() - yOffset;

		topic.setChildrenVisible(isVisible);

		root.clearTree();

		int destOffsetX = xOffset;
		int destOffsetY = topic.getTopicOffset() + topic.getRenderZoneY()
				- deltaY;

		if ((fixXOffset(destOffsetX) != destOffsetX)
				|| (fixXOffset(destOffsetY) != destOffsetY))
			smoothScroll(destOffsetX, destOffsetY);
		else
			sharpScroll(destOffsetX, destOffsetY);
	}

	/**
	 * Show/Hide chldren
	 * 
	 * @param topic
	 *            Parent TopicView
	 */
	private final void changeChildVisibleStatus(TopicView topic) {

		setChildrenVisible(topic, !topic.isChildrenVisible());
	}

	/*
	 * D-Pad functions.
	 */

	ArrayList<TopicView> searchResult = new ArrayList<TopicView>();

	void search(TopicView parent, Rect zone) {
		if (Rect.intersects(zone, parent.getTopicRectangle()))
			searchResult.add(parent);

		if (parent.isChildrenVisible())
			for (TopicView i : parent.children)
				search(i, zone);
	}

	int intersectLength(int a_start, int a_end, int b_start, int b_end) {

		/*
		 * b_start b_end |==================| ----------------------------
		 * |=========| a_start a_end
		 */

		if ((b_start <= a_start) && (a_end <= b_end)) {
			return a_end - a_start;
		}

		/*
		 * b_start b_end |===========| ----------------------------
		 * |====================| a_start a_end
		 */

		if ((a_start <= b_start) && (b_end <= a_end)) {
			return b_end - b_start;
		}

		/*
		 * b_start b_end |============| ---------------------------- |=========|
		 * a_start a_end
		 */
		if ((a_start <= b_start) && (a_end <= b_end)) {
			return a_end - b_start + 1;
		}

		/*
		 * b_start b_end |===============| -----------------------------
		 * |=========| a_start a_end
		 */

		if ((b_start <= a_start) && (b_end <= a_end)) {
			return b_end - a_start + 1;
		}

		return 0;
	}

	private static final int SEARCH_EPS = 5;

	TopicView searchUp(TopicView src) {
		searchResult.clear();
		Rect zone = src.getTopicRectangle();
		zone.bottom = zone.top;
		zone.top = 0;

		search(root, zone);

		if (searchResult.size() == 0)
			return null;
		else {
			TopicView res = null;
			int maxY = searchResult.get(0).getUnderlineOffset()
					+ searchResult.get(0).getRenderZoneY();
			for (TopicView i : searchResult) {
				int offset_i = i.getUnderlineOffset() + i.getRenderZoneY();

				if (maxY < offset_i)
					maxY = offset_i;

				if (res == null) {
					res = i;
				} else {
					int offset_res = res.getUnderlineOffset()
							+ res.getRenderZoneY();

					if (maxY - SEARCH_EPS < offset_i) {
						if (maxY - SEARCH_EPS < offset_res) {
							Rect r_i = i.getTopicRectangle();
							int iLen = intersectLength(r_i.left, r_i.right,
									zone.left, zone.right);

							Rect r_res = res.getTopicRectangle();
							int resLen = intersectLength(r_res.left,
									r_res.right, zone.left, zone.right);

							if (iLen > resLen)
								res = i;
						} else
							res = i;
					}

				}
			}
			return res;
		}
	}

	TopicView searchDown(TopicView src) {
		searchResult.clear();
		Rect zone = src.getTopicRectangle();
		zone.top = zone.bottom + 1;
		zone.bottom = this.getHeight();

		search(root, zone);

		if (searchResult.size() == 0)
			return null;
		else {
			TopicView res = null;
			int minY = searchResult.get(0).getUnderlineOffset()
					+ searchResult.get(0).getRenderZoneY();
			for (TopicView i : searchResult) {

				int offset_i = i.getUnderlineOffset() + i.getRenderZoneY();

				if (minY > offset_i)
					minY = offset_i;

				if (res == null) {
					res = i;
				} else {

					int offset_res = res.getUnderlineOffset()
							+ res.getRenderZoneY();

					if (minY + SEARCH_EPS > offset_i) {
						if (minY + SEARCH_EPS > offset_res) {
							Rect r_i = i.getTopicRectangle();
							int iLen = intersectLength(r_i.left, r_i.right,
									zone.left, zone.right);

							Rect r_res = res.getTopicRectangle();
							int resLen = intersectLength(r_res.left,
									r_res.right, zone.left, zone.right);

							if (iLen > resLen)
								res = i;
						} else
							res = i;
					}

					// if (res.getRenderZoneY() > i.getRenderZoneY())
					// res = i;
				}
			}
			return res;
		}
	}

	/**
	 * Moves selection up
	 */
	private final void moveUp() {

		if (selected.parent == null)
			return;

		// if (selected.up != null) {
		// if (selected.up.isVisible()) {
		// selected.up.down = selected;
		// focusTopic(selected.up);
		// return;
		// }
		// }

		int index = selected.getIndex();

		if (index == -1) {
			Log.e(DEBUG_TAG, "Danger! Seems to be broken tree!");
			return;
		}

		if (index > 0) // Is not highest child
		{
			selected.parent.children[index - 1].down = selected;
			focusTopic(selected.parent.children[index - 1]);
		} else {

			TopicView itm = searchUp(selected);
			if (itm != null) {
				itm.down = selected;
				focusTopic(itm);
			}
		}
	}

	/**
	 * Moves selection down
	 */
	private final void moveDown() {

		// if (selected.down != null) {
		// if (selected.down.isVisible()) {
		// selected.down.up = selected;
		// focusTopic(selected.down);
		// return;
		// }
		// }

		if (selected.parent == null)
			return;

		int index = selected.getIndex();
		TopicView[] parentChilds = selected.parent.children;

		if (index == -1) {
			Log.e(DEBUG_TAG, "Danger! Seems to be broken tree!");
			return;
		}

		if (index < parentChilds.length - 1) // Is not lowest child
		{
			parentChilds[index + 1].up = selected;
			focusTopic(parentChilds[index + 1]);
		} else {

			TopicView itm = searchDown(selected);
			if (itm != null) {
				itm.up = selected;
				focusTopic(itm);
			}
		}
	}

	/**
	 * Moves selection left
	 */
	private final void moveLeft() {

		if (selected.parent == null) {
			return;
		}

		if (selected.left != null) {
			selected.left.right = selected;
			focusTopic(selected.left);
			return;
		}

		selected.parent.right = selected;

		focusTopic(selected.parent);
	}

	/**
	 * Moves selection right
	 */
	private final void moveRight() {

		if (!selected.isChildrenVisible()) {
			this.setChildrenVisible(selected, true);
		}
		if (selected.right != null) {
			selected.right.left = selected;
			focusTopic(selected.right);
			return;
		}
		for (int i = 0; i < selected.children.length; i++) {
			if (selected.children[i].getRenderZoneY()
					+ selected.children[i].getTopicOffset() > selected
					.getRenderZoneY()
					+ selected.getTopicOffset()) {

				if (i == 0) {
					selected.children[0].left = selected;
					focusTopic(selected.children[0]);
				} else {
					selected.children[i - 1].left = selected;
					focusTopic(selected.children[i - 1]);
				}

				return;
			}
		}

		if (selected.children.length > 0) {
			selected.children[0].left = selected;
			focusTopic(selected.children[0]);
		}
	}

	@Override
	public void setScrollController(ScrollController scroll) {
		scrollController = scroll;
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

	@Override
	public void setBounds(int width, int height) {

	}

	@Override
	public void selectTopic(Topic topic) {
		for (TopicView i : items) {
			if (i.getTopic() == topic) {
				focusTopic(i);
				break;
			}
		}
	}

	@Override
	public boolean canRotate() {
		return true;
	}

	@Override
	public void onChangeSize() {

	}	
}