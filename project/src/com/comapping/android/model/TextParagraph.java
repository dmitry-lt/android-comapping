package com.comapping.android.model;

import java.util.ArrayList;
import java.util.List;

public class TextParagraph {
	private List<TextBlock> textBlocks;
	private int maxFontSize;
	private String simpleText;

	public TextParagraph(String text, TextFormat format) {		
		this.textBlocks = new ArrayList<TextBlock>();
		add(new TextBlock(text, format));
	}
	
	public TextParagraph(List<TextBlock> textBlocks) {
		this.textBlocks = textBlocks;
		update();
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

	public void add(TextBlock block) {
		textBlocks.add(block);
		update();
	}
	
	private void update() {
		StringBuilder text = new StringBuilder();
		for (TextBlock cur : textBlocks) {
			maxFontSize = Math.max(maxFontSize, cur.getFormat().getFontSize());
			text.append(cur.getText());
		}
		this.simpleText = text.toString();
	}
}
