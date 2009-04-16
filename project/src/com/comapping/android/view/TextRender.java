package com.comapping.android.view;

import com.comapping.android.Log;
import com.comapping.android.model.FormattedText;
import com.comapping.android.model.TextBlock;
import com.comapping.android.model.TextFormat;
import com.comapping.android.model.TextParagraph;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import static com.comapping.android.view.RenderHelper.pointLiesOnRect;

public class TextRender extends Render {
	private static final int BORDER = 4;
	private static final TextBlock THREE_DOTS = new TextBlock("...", new TextFormat());
	private static final int THREE_DOTS_WIDTH = calcWidth(THREE_DOTS);
	private static final int MIN_MAX_WIDTH = THREE_DOTS_WIDTH;
	// private static final int MIN_MAX_WIDTH = calcWidth(new TextBlock("ù", new
	// TextFormat(22, 0, "", false)));

	private static final int ADD_WIDTH = 10;

	private boolean isEmpty;

	private FormattedText text;
	private FormattedText textToDraw;
	private int bgColor;
	private Paint paint;
	private int width, height;
	private int[] parsWidth;
	private Point[][] blocksDrawCoord; // y is a baseline of text, it is'n
	// upper-left corner
	private Rect[][] blocksRect;

	private Context context;

	public TextRender(FormattedText text, Context context) {
		this(text, Color.TRANSPARENT, context);
	}

	public TextRender(FormattedText text, int bgColor, Context context) {
		if (text != null && !text.getSimpleText().equals("")) {
			isEmpty = false;
		} else {
			isEmpty = true;
		}

		if (!isEmpty) {
			this.text = text;
			this.bgColor = bgColor;
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
				paint.setTextSize(block.getFormat().getFontSize());
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

			setMaxWidthAndLinesCount(maxWidth, Integer.MAX_VALUE);
			/*
			 * if (maxWidth < MIN_MAX_WIDTH) { // too small width return; } int
			 * sumLinesCount = 0; this.maxWidth = 0; for (int i = 0; i <
			 * text.getTextParagraphs().size(); i++) { int approxLinesCount =
			 * parsWidth[i] / maxWidth; int parWidthWithAdding = parsWidth[i] +
			 * approxLinesCount * MIN_MAX_WIDTH; int linesCount =
			 * parWidthWithAdding / maxWidth + 1; sumLinesCount += linesCount;
			 * int optimalWidth = parWidthWithAdding / linesCount; this.maxWidth
			 * = Math.max(this.maxWidth, optimalWidth); }
			 * 
			 * Log.d(Log.topicRenderTag, "optimal maxWidth=" + this.maxWidth);
			 * Log.d(Log.topicRenderTag, "optimal lines count=" +
			 * sumLinesCount);
			 * 
			 * textToDraw = new FormattedText(); for (int i = 0; i <
			 * text.getTextParagraphs().size(); i++) { TextParagraph paragraph =
			 * text.getTextParagraphs().get(i); TextParagraph curParagraph = new
			 * TextParagraph(); int curLineWidth = 0; for (int j = 0; j <
			 * paragraph.getTextBlocks().size(); j++) { TextBlock block =
			 * paragraph.getTextBlocks().get(j);
			 * paint.setTextSize(block.getFormat().getFontSize()); while (true)
			 * { float blockWidth = paint.measureText(block.getText()); if
			 * (curLineWidth + blockWidth <= this.maxWidth) {
			 * curParagraph.add(block); curLineWidth += blockWidth; break; }
			 * else { // int fitInCount = block.getText().length(); // do { //
			 * float fitInPart = (this.maxWidth - curLineWidth) // / //
			 * blockWidth; // fitInCount = (int) (fitInCount * fitInPart); //
			 * blockWidth = paint.measureText(block.getText(), // 0, //
			 * fitInCount); // } while (curLineWidth + blockWidth > //
			 * this.maxWidth);
			 * 
			 * int fitInCount = paint.breakText(block.getText(), true,
			 * this.maxWidth - curLineWidth, null); TextBlock[] blocks =
			 * block.split(fitInCount); curParagraph.add(blocks[0]);
			 * textToDraw.add(curParagraph); curParagraph = new TextParagraph();
			 * block = blocks[1]; } } } textToDraw.add(curParagraph); }
			 * 
			 * recalcDrawingData();
			 */
		}
	}

	public TextBlock[] splitTextBlockByWidth(TextBlock block, int width) {
		TextBlock[] blocks;

		int fitInCount = paint.breakText(block.getText(), true, width, null);
		int splitCount = fitInCount;
		if (block.getText().charAt(fitInCount) == ' ') {
			blocks = new TextBlock[2];
			blocks[0] = block.split(fitInCount)[0];
			while (splitCount < block.getText().length() && block.getText().charAt(splitCount) == ' ')
				splitCount++;
			blocks[1] = block.split(splitCount)[1];
		} else {
			splitCount = block.getText().lastIndexOf(' ', fitInCount);
			if (splitCount == -1)
				splitCount = fitInCount;
			blocks = block.split(splitCount);
		}

		return blocks;
	}

