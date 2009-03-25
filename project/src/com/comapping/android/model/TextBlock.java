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
		this.text = unescape(text.replace('#', '&'));
	}

	public void setFormat(TextFormat format) {
		this.format = format;
	}

	public TextFormat getFormat() {
		return format;
	}

	private String unescape(String str) {
		str = str.replace("&amp;", "&");
		str = str.replace("&lt;", "<");
		str = str.replace("&gt;", ">");
		str = str.replace("&#039;", "\\");
		str = str.replace("&#39;", "'");
		str = str.replace("&quot;", "\"");
		str = str.replace("&lt;", "<");
		str = str.replace("&apos;", "'");
		return str;
	}
}
