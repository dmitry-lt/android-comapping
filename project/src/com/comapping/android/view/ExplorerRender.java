package com.comapping.android.view;

import java.util.ArrayList;

import com.comapping.android.model.Map;
import com.comapping.android.model.Topic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.KeyEvent;

public class ExplorerRender extends MapRender {

	// �������� �� ����� ��������
	private class TouchPoint {
		public int x, y;
		public MyTopic topic;

		public TouchPoint(int x, int y, MyTopic topic) {
			this.x = x;
			this.y = y;
			this.topic = topic;
		}
	}

	// �������� �� ����� ��������
	private class MyTopic {
		public Topic topic;
		public boolean open; // isOpen better
		public TopicRender topicRender;
		public int topicX, topicY;
		public ArrayList<MyTopic> childs; // children
		public int x1, y1, x2, y2; // unintelligible(hard to understand)
		public MyTopic up, down, left, right; // unintelligible(hard to understand)
	}

	private static final int X_SHIFT = 30;
	private static final int Y_SHIFT = 15;
	private static final int OUTER_SIZE = 15; // of what?
	private static final int CIRCLE_WIDTH = 2;
	private static final int PLUS_LENGTH = 7;
	private static final int PLUS_WIDTH = 2;
	private static final int BLOCK_SHIFT = 5;

	private MyTopic root;
	private MyTopic selectedTopic;
	private ScrollController scroll;
	private boolean toUpdate = true;
	private boolean toFocusRoot = true;

	private Context context;

	private ArrayList<TouchPoint> points = new ArrayList<TouchPoint>();
	private ArrayList<Rect> lines = new ArrayList<Rect>(); // line is rect?
	private ArrayList<MyTopic> topics = new ArrayList<MyTopic>();

	private int xPlus, yPlus;
	private int height, width;
	private int screenWidth, screenHeight;

	public ExplorerRender(Context context, Map map) {
		this.context = context;
		root = initTopic(map.getRoot(), null);
		selectedTopic = null;
	}

	// Is two segment intersects
	private boolean intersects(int a, int b, int c, int d) {
		if (b < c) {
			return false;
		}
		if (d < a) {
			return false;
		}
		return true;
	}

	// Is rectangle lies on screen
	private boolean onScreen(int x1, int y1, int x2, int y2) {
		return intersects(0, screenWidth, x1, x2)
				&& intersects(0, screenHeight, y1, y2);
	}

	// Method builds initially MyTopics tree
	private MyTopic initTopic(Topic topic, MyTopic parent) {
		MyTopic t = new MyTopic();
		t.topic = topic;
		t.open = true;
		t.topicRender = new TopicRender(topic, context);
		t.childs = new ArrayList<MyTopic>();
		for (int i = 0; i < topic.getChildrenCount(); i++) {
			t.childs.add(initTopic(topic.getChildByIndex(i), t));
		}
		t.left = parent;
		if (topic.getChildrenCount() > 0) {
			t.right = t.childs.get(0);
		} else {
			t.right = null;
		}
		return t;
	}