	public void setMaxWidthAndLinesCount(int maxWidth, int linesCount) {
		if (!isEmpty && linesCount > 0) {
			Log.d(Log.topicRenderTag, "setting maxWidth=" + maxWidth + " linesCount=" + linesCount + " in " + this);

			maxWidth = Math.max(maxWidth, MIN_MAX_WIDTH);

			// calc parLinesCount
			int sumLinesCount = 0;
			int[] parLinesCount = new int[text.getTextParagraphs().size()];
			for (int i = 0; i < text.getTextParagraphs().size(); i++) {
				TextParagraph paragraph = text.getTextParagraphs().get(i);
				parLinesCount[i] = 1;

				int curLineWidth = 0;
				for (int j = 0; j < paragraph.getTextBlocks().size(); j++) {
					TextBlock block = paragraph.getTextBlocks().get(j);
					paint.setTextSize(block.getFormat().getFontSize());
					while (true) {
						float blockWidth = paint.measureText(block.getText());
						if (curLineWidth + blockWidth <= maxWidth) {
							curLineWidth += blockWidth;
							break;
						} else {
							TextBlock[] blocks = splitTextBlockByWidth(block, maxWidth - curLineWidth);
							block = blocks[1];

							curLineWidth = 0;
							parLinesCount[i]++;
						}
					}
				}

				sumLinesCount += parLinesCount[i];
			}

			// calc curMaxWidth
			boolean fitIn;
			int curMaxWidth = 0;
			if (sumLinesCount <= linesCount) {
				for (int i = 0; i < text.getTextParagraphs().size(); i++) {
					int parWidthWithAdding = parsWidth[i] + parLinesCount[i] * ADD_WIDTH;
					curMaxWidth = Math.max(curMaxWidth, parWidthWithAdding / parLinesCount[i]);
				}
				fitIn = true;
			} else {
				curMaxWidth = maxWidth;
				fitIn = false;
			}
			curMaxWidth = Math.max(curMaxWidth, MIN_MAX_WIDTH);

			// construct textToDraw
			textToDraw = new FormattedText();
			int curLineNumber = 1;
			boolean finish = false;
			for (int i = 0; i < text.getTextParagraphs().size(); i++) {
				if (finish) {
					break;
				}
				TextParagraph paragraph = text.getTextParagraphs().get(i);
				TextParagraph paragraphToDraw = new TextParagraph();
				int curParLineNumber = 1;
				int curLineWidth = 0;
				for (int j = 0; j < paragraph.getTextBlocks().size(); j++) {
					if (finish) {
						break;
					}
					TextBlock block = paragraph.getTextBlocks().get(j);
					paint.setTextSize(block.getFormat().getFontSize());
					while (true) {
						if (curLineNumber > linesCount) {
							finish = true;
							break;
						}

						if (!fitIn && curLineNumber == linesCount) {
							curMaxWidth -= THREE_DOTS_WIDTH;
						}

						float blockWidth = paint.measureText(block.getText());
						if (curLineWidth + blockWidth <= curMaxWidth) {
							curLineWidth += blockWidth;
							paragraphToDraw.add(block);
							break;
						} else {
							TextBlock[] blocks = splitTextBlockByWidth(block, curMaxWidth - curLineWidth);
							paragraphToDraw.add(blocks[0]);
							block = blocks[1];

							if (!fitIn && curLineNumber == linesCount) {
								paragraphToDraw.add(THREE_DOTS);
							}

							textToDraw.add(paragraphToDraw);
							paragraphToDraw = new TextParagraph();
							curLineWidth = 0;
							curLineNumber++;
							curParLineNumber++;
						}
					}
				}
				if (!finish) {
					textToDraw.add(paragraphToDraw);
				}
			}

			recalcDrawingData();
		}
	}

	@Override
	public void draw(int x, int y, int width, int height, Canvas c) {
		if (!isEmpty) {
			if (bgColor != 0) {
				paint.setColor(bgColor);
				paint.setAlpha(255);
				c.drawRect(x, y, x + getWidth(), y + getHeight(), paint);
			}

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

	public String getSimpleText() {
		return (textToDraw != null) ? textToDraw.getSimpleText() : "";
	}

	public boolean isEmpty() {
		return isEmpty;
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

			Log.d(Log.topicRenderTag, "Touch " + touchPoint + " on " + this);

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

			paint.setTextSize(paragraph.getMaxFontSize());
			int baseline = height + (int) (-paint.ascent());
			int parHeight = (int) (-paint.ascent() + paint.descent());

			int curWidth = 0;
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
