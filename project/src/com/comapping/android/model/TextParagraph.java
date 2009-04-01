package com.comapping.android.model;

import java.util.List;

public class TextParagraph {
	private List<TextBlock> textBlocks;
	private int maxFontSize;
	private String simpleText;

	public TextParagraph(List<TextBlock> textBlocks) {
		this.textBlocks = textBlocks;

		StringBuilder text = new StringBuilder();
		for (TextBlock cur : textBlocks) {
			maxFontSize = Math.max(maxFontSize, cur.getFormat().getFontSize());
			text.append(cur.getText());
		}
		this.simpleText = text.toString();
	}

	public int getMaxFontSize() {
		return maxFontSize;
	}

	public String getSimpleText() {
		return simpleText;
	}

	public List<TextBlock> getTextBlocks() {
		return textBlocks;
	}
}
