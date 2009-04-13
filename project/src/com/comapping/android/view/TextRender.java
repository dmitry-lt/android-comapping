package com.comapping.android.view;

import com.comapping.android.Log;
import com.comapping.android.model.FormattedText;
import com.comapping.android.model.TextBlock;
import com.comapping.android.model.TextFormat;
import com.comapping.android.model.TextParagraph;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;

import static com.comapping.android.view.RenderHelper.pointLiesOnRect;

;

public class TextRender extends Render {
	private static final int BORDER = 4;
	private static final int MAX_LETTER_WIDTH = calcWidth(new TextBlock("ù", new TextFormat(22, 0, "", false)));
	private static final TextBlock THREE_DOTS = new TextBlock("...", new TextFormat());
	private static final int THREE_DOTS_WIDTH = calcWidth(THREE_DOTS);

	private boolean isEmpty;

	private FormattedText text;
	private FormattedText textToDraw;
	private Paint paint;
	private int width, height;
	private int maxWidth;

	private int[] parsWidth;
	private Point[][] blocksDrawCoord;
	private Rect[][] blocksRect;

	private Context context;

	public TextRender(FormattedText text, Context context) {
		if (text != null && !text.getSimpleText().equals("")) {
			isEmpty = false;
		} else {
			isEmpty = true;
		}

		if (!isEmpty) {
			this.text = text;
			this.context = context;

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

	public void setOneLineView(int maxWidth) {
		Log.v(Log.topicRenderTag, "setting OneLineView with maxWidth=" + maxWidth + " in " + this);

		maxWidth -= THREE_DOTS_WIDTH;
		TextParagraph parToDraw = new TextParagraph();
		int curWidth = 0;
		boolean isFull = false;
		for (TextParagraph paragraph : text.getTextParagraphs()) {
			for (TextBlock block : paragraph.getTextBlocks()) {
				paint.setTextScaleX(block.getFormat().getFontSize());
				float width = paint.measureText(block.getText());
				if (curWidth + width <= maxWidth) {
					curWidth += width;
					parToDraw.add(block);
				} else {
					int fitInCount = paint.breakText(block.getText(), true, maxWidth - curWidth, null);
					parToDraw.add(block.split(fitInCount)[0]);
					parToDraw.add(THREE_DOTS);
					isFull = true;
					break;
				}
			}
			if (isFull)
				break;
		}

		textToDraw = new FormattedText();
		textToDraw.add(parToDraw);

		recalcDrawingData();
	}

	public void setMaxWidth(int maxWidth) {
		if (!isEmpty) {
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
							// float fitInPart = (this.maxWidth - curLineWidth)
							// /
							// blockWidth;
							// fitInCount = (int) (fitInCount * fitInPart);
							// blockWidth = paint.measureText(block.getText(),
							// 0,
							// fitInCount);
							// } while (curLineWidth + blockWidth >
							// this.maxWidth);

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
					c.drawText(block.getText(), x + blocksDrawCoord[i][j].x, y + blocksDrawCoord[i][j].y, paint);
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
		if (!isEmpty) {
			Point touchPoint = new Point(x, y);
			Log.d(Log.topicRenderTag, "Touch on " + touchPoint + " " + this);
			for (int i = 0; i < textToDraw.getTextParagraphs().size(); i++) {
				TextParagraph paragraph = textToDraw.getTextParagraphs().get(i);
				for (int j = 0; j < paragraph.getTextBlocks().size(); j++) {
					if (pointLiesOnRect(touchPoint, blocksRect[i][j])) {
						Log.d(Log.topicRenderTag, "Touch on " + paragraph.getTextBlocks().get(j));
						String url = paragraph.getTextBlocks().get(j).getFormat().getHRef();
						if (!url.equals("")) {
							context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
						}
						return;
					}
				}
			}
		}
	}

	private void recalcDrawingData() {
		width = 0;
		height = 0;
		blocksDrawCoord = new Point[textToDraw.getTextParagraphs().size()][];
		blocksRect = new Rect[textToDraw.getTextParagraphs().size()][];
		for (int i = 0; i < textToDraw.getTextParagraphs().size(); i++) {
			TextParagraph paragraph = textToDraw.getTextParagraphs().get(i);
			blocksDrawCoord[i] = new Point[paragraph.getTextBlocks().size()];
			blocksRect[i] = new Rect[paragraph.getTextBlocks().size()];
			int curWidth = 0;
			paint.setTextSize(paragraph.getMaxFontSize());
			int baseline = height + (int) (-paint.ascent());
			int parHeight = (int) (-paint.ascent() + paint.descent());
			for (int j = 0; j < paragraph.getTextBlocks().size(); j++) {
				TextBlock block = paragraph.getTextBlocks().get(j);
				blocksDrawCoord[i][j] = new Point(curWidth, baseline);
				paint.setTextSize(block.getFormat().getFontSize());
				int blockWidth = (int) paint.measureText(block.getText());
				blocksRect[i][j] = new Rect(curWidth, height, curWidth + blockWidth, height + parHeight);
				curWidth += blockWidth;
			}
			width = Math.max(width, curWidth);
			height += parHeight;
		}

		width += BORDER * 2;
		height += BORDER * 2;
	}

	private static int calcWidth(TextBlock block) {
		Paint p = new Paint();
		p.setTextSize(block.getFormat().getFontSize());
		return (int) p.measureText(block.getText());
	}
}