	// Draw tree
	// We can use binary search on Y-coordinate of topics
	// and circles
	private void draw(Canvas c) {
		Paint p = new Paint();
		int lo, hi;

		// draw lines
		lo = -1;
		hi = lines.size() - 1;
		while (lo < hi) {
			int mid = (lo + hi + 1) / 2;
			if (lines.get(mid).bottom + yPlus < 0) {
				lo = mid;
			} else {
				hi = mid - 1;
			}
		}
		lo++;
		p.setColor(Color.GRAY);

		// TODO Sort lines

		for (int i = 0; i < lines.size(); i++) {
			Rect line = lines.get(i);
			int x1 = line.left + xPlus;
			int y1 = line.top + yPlus;
			int x2 = line.right + xPlus;
			int y2 = line.bottom + yPlus;
			// if (y1 > screenHeight) {
			// break;
			// }
			if (onScreen(x1, y1, x2, y2)) {
				c.drawLine(x1, y1, x2, y2, p);
			}
		}

		// draw circles
		lo = -1;
		hi = points.size() - 1;
		while (lo < hi) {
			int mid = (lo + hi + 1) / 2;
			if (points.get(mid).y + OUTER_SIZE + yPlus < 0) {
				lo = mid;
			} else {
				hi = mid - 1;
			}
		}
		lo++;
		p.setAntiAlias(true);
		for (int i = lo; i < points.size(); i++) {
			TouchPoint point = points.get(i);
			int x = point.x + xPlus;
			int y = point.y + yPlus;
			if (y - OUTER_SIZE > screenHeight) {
				break;
			}
			if (onScreen(x - OUTER_SIZE, y - OUTER_SIZE, x + OUTER_SIZE, y
					+ OUTER_SIZE)) {
				c.drawCircle(x, y, OUTER_SIZE, p);
				p.setColor(Color.WHITE);
				c.drawCircle(x, y, OUTER_SIZE - CIRCLE_WIDTH, p);
				p.setColor(Color.GRAY);
				c.drawRect(x - PLUS_LENGTH, y - PLUS_WIDTH, x + PLUS_LENGTH, y
						+ PLUS_WIDTH, p);
				if (!point.topic.open)
					c.drawRect(x - PLUS_WIDTH, y - PLUS_LENGTH, x + PLUS_WIDTH,
							y + PLUS_LENGTH, p);
			}
		}
		p.setAntiAlias(false);

		// draw topics
		lo = -1;
		hi = topics.size() - 1;
		while (lo < hi) {
			int mid = (lo + hi + 1) / 2;
			if (topics.get(mid).topicY
					+ topics.get(mid).topicRender.getHeight() + yPlus < 0) {
				lo = mid;
			} else {
				hi = mid - 1;
			}
		}
		lo++;
		for (int i = lo; i < topics.size(); i++) {
			MyTopic topic = topics.get(i);
			int x = topic.topicX + xPlus;
			int y = topic.topicY + yPlus;
			if (y > screenHeight) {
				break;
			}
			TopicRender topicRender = topic.topicRender;
			if (onScreen(x, y, x + topicRender.getWidth(), y
					+ topicRender.getHeight())) {
				topicRender.draw(x, y, 0, 0, c);
			}
		}

	}

	// Set selection and move scroll if it needed
	private void focusTopic(MyTopic topic) {
		if (topic == null) {
			return;
		}
		if (selectedTopic != null) {
			selectedTopic.topicRender.setSelected(false);
		}
		topic.topicRender.setSelected(true);
		selectedTopic = topic;
		int x1 = topic.x1 + xPlus;
		int y1 = topic.y1 + yPlus;
		int x2 = topic.x2 + xPlus;
		int y2 = topic.y2 + yPlus;
		int nx = -xPlus, ny = -yPlus;
		if (x2 > screenWidth) {
			nx -= screenWidth - x2;
		}
		if (y2 > screenHeight) {
			ny -= screenHeight - y2;
		}
		if (x1 < 0) {
			nx = topic.x1;
		}
		if (y1 < 0) {
			ny = topic.y1;
		}
		scroll.smoothScroll(nx, ny);
	}

