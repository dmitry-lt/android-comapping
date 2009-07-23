package com.comapping.android.map.render.topic;

import com.comapping.android.map.model.map.Topic;
import com.comapping.android.map.render.Render;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.Style;

import static com.comapping.android.map.render.topic.RenderHelper.pointLiesOnRect;

public class TopicRender extends Render {

	// private static final int HORISONTAL_MERGING = 0;
	private static final int SELECTION_COLOR = Color.argb(255, 127, 191, 255);
	private static final int SELECTION_WIDTH = 3;
	private static final int SELECTION_EDGES_RADIUS = 4;
	private Topic topic;

	private boolean isEmpty;

	private IconRender iconRender;
	private TextRender textRender;
	private AttachmentRender attachmentRender;
	private TaskRender taskRender;
	private NoteRender noteRender;

	private Rect iconRect = new Rect();
	private Rect textRect = new Rect();
	private Rect attachmentRect = new Rect();
	private Rect taskRect = new Rect();
	private Rect noteRect = new Rect();

	private int width, height;
	private int lastMaxWidth;
	private int lineOffset;
	private boolean selected;

	private float iconPart;
	private float textPart;

	public TopicRender(Topic topic, Context context) {
		this.topic = topic;
		isEmpty = (topic == null);

		if (!isEmpty) {
			textRender = new TextRender(topic.getFormattedText(), topic
					.getBgColor(), context);
			iconRender = new IconRender(topic, context);
			taskRender = new TaskRender(topic.getTask(), context);
			noteRender = new NoteRender(topic.getNote(), context);
			attachmentRender = new AttachmentRender(topic.getAttachment(),
					context);

			// calc approximately icon and text parts to recal width
			float iconSquare = iconRender.getWidth() * iconRender.getHeight();
			float textSquare = textRender.getWidth() * textRender.getHeight();
			iconPart = iconSquare / (iconSquare + textSquare);
			textPart = 1 - iconPart;

			recalcDrawingData();
		} else {
			height = 0;
			width = 0;
		}

		//Log.d(Log.TOPIC_RENDER_TAG, "created " + this);
	}

	public void draw(int x, int y, int width, int height, Canvas c) {
		if (!isEmpty) {
			iconRender.draw(x + iconRect.left, y + iconRect.top, 0, 0, c);
			textRender.draw(x + textRect.left, y + textRect.top, 0, 0, c);
			attachmentRender.draw(x + attachmentRect.left, y
					+ attachmentRect.top, 0, 0, c);
			taskRender.draw(x + taskRect.left, y + taskRect.top, 0, 0, c);
			noteRender.draw(x + noteRect.left, y + noteRect.top, 0, 0, c);

			// draw selection
			Paint p = new Paint();
			if (isSelected()) {
				p.setColor(SELECTION_COLOR);
				p.setAlpha(255);
				p.setAntiAlias(true);
				p.setStyle(Style.STROKE);
				p.setStrokeWidth(SELECTION_WIDTH);
				RectF rect = new RectF(x, y, x + getWidth(), y + getHeight());
				c.drawRoundRect(rect, SELECTION_EDGES_RADIUS,
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

	public Topic getTopic() {
		return topic;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public String toString() {
		if (!isEmpty) {
			return "[TopicRender: width=" + getWidth() + " height="
					+ getHeight() + "\n\t" + iconRender + "\n\t" + textRender
					+ "\n\t" + attachmentRender + "\n\t" + taskRender + "\n\t"
					+ noteRender + "]\n";
		} else {
			return "[TopicRender: EMPTY]";
		}
	}

	public boolean onTouch(int x, int y) {
		//Log.d(Log.TOPIC_RENDER_TAG, "Touch on " + this);
		boolean result = false;
		if (!isEmpty && selected) {
			Point touchPoint = new Point(x, y);

			if (pointLiesOnRect(touchPoint, iconRect)) {
				touchPoint.offset(-iconRect.left, -iconRect.top);
				if (iconRender.onTouch(touchPoint.x, touchPoint.y)) {
					result = true;
				}
			} else if (pointLiesOnRect(touchPoint, textRect)) {
				touchPoint.offset(-textRect.left, -textRect.top);
				if (textRender.onTouch(touchPoint.x, touchPoint.y)) {
					result = true;
				}
			} else if (pointLiesOnRect(touchPoint, attachmentRect)) {
				touchPoint.offset(-attachmentRect.left, -attachmentRect.top);
				if (attachmentRender.onTouch(touchPoint.x, touchPoint.y)) {
					result = true;
				}
			} else if (pointLiesOnRect(touchPoint, taskRect)) {
				touchPoint.offset(-taskRect.left, -taskRect.top);
				if (taskRender.onTouch(touchPoint.x, touchPoint.y)) {
					result = true;
				}

			} else if (pointLiesOnRect(touchPoint, noteRect)) {
				touchPoint.offset(-noteRect.left, -noteRect.top);
				if (noteRender.onTouch(touchPoint.x, touchPoint.y)) {
					topic.setNote(noteRender.getNote());
					result = true;
				}
			}
			if (result) {
				int t = lastMaxWidth;
				lastMaxWidth++;
				setMaxWidth(t);
			}
		}
		return result;
	}

	public void setMaxWidth(int maxWidth) {
		if (!isEmpty && maxWidth != lastMaxWidth) {
			// Log.d(Log.TOPIC_RENDER_TAG, "setting maxWidth=" + maxWidth +
			// " in " + this);

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

	private void recalcDrawingData() {
		// recalc size
		lineOffset = Math.max(iconRender.getHeight(), textRender.getHeight());
		lineOffset = Math.max(lineOffset, attachmentRender.getHeight());

		int widthWithoutAttach = Math.max(iconRender.getWidth()
				+ textRender.getWidth(), taskRender.getWidth());
		widthWithoutAttach = Math
				.max(widthWithoutAttach, noteRender.getWidth());
		width = widthWithoutAttach + attachmentRender.getWidth();
		height = lineOffset + taskRender.getHeight() + noteRender.getHeight();

		// recalc coords
		iconRect.left = 0;
		iconRect.top = (getLineOffset() - iconRender.getHeight()) / 2;
		iconRect.right = iconRect.left + iconRender.getWidth();
		iconRect.bottom = iconRect.top + iconRender.getHeight();

		textRect.left = iconRender.getWidth();
		textRect.top = (getLineOffset() - textRender.getHeight()) / 2;
		textRect.right = textRect.left + textRender.getWidth();
		textRect.bottom = textRect.top + textRender.getHeight();

		attachmentRect.left = widthWithoutAttach;
		attachmentRect.top = (getLineOffset() - attachmentRender.getHeight()) / 2;
		attachmentRect.right = attachmentRect.left
				+ attachmentRender.getWidth();
		attachmentRect.bottom = attachmentRect.top
				+ attachmentRender.getHeight();

		taskRect.left = 0;
		taskRect.top = getLineOffset();
		taskRect.right = taskRect.left + taskRender.getWidth();
		taskRect.bottom = taskRect.top + taskRender.getHeight();

		noteRect.left = 0;
		noteRect.top = getLineOffset() + taskRender.getHeight();
		noteRect.right = noteRect.left + noteRender.getWidth();
		noteRect.bottom = noteRect.top + noteRender.getHeight();
	}
}
