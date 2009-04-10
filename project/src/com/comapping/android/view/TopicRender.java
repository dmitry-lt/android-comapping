package com.comapping.android.view;

import com.comapping.android.Log;
import com.comapping.android.model.Topic;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Style;

public class TopicRender extends Render {

	private static final int HORISONTAL_MERGING = 5;
	private static final int SELECTION_COLOR = Color.argb(255, 127, 191, 255);
	private static final int SELECTION_WIDTH = 3;

	private boolean isEmpty;

	private IconRender iconRender;
	private TextRender textRender;
	private TaskRender taskRender;
	private NoteRender noteRender;

	private Topic topic;
	private int width, height;
	private int lineOffset;
	private boolean selected;

	public TopicRender(Topic topic) {
		if (topic != null) {
			isEmpty = false;
		} else {
			isEmpty = true;
		}

		if (!isEmpty) {
			this.topic = topic;
			textRender = new TextRender(topic.getFormattedText());
			iconRender = new IconRender(topic);
			taskRender = new TaskRender(topic.getTask());
			noteRender = new NoteRender(topic.getNote());

			lineOffset = Math.max(iconRender.getHeight(), textRender.getHeight());

			height = Math.max(iconRender.getHeight(), textRender.getHeight()) + taskRender.getHeight()
					+ noteRender.getHeight();
			width = Math.max(iconRender.getWidth() + HORISONTAL_MERGING + textRender.getWidth(), Math.max(taskRender
					.getWidth(), noteRender.getWidth()));
			taskRender.setWidth(width);
		} else {
			height = 0;
			width = 0;
		}

		Log.d(Log.topicRenderTag, "created " + this);
	}

	@Override
	public void draw(int x, int y, int width, int height, Canvas c) {
		if (!isEmpty) {
			Paint p = new Paint();
			int curX = x, curY = y;

			curY += this.getLineOffset() / 2;

			// draw icons
			iconRender.draw(curX, curY - iconRender.getHeight() / 2, 0, 0, c);
			curX += iconRender.getWidth();

			// draw text
			curX += HORISONTAL_MERGING;
			p.setColor(topic.getBgColor());
			p.setAlpha(255);
			c.drawRect(curX, curY - textRender.getHeight() / 2, curX + textRender.getWidth(), curY
					+ textRender.getHeight() / 2, p);

			textRender.draw(curX, curY - textRender.getHeight() / 2, 0, 0, c);

			// draw task
			curX = x;
			curY = y + this.getLineOffset();
			taskRender.draw(curX, curY, width, height, c);

			// draw note
			curX = x;
			curY += taskRender.getHeight();
			noteRender.draw(curX, curY, width, height, c);

			// draw selection
			if (isSelected()) {
				p.setColor(SELECTION_COLOR);
				p.setAlpha(255);
				p.setAntiAlias(true);
				p.setStyle(Style.STROKE);
				p.setStrokeWidth(SELECTION_WIDTH);
				c.drawRoundRect(new RectF(x, y, x + getWidth(), y + getHeight()), 4, 4, p);
			}

		} else {
			// nothing to draw
		}
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isSelected() {
		return selected;
	}

	public int getLineOffset() {
		return lineOffset;
	}

	@Override
	public String toString() {
		if (!isEmpty) {
			return "[TopicRender: width=" + getWidth() + " height=" + getHeight() + "\n" + iconRender + "\n"
					+ textRender + "\n" + taskRender + "\n" + noteRender + "]";
		} else {
			return "[TopicRender: EMPTY]";
		}
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
		Log.d(Log.topicRenderTag, "Touch on " + topic);
	}

}
