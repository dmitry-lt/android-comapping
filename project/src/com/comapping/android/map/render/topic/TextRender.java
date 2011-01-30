package com.comapping.android.map.render.topic;

import com.comapping.android.map.model.text.FormattedText;
import com.comapping.android.map.model.text.TextBlock;
import com.comapping.android.map.model.text.TextFormat;
import com.comapping.android.map.model.text.TextParagraph;
import com.comapping.android.map.render.Render;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import static com.comapping.android.map.render.topic.RenderHelper.pointLiesOnRect;

public class TextRender extends Render {
	public static final int DEFAULT_BORDER = 4;
	private static final int MIN_MAX_WIDTH = 100;
	// private static final int MIN_MAX_WIDTH = calcWidth(new TextBlock("�", new
	// TextFormat(22, 0, "", false)));

	private boolean isEmpty;

	private FormattedText text;
	private FormattedText textToDraw;
	private int bgColor;
	private Paint paint;
	private int width, height;

	private int leftBorder;
	private int topBorder;
	private int rightBorder;
	private int bottomBorder;

	private Point[][] blocksDrawCoord; // y is a baseline of text, it is'n
	// upper-left corner
	private Rect[][] blocksRect;

	private Context context;

	public TextRender(FormattedText text, Context context) {
		this(text, Color.TRANSPARENT, context);
	}

	public TextRender(FormattedText text, int bgColor, Context context) {
		isEmpty = (text == null || text.getSimpleText().equals(""));

		if (!isEmpty) {
			this.text = text;
			this.bgColor = bgColor;
			this.context = context;

			paint = new Paint();
			paint.setAntiAlias(true);
			paint.setSubpixelText(true);

			textToDraw = text;
			setBorder(DEFAULT_BORDER, DEFAULT_BORDER, DEFAULT_BORDER, DEFAULT_BORDER);
			recalcDrawingData();
		} else {
			width = 0;
			height = 0;
		}

	}

	public void setBorder(int leftBorder, int topBorder, int rightBorder, int bottomBorder) {
		if (!isEmpty) {
			this.leftBorder = leftBorder;
			this.topBorder = topBorder;
			this.rightBorder = rightBorder;
			this.bottomBorder = bottomBorder;

			recalcDrawingData();
		}
	}

	public void setMaxWidth(int maxWidth) {
		if (!isEmpty) {
			//Log.d(Log.TOPIC_RENDER_TAG, "setting maxWidth=" + maxWidth + " in " + this);

			setMaxWidthAndLinesCount(maxWidth, Integer.MAX_VALUE);
		}
	}

	private boolean substrConsistOf(String s, int start, int end, char c) {
		for (int i = start; i < end; i++) {
			if (s.charAt(i) != c) {
				return false;
			}
		}
		return true;
	}

	private TextBlock[] splitDeletingSpaces(TextBlock block, int splitCount) {
		TextBlock[] blocks = new TextBlock[2];

		int leftSplitCount = splitCount;
		while (leftSplitCount > 0 && block.getText().charAt(leftSplitCount - 1) == ' ') {
			leftSplitCount--;
		}
		blocks[0] = block.split(leftSplitCount)[0];

		int rightSplitCount = splitCount;
		while (rightSplitCount < block.getText().length() && block.getText().charAt(rightSplitCount) == ' ') {
			rightSplitCount++;
		}
		blocks[1] = block.split(rightSplitCount)[1];

		return blocks;
	}

	private TextBlock[] splitTextBlockByWidth(TextBlock block, int width, boolean isFirst) {
		TextBlock[] blocks;

		int fitInCount = paint.breakText(block.getText(), true, width, null);
		if (fitInCount == block.getText().length()) {
			blocks = new TextBlock[2];
			blocks[0] = block;
			blocks[1] = new TextBlock("", block.getFormat().clone());
		} else if (block.getText().charAt(fitInCount) == ' ' || fitInCount == 0
				|| block.getText().charAt(fitInCount - 1) == ' ') {
			blocks = splitDeletingSpaces(block, fitInCount);
		} else {
			int splitCount;
			splitCount = block.getText().lastIndexOf(' ', fitInCount - 1);
			if (isFirst && (splitCount == -1 || substrConsistOf(block.getText(), 0, splitCount, ' '))) {
				blocks = block.split(fitInCount);
			} else {
				blocks = splitDeletingSpaces(block, splitCount + 1);
			}
		}

		return blocks;
	}

	private int measureWidth(TextBlock block) {
		paint.setTextSize(block.getFormat().getFontSize());
		return (int) paint.measureText(block.getText());
	}

