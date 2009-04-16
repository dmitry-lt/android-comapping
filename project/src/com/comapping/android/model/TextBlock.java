package com.comapping.android.model;

public class TextBlock {
	private String text;
	private TextFormat format;

	public TextBlock(String text, TextFormat format) {
		setText(text);
		setFormat(format);
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = (text != null) ? text : "";
	}

	public void setFormat(TextFormat format) {
		this.format = (format != null) ? format : new TextFormat();
	}

	public TextFormat getFormat() {
		return format;
	}

	/**
	 * @param count
	 *            of chars in first part
	 * @return array of two TextBlocks, which have format of current TextBlock
	 *         and text of first TextBlock is substring of current TextBlock
	 *         from 0 to count-1 and second from count to the end respectively
	 */
	public TextBlock[] split(int count) {
		TextBlock[] blocks = new TextBlock[2];

		if (count > 0) {
			blocks[0] = new TextBlock(getText().substring(0, count - 1), getFormat().clone());
		} else {
			blocks[0] = new TextBlock("", getFormat().clone());
		}

		if (count < getText().length()) {
			blocks[1] = new TextBlock(getText().substring(count, getText().length() - 1), getFormat().clone());
		} else {
			blocks[1] = new TextBlock("", getFormat().clone());
		}

		return blocks;
	}
}
