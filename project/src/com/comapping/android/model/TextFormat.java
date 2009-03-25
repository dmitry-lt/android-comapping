package com.comapping.android.model;

public class TextFormat implements Cloneable {

	private int fontSize;
	private int fontColor;
	private String hRef;
	private boolean underlined;

	public int getFontSize() {
		return fontSize;
	}

	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	public int getFontColor() {
		return fontColor;
	}

	public void setFontColor(int fontColor) {
		this.fontColor = fontColor;
	}

	public String getHRef() {
		return hRef;
	}

	public void setHRef(String ref) {
		hRef = ref;
	}

	public boolean isUnderlined() {
		return underlined;
	}

	public void setUnderlined(boolean underlined) {
		this.underlined = underlined;
	}

	public TextFormat clone() {
		TextFormat format = new TextFormat();

		format.fontColor = this.fontColor;
		format.fontSize = this.fontSize;
		format.hRef = this.hRef;
		format.underlined = this.underlined;

		return format;
	}
}
