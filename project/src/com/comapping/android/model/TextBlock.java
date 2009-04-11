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

	public TextBlock[] split(int position) {
		TextBlock[] blocks = new TextBlock[2];
		blocks[0] = new TextBlock(getText().substring(0, position - 1), getFormat().clone());
		blocks[1] = new TextBlock(getText().substring(position, getText().length() - 1), getFormat().clone());
		return blocks;
	}
}
