package com.comapping.android.view;

import java.util.ArrayList;
import java.util.HashMap;

import com.comapping.android.model.Map;
import com.comapping.android.model.Topic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class ExplorerRender extends Render {

	private class touchPoint {
		public int x, y, id;

		public touchPoint(int x, int y, int id) {
			this.x = x;
			this.y = y;
			this.id = id;
		}
	}

	private static final int X_SHIFT = 30;
	private static final int Y_SHIFT = 15;
	private static final int OUTER_SIZE = 15;
	private static final int CIRCLE_WIDTH = 2;
	private static final int PLUS_LENGTH = 7;
	private static final int PLUS_WIDTH = 2;
	private static final int BLOCK_SHIFT = 5;

	private Map map;
	private HashMap<Integer, Boolean> open = new HashMap<Integer, Boolean>();
	private HashMap<Integer, TopicRender> topicRenders = new HashMap<Integer, TopicRender>();

	private boolean toUpdate = true;
	private ArrayList<touchPoint> points = new ArrayList<touchPoint>();
	private int xPlus, yPlus;
	private int height, width;
	private int screenWidth, screenHeight;

	public ExplorerRender(Context context, Map map) {
		this.map = map;
	}

	private boolean intersects(int a, int b, int c, int d) {
		if (b < c)
			return false;
		if (d < a)
			return false;
		return true;
	}

	private boolean onScreen(int x1, int y1, int x2, int y2) {
		return intersects(0, screenWidth, x1, x2)
				&& intersects(0, screenHeight, y1, y2);
	}

	private int[] drawTopic(Topic topic, int x, int y, Canvas c) {
		if (topic == null) {
			return new int[3];
		}

		if (topicRenders.get(topic.getId()) == null)
			topicRenders.put(topic.getId(), new TopicRender(topic));
		TopicRender topicRender = topicRenders.get(topic.getId());
		int height = topicRender.getHeight();
		if (topic.getChildrenCount() > 0)
			height = Math.max(height, OUTER_SIZE * 2);
		int[] ret = new int[3];
		ret[0] = topicRender.getWidth() + BLOCK_SHIFT;
		ret[1] = y;
		ret[2] = height / 2;

		x += OUTER_SIZE;
		y += (height - topicRender.getHeight()) / 2;
		x += X_SHIFT + BLOCK_SHIFT;
		if (onScreen(x, y, x + topicRender.getWidth(), y
				+ topicRender.getHeight()))
			topicRender.draw(x, y, 0, 0, c);
		x -= X_SHIFT + BLOCK_SHIFT;

		// draw circle and line
		y = ret[1] + ret[2];
		Paint p = new Paint();
		p.setColor(Color.GRAY);
		if (onScreen(x, y, x + X_SHIFT, y))
			c.drawLine(x, y, x + X_SHIFT, y, p);
		if (topic.getChildrenCount() > 0) {
			if (open.get(topic.getId()) == null)
				open.put(topic.getId(), true);
			if (toUpdate)
				points.add(new touchPoint(x - xPlus, y - yPlus, topic.getId()));
			if (onScreen(x - OUTER_SIZE, y - OUTER_SIZE, x + OUTER_SIZE, y
					+ OUTER_SIZE)) {
				c.drawCircle(x, y, OUTER_SIZE, p);
				p.setColor(Color.WHITE);
				c.drawCircle(x, y, OUTER_SIZE - CIRCLE_WIDTH, p);
				p.setColor(Color.GRAY);
				c.drawRect(x - PLUS_LENGTH, y - PLUS_WIDTH, x + PLUS_LENGTH, y
						+ PLUS_WIDTH, p);
				if (!open.get(topic.getId()))
					c.drawRect(x - PLUS_WIDTH, y - PLUS_LENGTH, x + PLUS_WIDTH,
							y + PLUS_LENGTH, p);
			}
		}

		// draw subtopics
		x += X_SHIFT;
		y += ret[2];
		if (topic.getChildrenCount() > 0 && open.get(topic.getId())) {
			int py = y - ret[2];
			p.setColor(Color.GRAY);
			int[] temp;
			if (y <= screenHeight) {
				for (int i = 0; i < topic.getChildrenCount(); i++) {
					y += Y_SHIFT;
					temp = drawTopic(topic.getChildByIndex(i), x - OUTER_SIZE,
							y, c);

					int ny = y + temp[2];
					if (topic.getChildByIndex(i).getChildrenCount() > 0)
						ny -= OUTER_SIZE;
					if (onScreen(x, py, x, ny))
						c.drawLine(x, py, x, ny, p);
					py = y + temp[2];
					if (topic.getChildByIndex(i).getChildrenCount() > 0)
						py += OUTER_SIZE;

					ret[0] = Math.max(ret[0], temp[0] - OUTER_SIZE);
					y += temp[1];
				}
			}
		}

		ret[0] += X_SHIFT + OUTER_SIZE;
		ret[1] = y - ret[1];

		return ret;
	}

	@Override
	public void draw(int x, int y, int width, int height, Canvas c) {
		x = -x;
		y = -y;
		xPlus = x;
		yPlus = y;
		screenWidth = width;
		screenHeight = height;
		if (toUpdate)
			points.clear();
		int[] temp = drawTopic(map.getRoot(), x, y, c);
		this.width = temp[0];
		this.height = temp[1];
		toUpdate = false;
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
		for (touchPoint point : points) {
			if (Math.hypot(point.x + xPlus - x, point.y + yPlus - y) <= OUTER_SIZE) {
				open.put(point.id, !open.get(point.id));
				toUpdate = true;
				break;
			}
		}
	}

}
