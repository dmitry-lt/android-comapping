package com.comapping.android.map.model.text;

import java.io.Serializable;

import android.graphics.Color;

public class TextFormat implements Cloneable, Serializable {
	private static final long serialVersionUID = 3663809688417285721L;

	private int fontSize = 16;
	private int fontColor = Color.BLACK;
	private String hRef = "";
	private boolean underlined = false;

	public TextFormat() {
	}

	public TextFormat(int fontSize, int fontColor, String hRef, boolean underlined) {
		setFontSize(fontSize);
		setFontColor(fontColor);
		setHRef(hRef);
		setUnderlined(underlined);
	}

	public boolean equals(TextFormat format) {
		return (this.fontColor == format.fontColor && this.fontSize == format.fontSize
				&& this.underlined == format.underlined && this.getHRef().equals(format.getHRef()));
	}

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

	public void setHRef(String hRef) {
		this.hRef = hRef == null ? "" : hRef;;
	}

	public boolean isUnderlined() {
		return underlined;
	}

	public void setUnderlined(boolean underlined) {
		this.underlined = underlined;
	}

	public TextFormat clone() {
		return new TextFormat(this.fontSize, this.fontColor, this.hRef, this.underlined);
	}
}