	// Method to calculate coordinates of topics, circles
	// and lines according screen size and collapsed topics
	private int[] updateTopic(MyTopic topic, int x, int y) { // there is a Point class
		// calculate sizes
		topic.x1 = x;
		topic.y1 = y;
		TopicRender topicRender = topic.topicRender;
		topicRender.setMaxWidth(screenWidth - OUTER_SIZE - X_SHIFT
				- BLOCK_SHIFT);
		int height = topicRender.getHeight();
		if (topic.childs.size() > 0) {
			height = Math.max(height, OUTER_SIZE * 2);
		}
		int[] ret = new int[3];
		ret[0] = topicRender.getWidth() + BLOCK_SHIFT;
		ret[1] = y;
		ret[2] = height / 2;

		// update topic
		x += OUTER_SIZE;
		y += (height - topicRender.getHeight()) / 2;
		x += X_SHIFT + BLOCK_SHIFT;
		topic.topicX = x;
		topic.topicY = y;
		topic.x2 = x + topicRender.getWidth();
		x -= X_SHIFT + BLOCK_SHIFT;
		topics.add(topic);

		// update circle and line
		y = ret[1] + ret[2];
		topic.y2 = y + y - topic.y1;
		lines.add(new Rect(x, y, x + X_SHIFT, y));
		if (topic.childs.size() > 0) {
			points.add(new TouchPoint(x, y, topic));
		}

		// update subtopics
		x += X_SHIFT;
		y += ret[2];
		if (topic.childs.size() > 0 && topic.open) {
			int py = y - ret[2];
			int[] temp;
			for (int i = 0; i < topic.childs.size(); i++) {
				y += Y_SHIFT;
				temp = updateTopic(topic.childs.get(i), x - OUTER_SIZE, y);

				int ny = y + temp[2];
				if (topic.topic.getChildByIndex(i).getChildrenCount() > 0)
					ny -= OUTER_SIZE;
				lines.add(new Rect(x, py, x, ny));
				py = y + temp[2];
				if (topic.childs.get(i).childs.size() > 0)
					py += OUTER_SIZE;

				ret[0] = Math.max(ret[0], temp[0] - OUTER_SIZE);
				y += temp[1];
			}
		}

		ret[0] += X_SHIFT + OUTER_SIZE;
		ret[1] = y - ret[1];

		return ret;
	}

	// Public methods

	// Method to update tree, sizes and references for
	// key pressing
	@Override
	public void update() { // why it's method of abstract class MapRender
		points.clear();
		lines.clear();
		topics.clear();
		int[] temp = updateTopic(root, 0, 0);
		this.width = temp[0];
		this.height = temp[1];
		toUpdate = false;
		for (int i = 0; i < topics.size(); i++) {
			if (i > 0)
				topics.get(i).up = topics.get(i - 1);
			else
				topics.get(i).up = null;
			if (i + 1 < topics.size())
				topics.get(i).down = topics.get(i + 1);
			else
				topics.get(i).down = null;
		}
		if (toFocusRoot) {
			focusTopic(root);
			toFocusRoot = false;
		}
	}

	@Override
	public void draw(int x, int y, int width, int height, Canvas c) {
		x = -x; // magic?
		y = -y;
		xPlus = x;
		yPlus = y;
		screenWidth = width;
		screenHeight = height;
		if (toUpdate) {
			update();
		}
		draw(c);
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
		// touch circles
		for (TouchPoint point : points) {
			if (Math.hypot(point.x - x, point.y - y) <= OUTER_SIZE) {
				point.topic.open = !point.topic.open;
				focusTopic(point.topic);
				toUpdate = true;
				break;
			}
		}

		// touch topics
		for (MyTopic topic : topics) {
			int x1 = topic.topicX;
			int y1 = topic.topicY;
			int x2 = x1 + topic.topicRender.getWidth();
			int y2 = y1 + topic.topicRender.getHeight();
			if (x1 <= x && x <= x2 && y1 <= y && y <= y2) {
				topic.topicRender.onTouch(x - x1, y - y1);
				focusTopic(topic);
				break;
			}
		}

	}

	@Override
	public void setScrollController(ScrollController scroll) {
		this.scroll = scroll;
	}

	@Override
	public void onKeyDown(int keyCode) {
		if (selectedTopic == null) {
			return;
		}
		if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
			focusTopic(selectedTopic.up);
		}
		if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			focusTopic(selectedTopic.down);
		}
		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			focusTopic(selectedTopic.left);
		}
		if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			if (selectedTopic.childs.size() > 0 && !selectedTopic.open) {
				selectedTopic.open = true;
				update();
			}
			focusTopic(selectedTopic.right);
		}
		if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
			if (selectedTopic.childs.size() > 0) {
				selectedTopic.open = !selectedTopic.open;
				update();
			}
		}
	}
}
