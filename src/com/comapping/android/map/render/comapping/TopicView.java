package com.comapping.android.map.render.comapping;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.comapping.android.map.model.map.Topic;
import com.comapping.android.map.render.PlusMinusRender;
import com.comapping.android.map.render.topic.TopicRender;

/**
 * Container for Topic's whith some helpful functions for drawing, calculating
 * positions, working with childs
 * 
 * @author Korshakov Stepan
 * 
 */
class TopicView {

	private static final int HORIZONTAL_BORDER_SIZE = 10;
	private static final int LINE_COLOR = Color.GRAY;
	private static final int TEXT_WIDTH = 300;

	/* ------- Navigation data ------- */
	public TopicView up = null;
	public TopicView down = null;
	public TopicView left = null;
	public TopicView right = null;

	/* ---------- Tree data ---------- */

	public TopicView[] children;
	public TopicView parent = null;

	/* ----- Comapping tree data ----- */
	public Topic topicData;

	/* --------- TopicView state ---------- */
	private boolean childrenVisible = true;

	/* ------ Rendering helpers ------ */
	public TopicRender topicRender;
	public PlusMinusRender plusMinusRender;

	private Paint p = new Paint();

	/* ---------- Lazy buffers ---------- */

	private int lazyOffset = -1;
	private int lazyTreeWidth = -1;
	private int lazyRenderZoneHeight = -1;
	private int lazyAbsoluteX = -1;
	private int lazyAbsoluteY = -1;

	private boolean visible = true;

	/**
	 * Constructor of Render TopicView
	 * 
	 * @param topic
	 *            Topic for container
	 */
	public TopicView(Topic topic, Context context) {
		topicData = topic;
		topicRender = new TopicRender(topicData, context);
		topicRender.setMaxWidth(TEXT_WIDTH);
		plusMinusRender = new PlusMinusRender(!childrenVisible);

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

		if (childrenVisible == isVisible)
			return;

		childrenVisible = isVisible;
		plusMinusRender.isPlus = !childrenVisible;
		setVisible(isVisible);
		clearLazyBuffers();
	}

	/**
	 * Returns if children is visible
	 * 
	 * @return Visible state
	 */
	public boolean isChildrenVisible() {
		return childrenVisible;
	}

	public boolean isVisible() {
		return visible;
	}

	public boolean show() {
		if (childrenVisible)
			return false;
		
		setChildrenVisible(true);
		if (parent != null)
			parent.show();
		
		return true;
	}

	private void setVisible(boolean _visible) {
		visible = _visible;
		for (TopicView i : children)
			i.setVisible(_visible);
	}

	public void setSelected(boolean isSelected) {
		topicRender.setSelected(isSelected);
	}

	public Topic getTopic() {
		return topicData;
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
		topicRender.draw(x, y + vertOffset, topicRender.getWidth(), topicRender
				.getHeight(), c);

		// Drawing lines
		c.drawLine(x, y + getUnderlineOffset(), x + topicRender.getWidth(), y
				+ getUnderlineOffset(), p);

		if (topicData.getChildrenCount() != 0) {
			// Draw +/- circle

			plusMinusRender.draw(x + topicRender.getWidth(), y
					+ getUnderlineOffset() - plusMinusRender.getWidth() / 2,
					plusMinusRender.getWidth(), plusMinusRender.getHeight(), c);

		}
	}

	/* ---------- Input code ---------- */

	/**
	 * Checks if point is over button
	 * 
	 * @param x
	 *            x-coord of point in TopicView coordinate system
	 * @param y
	 *            y-coord of point in TopicView coordinate system
	 * @return if point is over button
	 */
	public boolean isOverButton(int x, int y) {
		return ((x >= topicRender.getWidth())
				&& (y >= topicRender.getLineOffset()
						- plusMinusRender.getWidth() / 2)
				&& (x <= topicRender.getWidth() + plusMinusRender.getWidth()) && (y <= topicRender
				.getLineOffset()
				+ plusMinusRender.getWidth() / 2));
	}

	/**
	 * Checks if point is over topic
	 * 
	 * @param x
	 *            x-coord of point in TopicView coordinate system
	 * @param y
	 *            y-coord of point in TopicView coordinate system
	 * @return if point is over topic
	 */
	public boolean isOverTopic(int x, int y) {
		return (!isOverButton(x, y))
				&& ((x >= 0) && (y >= 0) && (x <= topicRender.getWidth()) && (y <= topicRender
						.getHeight()));
	}

	/* -- Topic sizes and positions code -- */

