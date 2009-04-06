package com.comapping.android.view;

import java.util.ArrayList;

import com.comapping.android.model.Map;
import com.comapping.android.model.Topic;
import com.comapping.android.Log;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class ExplorerRender extends Render {

	private class TouchPoint {
		public int x, y, index;

		public TouchPoint(int x, int y, int index) {
			this.x = x;
			this.y = y;
			this.index = index;
		}
	}

	private static final int X_SHIFT = 30;
	private static final int Y_SHIFT = 15;
	private static final int OUTER_SIZE = 15;
	private static final int CIRCLE_WIDTH = 2;
	private static final int PLUS_LENGTH = 7;
	private static final int PLUS_WIDTH = 2;
	private static final int BLOCK_SHIFT = 5;
	private static final int FAKE_X = 100000000;

	private Map map;
	private boolean toUpdate = true;

	private int size;
	private ArrayList<Boolean> open = new ArrayList<Boolean>();
	private ArrayList<TopicRender> topicRenders = new ArrayList<TopicRender>();
	private ArrayList<Integer> topicX = new ArrayList<Integer>();
	private ArrayList<Integer> topicY = new ArrayList<Integer>();
	private ArrayList<ArrayList<Integer>> childs = new ArrayList<ArrayList<Integer>>();

	private ArrayList<TouchPoint> points = new ArrayList<TouchPoint>();
	private ArrayList<Rect> lines = new ArrayList<Rect>();

	private int xPlus, yPlus;
	private int height, width;
	private int screenWidth, screenHeight;

	public ExplorerRender(Context context, Map map) {
		this.map = map;
		initTopic(map.getRoot());
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

	private int initTopic(Topic topic) {
		open.add(true);
		topicRenders.add(new TopicRender(topic));
		topicX.add(0);
		topicY.add(0);
		childs.add(new ArrayList<Integer>());
		int cur = size++;
		ArrayList<Integer> temp = new ArrayList<Integer>();
		for (int i = 0; i < topic.getChildrenCount(); i++)
			temp.add(initTopic(topic.getChildByIndex(i)));
		childs.set(cur, temp);
		return cur;
	}

	private void drawTopics(Canvas c) {
		Paint p = new Paint();

		// draw lines
		p.setColor(Color.GRAY);
		for (Rect line : lines) {
			int x1 = line.left + xPlus;
			int y1 = line.top + yPlus;
			int x2 = line.right + xPlus;
			int y2 = line.bottom + yPlus;
			if (onScreen(x1, y1, x2, y2)) {
				c.drawLine(x1, y1, x2, y2, p);
			}
		}

		// draw circles
		for (TouchPoint point : points) {
			int x = point.x + xPlus;
			int y = point.y + yPlus;
			int index = point.index;
			if (onScreen(x - OUTER_SIZE, y - OUTER_SIZE, x + OUTER_SIZE, y
					+ OUTER_SIZE)) {
				c.drawCircle(x, y, OUTER_SIZE, p);
				p.setColor(Color.WHITE);
				c.drawCircle(x, y, OUTER_SIZE - CIRCLE_WIDTH, p);
				p.setColor(Color.GRAY);
				c.drawRect(x - PLUS_LENGTH, y - PLUS_WIDTH, x + PLUS_LENGTH, y
						+ PLUS_WIDTH, p);
				if (!open.get(index))
					c.drawRect(x - PLUS_WIDTH, y - PLUS_LENGTH, x + PLUS_WIDTH,
							y + PLUS_LENGTH, p);
			}
		}

		// draw topics
		for (int i = 0; i < size; i++) {
			int x = topicX.get(i) + xPlus;
			if (x == FAKE_X + xPlus)
				continue;
			int y = topicY.get(i) + yPlus;
			TopicRender topicRender = topicRenders.get(i);
			if (onScreen(x, y, x + topicRender.getWidth(), y
					+ topicRender.getHeight()))
				topicRender.draw(x, y, 0, 0, c);
		}

	}

	private int[] updateTopic(Topic topic, int index, int x, int y) {
		TopicRender topicRender = topicRenders.get(index);
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
		topicX.set(index, x);
		topicY.set(index, y);
		x -= X_SHIFT + BLOCK_SHIFT;

		// update circle and line
		y = ret[1] + ret[2];
		lines.add(new Rect(x, y, x + X_SHIFT, y));
		if (topic.getChildrenCount() > 0) {
			points.add(new TouchPoint(x, y, index));
		}

		// update subtopics
		x += X_SHIFT;
		y += ret[2];
		if (topic.getChildrenCount() > 0 && open.get(index)) {
			int py = y - ret[2];
			int[] temp;
			for (int i = 0; i < topic.getChildrenCount(); i++) {
				y += Y_SHIFT;
				temp = updateTopic(topic.getChildByIndex(i), childs.get(index)
						.get(i), x - OUTER_SIZE, y);

				int ny = y + temp[2];
				if (topic.getChildByIndex(i).getChildrenCount() > 0)
					ny -= OUTER_SIZE;
				lines.add(new Rect(x, py, x, ny));
				py = y + temp[2];
				if (topic.getChildByIndex(i).getChildrenCount() > 0)
					py += OUTER_SIZE;

				ret[0] = Math.max(ret[0], temp[0] - OUTER_SIZE);
				y += temp[1];
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
		if (toUpdate) {
			points.clear();
			lines.clear();
			for (int i = 0; i < size; i++)
				topicX.set(i, FAKE_X);
			int[] temp = updateTopic(map.getRoot(), 0, 0, 0);
			this.width = temp[0];
			this.height = temp[1];
			toUpdate = false;
		}
		drawTopics(c);
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
		for (TouchPoint point : points) {
			Log.d(Log.explorerRenderTag, "Point : " + point.x + " " + point.y
					+ " " + point.index);
			if (Math.hypot(point.x + xPlus - x, point.y + yPlus - y) <= OUTER_SIZE) {
				int index = point.index;
				Log.d(Log.explorerRenderTag, "Touch : " + index);
				open.set(index, !open.get(index));
				toUpdate = true;
			}
		}
	}

}
