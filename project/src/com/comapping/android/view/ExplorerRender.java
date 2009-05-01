package com.comapping.android.view;

import java.util.ArrayList;

import com.comapping.android.model.Map;
import com.comapping.android.model.Topic;
import com.comapping.android.view.topic.TopicRender;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.KeyEvent;

public class ExplorerRender extends MapRender {

	private class Expander implements Comparable<Expander> {
		public int x, y;
		public TopicView topic;

		public Expander(int x, int y, TopicView topic) {
			this.x = x;
			this.y = y;
			this.topic = topic;
		}

		@Override
		public int compareTo(Expander another) {
			if (this.y < another.y) {
				return -1;
			} else if (this.y == another.y) {
				return 0;
			} else {
				return 1;
			}
		}
	}

	private class TopicView implements Comparable<TopicView> {
		public boolean isOpen;
		public TopicRender topicRender;
		public int topicRenderX, topicRenderY;
		public ArrayList<TopicView> children;
		public int x1, y1, x2, y2;
		public TopicView parent, nextSibling = null, prevSibling = null;

		public TopicView(Topic topic, TopicView parent) {
			isOpen = true;
			topicRender = new TopicRender(topic, context);
			children = new ArrayList<TopicView>();
			for (int i = 0; i < topic.getChildrenCount(); i++) {
				children.add(new TopicView(topic.getChildByIndex(i), this));
			}
			this.parent = parent;
			for (int i = 0; i < topic.getChildrenCount(); i++) {
				if (i - 1 >= 0) {
					children.get(i).prevSibling = children.get(i - 1);
				}
				if (i + 1 < topic.getChildrenCount()) {
					children.get(i).nextSibling = children.get(i + 1);
				}
			}
		}

		public TopicView getNextTopic() {
			if (children.size() > 0 && isOpen) {
				return children.get(0);
			}
			TopicView topic = this;
			while (true) {
				if (topic.nextSibling != null) {
					return topic.nextSibling;
				}
				if (topic.parent != null) {
					topic = topic.parent;
				} else {
					break;
				}
			}
			return null;
		}

		private TopicView getLastTopic() {
			if (children.size() > 0 && isOpen) {
				return children.get(children.size() - 1).getLastTopic();
			}
			return this;
		}

		public TopicView getPrevTopic() {
			if (prevSibling != null) {
				return prevSibling.getLastTopic();
			}
			return parent;
		}

		public TopicView(int y) {
			topicRenderY = y;
		}

		@Override
		public int compareTo(TopicView another) {
			int y1 = this.topicRenderY + this.topicRender.getHeight();
			int y2 = another.topicRenderY;
			if (y1 < y2) {
				return -1;
			} else if (y1 == y2) {
				return 0;
			} else {
				return 1;
			}
		}
	}

	private static final int X_SHIFT = 30;
	private static final int Y_SHIFT = 15;
	private static final int BLOCK_SHIFT = 5;

	private TopicView root;
	private TopicView selectedTopic;
	private ScrollController scroll;
	private PlusMinusRender plusMinusRender = new PlusMinusRender(true);
	private boolean selectRootNeeded = true;
	private boolean expandingNeeded = true;
	private boolean setBoundsNeeded = true;

	private Context context;

	private ArrayList<Expander> expanders = new ArrayList<Expander>();
	private ArrayList<Rect> lines = new ArrayList<Rect>();
	private ArrayList<TopicView> topics = new ArrayList<TopicView>();

	private int radius = plusMinusRender.getHeight() / 2;
	private int xOffset, yOffset;
	private int height, width;
	private int screenWidth, screenHeight;

	public ExplorerRender(Context context, Map map) {
		this.context = context;
		root = new TopicView(map.getRoot(), null);
		selectedTopic = null;
	}

	// Is two segment intersects
	private boolean isSegmentsIntersects(int a1, int a2, int b1, int b2) {
		if (a2 < b1) {
			return false;
		}
		if (b2 < a1) {
			return false;
		}
		return true;
	}

	// Is rectangle lies on screen
	private boolean onScreen(int x1, int y1, int x2, int y2) {
		return isSegmentsIntersects(0, screenWidth, x1, x2)
				&& isSegmentsIntersects(0, screenHeight, y1, y2);
	}

	private <T> int getFirstOccurence(ArrayList<? extends Comparable<T>> a, T o) {
		int lo = -1;
		int hi = a.size() - 1;
		while (lo < hi) {
			int mid = (lo + hi + 1) / 2;
			if (a.get(mid).compareTo(o) < 0) {
				lo = mid;
			} else {
				hi = mid - 1;
			}
		}
		lo++;
		return lo;
	}

