package com.comapping.android.view;

import com.comapping.android.Log;
import com.comapping.android.model.Topic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.Style;

import static com.comapping.android.view.RenderHelper.pointLiesOnRect;

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
	private Rect iconRect = new Rect();
	private Rect textRect = new Rect();
	private Rect taskRect = new Rect();
	private Rect noteRect = new Rect();
	private Rect attachmentRect = new Rect();

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
			textRender = new TextRender(topic.getFormattedText(), context);
			iconRender = new IconRender(topic);
			taskRender = new TaskRender(topic.getTask(), context);
			noteRender = new NoteRender(topic.getNote(), context);
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
			iconRender.draw(x + iconRect.left, y + iconRect.top, 0, 0, c);
			// draw text background
			Paint p = new Paint();
			p.setColor(topic.getBgColor());
			p.setAlpha(255);
			c.drawRect(x + textRect.left, y + textRect.top, x + textRect.right, y + textRect.bottom, p);

			textRender.draw(x + textRect.left, y + textRect.top, 0, 0, c);
			attachmentRender.draw(x + attachmentRect.left, y + attachmentRect.top, 0, 0, c);

			taskRender.draw(x + taskRect.left, y + taskRect.top, 0, 0, c);

			noteRender.draw(x + noteRect.left, y + noteRect.top, 0, 0, c);

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

			if (pointLiesOnRect(touchPoint, iconRect)) {
				touchPoint.offset(-iconRect.left, -iconRect.top);
				iconRender.onTouch(touchPoint.x, touchPoint.y);

			} else if (pointLiesOnRect(touchPoint, textRect)) {
				touchPoint.offset(-textRect.left, -textRect.top);
				textRender.onTouch(touchPoint.x, touchPoint.y);

			} else if (pointLiesOnRect(touchPoint, attachmentRect)) {
				touchPoint.offset(-attachmentRect.left, -attachmentRect.top);
				attachmentRender.onTouch(touchPoint.x, touchPoint.y);

			} else if (pointLiesOnRect(touchPoint, taskRect)) {
				touchPoint.offset(-taskRect.left, -taskRect.top);
				taskRender.onTouch(touchPoint.x, touchPoint.y);

			} else if (pointLiesOnRect(touchPoint, noteRect)) {
				touchPoint.offset(-noteRect.left, -noteRect.top);
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

	private void recalcDrawingData() {
		// recalc size
		lineOffset = Math.max(iconRender.getHeight(), textRender.getHeight());
		lineOffset = Math.max(lineOffset, attachmentRender.getHeight());

		height = Math.max(iconRender.getHeight(), textRender.getHeight()) + taskRender.getHeight()
				+ noteRender.getHeight();
		height = Math.max(height, attachmentRender.getHeight());
		width = Math.max(iconRender.getWidth() + HORISONTAL_MERGING + textRender.getWidth(), Math.max(taskRender
				.getWidth(), noteRender.getWidth()))
				+ attachmentRender.getWidth();

		// recalc coords
		iconRect.left = 0;
		iconRect.top = (getLineOffset() - iconRender.getHeight()) / 2;
		iconRect.right = iconRect.left + iconRender.getWidth();
		iconRect.bottom = iconRect.top + iconRender.getHeight();

		textRect.left = iconRender.getWidth() + HORISONTAL_MERGING;
		textRect.top = (getLineOffset() - textRender.getHeight()) / 2;
		textRect.right = textRect.left + textRender.getWidth();
		textRect.bottom = textRect.top + textRender.getHeight();

		attachmentRect.left = iconRender.getWidth() + textRender.getWidth() + HORISONTAL_MERGING * 2;
		attachmentRect.top = (getLineOffset() - attachmentRender.getHeight()) / 2;
		attachmentRect.right = attachmentRect.left + attachmentRender.getWidth();
		attachmentRect.bottom = attachmentRect.top + attachmentRender.getHeight();

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
