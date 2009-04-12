package com.comapping.android.view;

import com.comapping.android.Log;
import com.comapping.android.model.Topic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Paint.Style;

public class TopicRender extends Render {

	private static final int HORISONTAL_MERGING = 0;
	private static final int SELECTION_COLOR = Color.argb(255, 127, 191, 255);
	private static final int SELECTION_WIDTH = 3;
	private static final int SELECTION_EDGES_RADIUS = 4;

	private boolean isEmpty;

	private IconRender iconRender;
	private TextRender textRender;
	private TaskRender taskRender;
	private NoteRender noteRender;
	private AttachmentRender attachmentRender;

	// relative coordinates of upper left corner of renders
	private Point iconCoords = new Point();
	private Point textCoords = new Point();
	private Point taskCoords = new Point();
	private Point noteCoords = new Point();
	private Point attachmentCoords = new Point();

	private Topic topic;
	private int width, height;
	private int lastMaxWidth;
	private int lineOffset;
	private boolean selected;

	private float iconPart;
	private float textPart;

	public TopicRender(Topic topic, Context context) {
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
			attachmentRender = new AttachmentRender(topic.getAttachment(), context);

			float iconSquare = iconRender.getWidth() * iconRender.getHeight();
			float textSquare = textRender.getWidth() * textRender.getHeight();

			iconPart = iconSquare / (iconSquare + textSquare);
			textPart = 1 - iconPart;

			recalcDrawingData();
		} else {
			height = 0;
			width = 0;
		}

		Log.d(Log.topicRenderTag, "created " + this);
	}

	@Override
	public void draw(int x, int y, int width, int height, Canvas c) {
		if (!isEmpty) {
			iconRender.draw(x + iconCoords.x, y + iconCoords.y, 0, 0, c);
			// draw text background
			Paint p = new Paint();
			p.setColor(topic.getBgColor());
			p.setAlpha(255);
			c.drawRect(x + textCoords.x, y + textCoords.y, x + textCoords.x + textRender.getWidth(), y + textCoords.y
					+ textRender.getHeight(), p);

			textRender.draw(x + textCoords.x, y + textCoords.y, 0, 0, c);
			attachmentRender.draw(x + attachmentCoords.x, y + attachmentCoords.y, 0, 0, c);

			taskRender.draw(x + taskCoords.x, y + taskCoords.y, 0, 0, c);

			noteRender.draw(x + noteCoords.x, y + noteCoords.y, 0, 0, c);

			// draw selection
			if (isSelected()) {
				p.setColor(SELECTION_COLOR);
				p.setAlpha(255);
				p.setAntiAlias(true);
				p.setStyle(Style.STROKE);
				p.setStrokeWidth(SELECTION_WIDTH);
				c.drawRoundRect(new RectF(x, y, x + getWidth(), y + getHeight()), SELECTION_EDGES_RADIUS,
						SELECTION_EDGES_RADIUS, p);
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
		Log.d(Log.topicRenderTag, "Touch on " + this);

		if (!isEmpty && selected) {
			Point touchPoint = new Point(x, y);

			if (pointLiesOnRect(touchPoint, iconCoords, iconRender.getWidth(), iconRender.getHeight())) {
				touchPoint.offset(-iconCoords.x, -iconCoords.y);
				iconRender.onTouch(touchPoint.x, touchPoint.y);

			} else if (pointLiesOnRect(touchPoint, textCoords, textRender.getWidth(), textRender.getHeight())) {
				touchPoint.offset(-textCoords.x, -textCoords.y);
				textRender.onTouch(touchPoint.x, touchPoint.y);

			} else if (pointLiesOnRect(touchPoint, attachmentCoords, attachmentRender.getWidth(), attachmentRender
					.getHeight())) {
				touchPoint.offset(-attachmentCoords.x, -attachmentCoords.y);
				attachmentRender.onTouch(touchPoint.x, touchPoint.y);

			} else if (pointLiesOnRect(touchPoint, taskCoords, taskRender.getWidth(), taskRender.getHeight())) {
				touchPoint.offset(-taskCoords.x, -taskCoords.y);
				taskRender.onTouch(touchPoint.x, touchPoint.y);

			} else if (pointLiesOnRect(touchPoint, noteCoords, noteRender.getWidth(), noteRender.getHeight())) {
				touchPoint.offset(-noteCoords.x, -noteCoords.y);
				noteRender.onTouch(touchPoint.x, touchPoint.y);
			}
		}
	}

	public void setMaxWidth(int maxWidth) {
		if (maxWidth != lastMaxWidth) {
			Log.d(Log.topicRenderTag, "setting maxWidth=" + maxWidth + " in " + this);

			lastMaxWidth = maxWidth;
			maxWidth -= attachmentRender.getWidth();
			int iconMaxWidth = (int) (iconPart * maxWidth);
			int textMaxWidth = (int) (textPart * maxWidth);
			iconRender.setMaxWidth(iconMaxWidth);
			textRender.setMaxWidth(textMaxWidth);
			maxWidth = iconRender.getWidth() + textRender.getWidth();
			taskRender.setWidth(maxWidth);
			noteRender.setMaxWidth(maxWidth);

			recalcDrawingData();
		}
	}

	private boolean pointLiesOnRect(Point p, Point corner, int width, int height) {
		if (corner.x <= p.x && p.x <= corner.x + width && corner.y <= p.y && p.y <= corner.y + height) {
			return true;
		} else {
			return false;
		}
	}

	private void recalcDrawingData() {
		// recalc size
		lineOffset = Math.max(iconRender.getHeight(), textRender.getHeight());

		height = Math.max(iconRender.getHeight(), textRender.getHeight()) + taskRender.getHeight()
				+ noteRender.getHeight();
		width = Math.max(iconRender.getWidth() + HORISONTAL_MERGING + textRender.getWidth(), Math.max(taskRender
				.getWidth(), noteRender.getWidth()))
				+ attachmentRender.getWidth();

		// recalc coords
		iconCoords.x = 0;
		iconCoords.y = (getLineOffset() - iconRender.getHeight()) / 2;

		textCoords.x = iconRender.getWidth() + HORISONTAL_MERGING;
		textCoords.y = (getLineOffset() - textRender.getHeight()) / 2;

		attachmentCoords.x = iconRender.getWidth() + textRender.getWidth() + HORISONTAL_MERGING * 2;
		attachmentCoords.y = (getLineOffset() - attachmentRender.getHeight()) / 2;

		taskCoords.x = 0;
		taskCoords.y = getLineOffset();

		noteCoords.x = 0;
		noteCoords.y = getLineOffset() + taskRender.getHeight();
	}
}
