package com.comapping.android.view;

import com.comapping.android.Log;
import com.comapping.android.model.FormattedText;
import com.comapping.android.model.TextBlock;
import com.comapping.android.model.TextParagraph;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

public class TextRender extends Render {
	private static final int BORDER = 4;
	private static final int MAX_LETTER_WIDTH = calcMaxLetterWidth();

	private boolean isEmpty;

	private FormattedText text;
	private FormattedText textToDraw;
	private Paint paint;
	private int width, height;
	private int maxWidth;

	private int[] parsWidth;
	private Point[][] blocksCoord;

	public TextRender(FormattedText text) {
		if (text != null && !text.getSimpleText().equals("")) {
			isEmpty = false;
		} else {
			isEmpty = true;
		}

		if (!isEmpty) {
			this.text = text;

			paint = new Paint();
			paint.setAntiAlias(true);

			// precalc of paragraphs width
			parsWidth = new int[text.getTextParagraphs().size()];
			for (int i = 0; i < text.getTextParagraphs().size(); i++) {
				TextParagraph paragraph = text.getTextParagraphs().get(i);
				parsWidth[i] = 0;
				for (TextBlock block : paragraph.getTextBlocks()) {
					paint.setTextSize(block.getFormat().getFontSize());
					parsWidth[i] += paint.measureText(block.getText());
				}
			}

			textToDraw = text;
			recalcDrawingData();
		} else {
			width = 0;
			height = 0;
		}

	}

	public void setMaxWidth(int maxWidth) {
		Log.d(Log.topicRenderTag, "setting maxWidth=" + maxWidth + " in " + this);

		if (maxWidth < MAX_LETTER_WIDTH) {
			// too small width
			return;
		}
		int sumLinesCount = 0;
		this.maxWidth = 0;
		for (int i = 0; i < text.getTextParagraphs().size(); i++) {
			int approxLinesCount = parsWidth[i] / maxWidth;
			int parWidthWithAdding = parsWidth[i] + approxLinesCount * MAX_LETTER_WIDTH;
			int linesCount = parWidthWithAdding / maxWidth + 1;
			sumLinesCount += linesCount;
			int optimalWidth = parWidthWithAdding / linesCount;
			this.maxWidth = Math.max(this.maxWidth, optimalWidth);
		}

		Log.d(Log.topicRenderTag, "optimal maxWidth=" + this.maxWidth);
		Log.d(Log.topicRenderTag, "optimal lines count=" + sumLinesCount);

		textToDraw = new FormattedText();
		for (int i = 0; i < text.getTextParagraphs().size(); i++) {
			TextParagraph paragraph = text.getTextParagraphs().get(i);
			TextParagraph curParagraph = new TextParagraph();
			int curLineWidth = 0;
			for (int j = 0; j < paragraph.getTextBlocks().size(); j++) {
				TextBlock block = paragraph.getTextBlocks().get(j);
				paint.setTextSize(block.getFormat().getFontSize());
				while (true) {
					float blockWidth = paint.measureText(block.getText());
					if (curLineWidth + blockWidth <= this.maxWidth) {
						curParagraph.add(block);
						curLineWidth += blockWidth;
						break;
					} else {
						// int fitInCount = block.getText().length();
						// do {
						// float fitInPart = (this.maxWidth - curLineWidth) /
						// blockWidth;
						// fitInCount = (int) (fitInCount * fitInPart);
						// blockWidth = paint.measureText(block.getText(), 0,
						// fitInCount);
						// } while (curLineWidth + blockWidth > this.maxWidth);

						int fitInCount = paint.breakText(block.getText(), true, this.maxWidth - curLineWidth, null);
						TextBlock[] blocks = block.split(fitInCount);
						curParagraph.add(blocks[0]);
						textToDraw.add(curParagraph);
						curParagraph = new TextParagraph();
						block = blocks[1];
					}
				}
			}
			textToDraw.add(curParagraph);
		}

		recalcDrawingData();
	}

	@Override
	public void draw(int x, int y, int width, int height, Canvas c) {
		if (!isEmpty) {
			x += BORDER;
			y += BORDER;

			for (int i = 0; i < textToDraw.getTextParagraphs().size(); i++) {
				TextParagraph paragraph = textToDraw.getTextParagraphs().get(i);
				for (int j = 0; j < paragraph.getTextBlocks().size(); j++) {
					TextBlock block = paragraph.getTextBlocks().get(j);
					paint.setTextSize(block.getFormat().getFontSize());
					paint.setColor(block.getFormat().getFontColor());
					paint.setUnderlineText(block.getFormat().isUnderlined());
					c.drawText(block.getText(), x + blocksCoord[i][j].x, y + blocksCoord[i][j].y, paint);
				}
			}
		} else {
			// nothing to draw
		}
	}

	public String getText() {
		return (textToDraw != null) ? textToDraw.getSimpleText() : "";
	}

	@Override
	public String toString() {
		if (!isEmpty) {
			return "[TextRender: width=" + getWidth() + " height=" + getHeight() + " text="
					+ textToDraw.getSimpleText() + "]";
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

	private void recalcDrawingData() {
		width = 0;
		height = 0;
		blocksCoord = new Point[textToDraw.getTextParagraphs().size()][];
		for (int i = 0; i < textToDraw.getTextParagraphs().size(); i++) {
			TextParagraph paragraph = textToDraw.getTextParagraphs().get(i);
			blocksCoord[i] = new Point[paragraph.getTextBlocks().size()];
			int curWidth = 0;
			paint.setTextSize(paragraph.getMaxFontSize());
			height += -paint.ascent();
			for (int j = 0; j < paragraph.getTextBlocks().size(); j++) {
				TextBlock block = paragraph.getTextBlocks().get(j);
				blocksCoord[i][j] = new Point(curWidth, height);
				paint.setTextSize(block.getFormat().getFontSize());
				curWidth += paint.measureText(block.getText());
			}
			width = Math.max(width, curWidth);
			paint.setTextSize(paragraph.getMaxFontSize());
			height += paint.descent();
		}

		width += BORDER * 2;
		height += BORDER * 2;
	}

	private static int calcMaxLetterWidth() {
		Paint p = new Paint();
		Rect r = new Rect();
		p.setTextSize(22);
		p.getTextBounds("ù", 0, 1, r);
		return r.width();
	}

}
