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
		this.text = text;
	}

	public void setFormat(TextFormat format) {
		this.format = format;
	}

	public TextFormat getFormat() {
		return format;
	}
}
