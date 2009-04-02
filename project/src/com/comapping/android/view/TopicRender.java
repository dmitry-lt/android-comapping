package com.comapping.android.view;

import com.comapping.android.model.Topic;

import android.graphics.Canvas;
import android.graphics.Paint;

public class TopicRender extends Render {

	private static final int HORISONTAL_MERGING = 5;
	private static final int BORDER_SIZE = 4;

	private IconRender iconRender;
	private TextRender textRender;
	private Topic topic;
	private int width, height;
	private boolean selected;

	public TopicRender(Topic topic) {
		this.topic = topic;
		textRender = new TextRender(topic.getFormattedText());
		iconRender = new IconRender(topic);

		height = Math.max(iconRender.getHeight(), textRender.getHeight() + BORDER_SIZE * 2);
		width = iconRender.getWidth() + HORISONTAL_MERGING + textRender.getWidth() + BORDER_SIZE * 2;
	}

	@Override
	public void draw(int x, int y, int width, int height, Canvas c) {

		Paint p = new Paint();

		y += this.height / 2;

		// draw icons
		iconRender.draw(x, y - iconRender.getHeight() / 2, 0, 0, c);
		x += iconRender.getWidth();

		// draw text
		x += HORISONTAL_MERGING;
		p.setColor(topic.getBgColor());
		p.setAlpha(255);
		c.drawRect(x, y - textRender.getHeight() / 2 - BORDER_SIZE, x + textRender.getWidth() + BORDER_SIZE * 2, y
				+ textRender.getHeight() / 2 + BORDER_SIZE, p);

		x += BORDER_SIZE;

		textRender.draw(x, y - textRender.getHeight() / 2, 0, 0, c);

	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isSelected() {
		return selected;
	}

	public int getLineOffset() {
		return height;
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
		// TODO Auto-generated method stub

	}

}