	public void setMaxWidthAndLinesCount(int maxWidth, int linesCount) {
		if (!isEmpty && linesCount > 0) {
			//Log.d(Log.TOPIC_RENDER_TAG, "setting maxWidth=" + maxWidth + " linesCount=" + linesCount + " in " + this);

			maxWidth -= leftBorder + rightBorder;

			maxWidth = Math.max(maxWidth, MIN_MAX_WIDTH);

			int curMaxWidth = maxWidth;

			// construct textToDraw
			textToDraw = new FormattedText();
			int curLineNumber = 0;
			int lastLineWidth = 0;
			boolean fitIn = true;
			boolean finish = false;
			for (int i = 0; i < text.getTextParagraphs().size(); i++) {
				if (finish) {
					break;
				}

				TextParagraph paragraph = text.getTextParagraphs().get(i);
				TextParagraph paragraphToDraw = new TextParagraph();
				int curParLineNumber = 1;
				curLineNumber++;
				int curLineWidth = 0;

				for (int j = 0; j < paragraph.getTextBlocks().size(); j++) {
					if (finish) {
						break;
					}

					TextBlock block = paragraph.getTextBlocks().get(j);
					paint.setTextSize(block.getFormat().getFontSize());

					while (true) {
						if (curLineNumber > linesCount) {
							fitIn = false;
							finish = true;
							break;
						}

						int blockWidth = (int) paint.measureText(block.getText());
						if (curLineWidth + blockWidth <= curMaxWidth) {
							curLineWidth += blockWidth;
							paragraphToDraw.add(block);
							break;
						} else {
							TextBlock[] blocks = splitTextBlockByWidth(block, curMaxWidth - curLineWidth,
									curLineWidth == 0);
							paragraphToDraw.add(blocks[0]);
							block = blocks[1];

							if (curLineNumber == linesCount) {
								lastLineWidth = curLineWidth + (int) (paint.measureText(blocks[0].getText()));
							}

							textToDraw.add(paragraphToDraw);
							paragraphToDraw = new TextParagraph();
							curParLineNumber++;
							curLineNumber++;
							curLineWidth = 0;
						}
					}
				}

				if (!finish) {
					textToDraw.add(paragraphToDraw);
				}
				if (curLineNumber == linesCount) {
					lastLineWidth = curLineWidth;
				}
			}

			if (!fitIn) {
				addThreeDots(textToDraw, lastLineWidth, curMaxWidth);
			}

			recalcDrawingData();
		}
	}

	private void addThreeDots(FormattedText formattedText, int lastLineWidth, int maxLineWidth) {
		// checking data validness
		if (formattedText == null || lastLineWidth > maxLineWidth || maxLineWidth < MIN_MAX_WIDTH) {
			//Log.d(Log.TOPIC_RENDER_TAG, "Cannot add three dots, invalide args:\n" + formattedText + "\nlastLineWidth="
			//		+ lastLineWidth + " maxLineWidth=" + maxLineWidth);
			return;
		}

		TextBlock threeDots = new TextBlock("...", new TextFormat());

		if (formattedText.getTextParagraphs().size() == 0) {
			formattedText.add(new TextParagraph(threeDots));
			return;
		}

		if (formattedText.getLast().getSimpleText().equals("")) {
			formattedText.getLast().add(threeDots);
			formattedText.update();
			return;
		}

		TextParagraph lastParagraph = formattedText.getLast();

		// add fake block
		TextBlock lastRemoved = new TextBlock("", lastParagraph.getLast().getFormat());
		threeDots.setFormat(lastRemoved.getFormat());
		while (true) {
			if (lastLineWidth + measureWidth(threeDots) <= maxLineWidth) {
				int splitWidth = maxLineWidth - lastLineWidth - measureWidth(threeDots);
				TextBlock lastBlock = splitTextBlockByWidth(lastRemoved, splitWidth, true)[0];
				if (!lastBlock.getText().equals("")) {
					lastParagraph.add(lastBlock);
					lastParagraph.add(threeDots);
					break;
				}
			}

			lastRemoved = lastParagraph.removeLast();
			lastLineWidth -= measureWidth(lastRemoved);
			threeDots.setFormat(lastRemoved.getFormat());
		}

		formattedText.update();
	}

	@Override
	public void draw(int x, int y, int width, int height, Canvas c) {
		if (!isEmpty) {
			if (bgColor != 0) {
				paint.setColor(bgColor);
				paint.setAlpha(255);
				c.drawRect(x, y, x + getWidth(), y + getHeight(), paint);
			}

			x += leftBorder;
			y += topBorder;

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
	public boolean onTouch(int x, int y) {
		if (!isEmpty) {
			Point touchPoint = new Point(x, y);

			//Log.d(Log.TOPIC_RENDER_TAG, "Touch " + touchPoint + " on " + this);

			for (int i = 0; i < textToDraw.getTextParagraphs().size(); i++) {
				TextParagraph paragraph = textToDraw.getTextParagraphs().get(i);
				for (int j = 0; j < paragraph.getTextBlocks().size(); j++) {
					if (pointLiesOnRect(touchPoint, blocksRect[i][j])) {
						//Log.d(Log.TOPIC_RENDER_TAG, "Touch on " + paragraph.getTextBlocks().get(j));

						String url = paragraph.getTextBlocks().get(j).getFormat().getHRef();
						if (!url.equals("")) {
							context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
						}

						return false;
					}
				}
			}
		}
		return false;
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

		width += leftBorder + rightBorder;
		height += topBorder + bottomBorder;
	}
}
