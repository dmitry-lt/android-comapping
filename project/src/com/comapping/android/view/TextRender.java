package com.comapping.android.view;

import com.comapping.android.model.FormattedText;
import com.comapping.android.model.TextBlock;
import com.comapping.android.model.TextParagraph;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class TextRender extends Render {

	private static final int VERTICAL_MERGING = 3;
	private static final int BORDER = 4;

	private static final int MAX_LETTER_WIDTH = calcMaxLetterWidth();

	private boolean isEmpty;

	private FormattedText text;
	private FormattedText textToDraw;
	private Paint paint;
	private int width, height;
	private int maxWidth;
	private int[] parsWidth;
	private int[] parsHeight;

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
		if (maxWidth < MAX_LETTER_WIDTH) {
			// too small width
			return;
		}
		this.maxWidth = 0;
		for (int i = 0; i < text.getTextParagraphs().size(); i++) {
			int approxLinesCount = parsWidth[i] / maxWidth;
			int parWidthWithAdding = parsWidth[i] + approxLinesCount * MAX_LETTER_WIDTH;
			int linesCount = parWidthWithAdding / maxWidth + 1;
			int optimalWidth = parWidthWithAdding / linesCount;
			this.maxWidth = Math.max(this.maxWidth, optimalWidth);
		}

		textToDraw = new FormattedText();
		for (int i = 0; i < text.getTextParagraphs().size(); i++) {
			TextParagraph paragraph = text.getTextParagraphs().get(i);
			TextParagraph curParagraph = new TextParagraph();
			int curLineWidth = 0;
			for (int j = 0; j < paragraph.getTextBlocks().size(); j++) {
				TextBlock block = paragraph.getTextBlocks().get(j);
				while (true) {
					float blockWidth = paint.measureText(block.getText());
					if (curLineWidth + blockWidth <= this.maxWidth) {
						curParagraph.add(block);
						curLineWidth += blockWidth;
						break;
					} else {
						int fitInCount;
						do {
							float fitInPart = (this.maxWidth - curLineWidth) / blockWidth;
							fitInCount = (int) (block.getText().length() * fitInPart) - 1;
							blockWidth = paint.measureText(block.getText(), 0, fitInCount);
						} while (curLineWidth + blockWidth > this.maxWidth);
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

			int curY = y;
			for (int i = 0; i < textToDraw.getTextParagraphs().size(); i++) {
				TextParagraph paragraph = textToDraw.getTextParagraphs().get(i);
				int curX = x;
				curY += parsHeight[i];
				for (TextBlock block : paragraph.getTextBlocks()) {
					paint.setTextSize(block.getFormat().getFontSize());
					paint.setColor(block.getFormat().getFontColor());
					c.drawText(block.getText(), curX, curY, paint);
					curX += paint.measureText(block.getText());
				}
				curY += VERTICAL_MERGING;
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
		parsHeight = new int[textToDraw.getTextParagraphs().size()];
		Rect r = new Rect();
		for (int i = 0; i < textToDraw.getTextParagraphs().size(); i++) {
			TextParagraph paragraph = textToDraw.getTextParagraphs().get(i);
			int parHeight = 0;
			int parWidth = 0;

			paint.setTextSize(paragraph.getMaxFontSize());
			paint.getTextBounds("1", 0, 1, r);
			parHeight = Math.max(parHeight, r.height());

			for (TextBlock block : paragraph.getTextBlocks()) {
				paint.setTextSize(block.getFormat().getFontSize());
				// paint.getTextBounds(block.getText(), 0,
				// block.getText().length(), r);
				parWidth += paint.measureText(block.getText());
			}
			parsHeight[i] = parHeight;
			// parWidth += HORISONTAL_MERGING *
			// (paragraph.getTextBlocks().size() - 1);

			width = Math.max(width, parWidth);
			height += parHeight;
		}
		height += VERTICAL_MERGING * (textToDraw.getTextParagraphs().size() - 1);

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