	// Draw tree
	// We can use binary search on Y-coordinate of topics
	// and circles
	private void draw(Canvas c) {
		Paint p = new Paint();
		p.setColor(Color.GRAY);

		// draw lines
		for (Rect line : lines) {
			int x1 = line.left + xOffset;
			int y1 = line.top + yOffset;
			int x2 = line.right + xOffset;
			int y2 = line.bottom + yOffset;
			if (onScreen(x1, y1, x2, y2)) {
				c.drawLine(x1, y1, x2, y2, p);
			}
		}

		// draw circles
		p.setAntiAlias(true);
		for (int i = getFirstOccurence(expanders, new Expander(0, -radius
				- yOffset, null)); i < expanders.size(); i++) {
			Expander expander = expanders.get(i);
			int x = expander.x + xOffset;
			int y = expander.y + yOffset;
			if (y - radius > screenHeight) {
				break;
			}
			if (onScreen(x - radius, y - radius, x + radius, y + radius)) {
				plusMinusRender.isPlus = !expander.topic.isOpen;
				plusMinusRender.draw(x - radius, y - radius, 0, 0, c);
			}
		}
		p.setAntiAlias(false);

		for (int i = getFirstOccurence(topics, new TopicView(-yOffset)); i < topics
				.size(); i++) {
			TopicView topic = topics.get(i);
			int x = topic.topicRenderX + xOffset;
			int y = topic.topicRenderY + yOffset;
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
	private void selectTopic(TopicView topic) {
		if (topic == null) {
			return;
		}
		if (selectedTopic != null) {
			selectedTopic.topicRender.setSelected(false);
		}
		topic.topicRender.setSelected(true);
		selectedTopic = topic;
		int x1 = topic.x1 + xOffset;
		int y1 = topic.y1 + yOffset;
		int x2 = topic.x2 + xOffset;
		int y2 = topic.y2 + yOffset;
		int nx = -xOffset, ny = -yOffset;
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
	private int[] updateTopic(TopicView topic, int x, int y) {
		// calculate sizes
		topic.x1 = x;
		topic.y1 = y;
		TopicRender topicRender = topic.topicRender;
		topicRender.setMaxWidth(screenWidth - radius - X_SHIFT - BLOCK_SHIFT);
		int height = topicRender.getHeight();
		if (topic.children.size() > 0) {
			height = Math.max(height, radius * 2);
		}
		int[] ret = new int[3];
		ret[0] = topicRender.getWidth() + BLOCK_SHIFT;
		ret[1] = y;
		ret[2] = height / 2;

		// update topic
		x += radius;
		y += (height - topicRender.getHeight()) / 2;
		x += X_SHIFT + BLOCK_SHIFT;
		topic.topicRenderX = x;
		topic.topicRenderY = y;
		topic.x2 = x + topicRender.getWidth();
		x -= X_SHIFT + BLOCK_SHIFT;
		topics.add(topic);

		// update circle and line
		y = ret[1] + ret[2];
		topic.y2 = y + y - topic.y1;
		lines.add(new Rect(x, y, x + X_SHIFT, y));
		if (topic.children.size() > 0) {
			expanders.add(new Expander(x, y, topic));
		}

		// update subtopics
		x += X_SHIFT;
		y += ret[2];
		if (topic.children.size() > 0 && topic.isOpen) {
			int py = y - ret[2];
			int[] temp;
			for (int i = 0; i < topic.children.size(); i++) {
				y += Y_SHIFT;
				temp = updateTopic(topic.children.get(i), x - radius, y);

				int ny = y + temp[2];
				if (topic.children.get(i).children.size() > 0)
					ny -= radius;
				lines.add(new Rect(x, py, x, ny));
				py = y + temp[2];
				if (topic.children.get(i).children.size() > 0)
					py += radius;

				ret[0] = Math.max(ret[0], temp[0] - radius);
				y += temp[1];
			}
		}

		ret[0] += X_SHIFT + radius;
		ret[1] = y - ret[1];

		return ret;
	}

	private void expanding() {
		for (TopicView topic : topics) {
			if (topic.topicRenderX + topic.topicRender.getWidth() > screenWidth) {
				topic.parent.isOpen = false;
			}
		}
		update();
		expandingNeeded = false;
	}

	// Method to update tree, sizes and references for
	// key pressing
	private void update() {
		expanders.clear();
		lines.clear();
		topics.clear();
		int[] temp = updateTopic(root, 0, 0);
		this.width = temp[0];
		this.height = temp[1];
	}

	// Public methods

	public void selectTopic(Topic topic) {
		for (int i = 0; i < topics.size(); i++) {
			if (topics.get(i).topicRender.getTopic().equals(topic)) {
				selectTopic(topics.get(i));
			}
		}
	}

	@Override
	public void setBounds(int width, int height) {
		screenWidth = width;
		screenHeight = height;
		if (setBoundsNeeded) {
			update();
			setBoundsNeeded = false;
		}
	}

	@Override
	public void draw(int x, int y, int width, int height, Canvas c) {
		xOffset = -x;
		yOffset = -y;
		screenWidth = width;
		screenHeight = height;
		if (selectRootNeeded) {
			selectTopic(root);
			selectRootNeeded = false;
		}
		if (expandingNeeded) {
			expanding();
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
		for (Expander expander : expanders) {
			if ((expander.x - x) * (expander.x - x) + (expander.y - y)
					* (expander.y - y) <= radius * radius) {
				expander.topic.isOpen = !expander.topic.isOpen;
				selectTopic(expander.topic);
				update();
				break;
			}
		}

		// touch topics
		for (TopicView topic : topics) {
			int x1 = topic.topicRenderX;
			int y1 = topic.topicRenderY;
			int x2 = x1 + topic.topicRender.getWidth();
			int y2 = y1 + topic.topicRender.getHeight();
			if (x1 <= x && x <= x2 && y1 <= y && y <= y2) {
				topic.topicRender.onTouch(x - x1, y - y1);
				selectTopic(topic);
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
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_UP:
			selectTopic(selectedTopic.getPrevTopic());
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			selectTopic(selectedTopic.getNextTopic());
			break;
		case KeyEvent.KEYCODE_DPAD_LEFT:
			selectTopic(selectedTopic.parent);
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			if (selectedTopic.children.size() > 0) {
				if (!selectedTopic.isOpen) {
					selectedTopic.isOpen = true;
					update();
				}
				selectTopic(selectedTopic.children.get(0));
			}
			break;
		case KeyEvent.KEYCODE_DPAD_CENTER:
			if (selectedTopic.children.size() > 0) {
				selectedTopic.isOpen = !selectedTopic.isOpen;
				update();
			}
			break;
		}
	}

}