	/**
	 * Return width of topic (without children)
	 * 
	 * @return Width of a rendering topic
	 */
	public int getTopicWidth() {
		if (this.children.length > 0)
			return topicRender.getWidth() + plusMinusRender.getWidth();
		else
			return topicRender.getWidth();
	}

	/**
	 * Return height of topic (without children)
	 * 
	 * @return Height of a rendering topic
	 */
	public int getTopicHeight() {
		if (this.children.length > 0) {
			if (topicRender.getHeight() - topicRender.getLineOffset() < plusMinusRender
					.getHeight() / 2)
				return topicRender.getLineOffset()
						+ plusMinusRender.getHeight() / 2;
			else
				return topicRender.getHeight() + HORIZONTAL_BORDER_SIZE;
		} else
			return topicRender.getHeight() + HORIZONTAL_BORDER_SIZE;
	}

	/**
	 * Returns vertical offset from (0,0) (in TopicView coord system) to draw
	 * topic centered in topicRender zone
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
	 * Returns offset for underline in topicRender zone coord system
	 * 
	 * @return offset to line from (0,0) of topicRender zone
	 */
	public int getUnderlineOffset() {
		return getTopicOffset() + topicRender.getLineOffset();
	}

	/* ---------- Tree and zone sizes code ---------- */

	/**
	 * Returns topicRender zone width
	 * 
	 * @return Width of the zone
	 */
	public int getRenderZoneWidth() {
		return this.getTopicWidth();
	}

	/**
	 * Returns topicRender zone height
	 * 
	 * @return Height of the zone
	 */
	public int getRenderZoneHeight() {
		if (lazyRenderZoneHeight == -1) {
			int w = 0;
			if (childrenVisible)
				for (TopicView i : children) {
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
				for (TopicView i : children) {
					w = Math.max(i.getTreeWidth(), w);
				}
			lazyTreeWidth = this.getRenderZoneWidth() + w;
		}
		return lazyTreeWidth;
	}

	/* ---------- Global positions code ---------- */

	/**
	 * Calculates absolute positions of TopicView
	 */
	private void calcRenderZonePositions() {

		// TODO: Must to do it in parent TopicView. Work time - O(n^2). Must be
		// O(n).

		if (parent == null) {
			lazyAbsoluteX = 0;
			lazyAbsoluteY = 0;
			return;
		}

		int baseX = this.parent.getRenderZoneX();
		int baseY = this.parent.getRenderZoneY();
		int dataLen = this.parent.getRenderZoneWidth();
		int vertOffset = 0;
		for (TopicView i : this.parent.children) {

			if (i == this) {
				lazyAbsoluteX = baseX + dataLen;
				lazyAbsoluteY = baseY + vertOffset;
				return;
			}

			vertOffset += i.getRenderZoneHeight();
		}
	}

	/**
	 * Calculates absolute X-coordinate of topicRender zone
	 * 
	 * @return X-coord for topicRender zone
	 */
	public int getRenderZoneX() {
		if (lazyAbsoluteX == -1) {
			calcRenderZonePositions();
		}
		return lazyAbsoluteX;
	}

	/**
	 * Calculates absolute Y-coordinate of topicRender zone
	 * 
	 * @return Y-coord for topicRender zone
	 */
	public int getRenderZoneY() {
		if (lazyAbsoluteY == -1) {
			calcRenderZonePositions();
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

	public void clearTree() {
		lazyAbsoluteY = -1;
		lazyAbsoluteX = -1;

		lazyOffset = -1;
		lazyRenderZoneHeight = -1;
		lazyTreeWidth = -1;
		for (int i = 0; i < children.length; i++)
			children[i].clearTree();
	}

	/* ---------- Misc code ---------- */

	/**
	 * Returns index in parent.children array of this TopicView
	 * 
	 * @return index in parent.children array
	 */
	public int getIndex() {
		if (parent == null)
			return -1;

		// May be I should add buffering?
		int index = -1;
		TopicView[] parentChildren = parent.children;
		for (int i = 0; i < parentChildren.length; i++) {
			if (parentChildren[i] == this) {
				index = i;
				break;
			}
		}
		return index;
	}

	public void onTouch(int x, int y) {
		topicRender.onTouch(x, y);
	}

	public Rect getTopicRectangle() {
		Rect res = new Rect();
		int topicAbsX = this.getRenderZoneX();
		int topicAbsY = this.getRenderZoneY() + this.getTopicOffset();

		res.set(topicAbsX, topicAbsY, topicAbsX + topicRender.getWidth(),
				topicAbsY + topicRender.getHeight());

		return res;
	}
	
	
	public int getFocusWidth()
	{
		return topicRender.getWidth();
	}
	
	public int getFocusHeight()
	{
		return topicRender.getHeight();
	}
}
