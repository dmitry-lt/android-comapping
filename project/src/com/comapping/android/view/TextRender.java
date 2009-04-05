package com.comapping.android.view;

import com.comapping.android.model.FormattedText;
import com.comapping.android.model.TextBlock;
import com.comapping.android.model.TextParagraph;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class TextRender extends Render {

	private static final int HORISONTAL_MERGING = 3;
	private static final int VERTICAL_MERGING = 3;
	private static final int BORDER = 4;

	private boolean isEmpty;

	private FormattedText text;
	private int width, height;
	private int[] parsHeight;

	public TextRender(FormattedText text) {
		if (text != null && !text.getSimpleText().equals("")) {
			isEmpty = false;
		} else {
			isEmpty = true;
		}

		// calculate sizes
		if (!isEmpty) {
			this.text = text;

			parsHeight = new int[text.getTextParagraphs().size()];
			Paint p = new Paint();
			Rect r = new Rect();
			for (int i = 0; i < text.getTextParagraphs().size(); i++) {
				TextParagraph paragraph = text.getTextParagraphs().get(i);
				int parHeight = 0;
				int parWidth = 0;

				p.setTextSize(paragraph.getMaxFontSize());
				p.getTextBounds("1", 0, 1, r);
				parHeight = Math.max(parHeight, r.height());

				for (TextBlock block : paragraph.getTextBlocks()) {
					p.setTextSize(block.getFormat().getFontSize());
					p.getTextBounds(block.getText(), 0, block.getText().length(), r);
					parWidth += r.width();
				}
				parsHeight[i] = parHeight;
				parWidth += HORISONTAL_MERGING * (paragraph.getTextBlocks().size() - 1);

				width = Math.max(width, parWidth);
				height += parHeight;
			}
			height += VERTICAL_MERGING * (text.getTextParagraphs().size() - 1);

			width += BORDER * 2;
			height += BORDER * 2;
		} else {
			width = 0;
			height = 0;
		}

	}

	@Override
	public void draw(int x, int y, int width, int height, Canvas c) {
		if (!isEmpty) {
			x += BORDER;
			y += BORDER;

			Paint p = new Paint();
			Rect r = new Rect();
			int curY = y;
			for (int i = 0; i < text.getTextParagraphs().size(); i++) {
				TextParagraph paragraph = text.getTextParagraphs().get(i);
				int curX = x;
				curY += parsHeight[i];
				for (TextBlock block : paragraph.getTextBlocks()) {
					p.setTextSize(block.getFormat().getFontSize());
					p.setColor(block.getFormat().getFontColor());
					p.setAntiAlias(true);
					c.drawText(block.getText(), curX, curY, p);
					p.getTextBounds(block.getText(), 0, block.getText().length(), r);

					// p.setColor(Color.BLACK);
					// c.drawLine(curX, curY - parsHeight[i], curX + r.width(),
					// curY - parsHeight[i], p);

					curX += r.width() + HORISONTAL_MERGING;
				}
				curY += VERTICAL_MERGING;
			}
		} else {
			// nothing to draw
		}
	}

	public String getText() {
		return (text != null) ? text.getSimpleText() : "";
	}

	@Override
	public String toString() {
		if (!isEmpty) {
			return "[TextRender: width=" + getWidth() + " height=" + getHeight() + " text=" + text.getSimpleText()
					+ "]";
		} else {
			return "[TextRender: EMPTY]";
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
		// TODO Auto-generated method stub

	}

}
