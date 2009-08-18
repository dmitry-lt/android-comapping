package com.comapping.android.map.model.text;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TextParagraph implements Serializable {
	private static final long serialVersionUID = 150495793618162768L;

	private List<TextBlock> textBlocks;
	private int maxFontSize;
	private String simpleText;

	public TextParagraph() {
		this(new ArrayList<TextBlock>());
	}

	public TextParagraph(String text, TextFormat format) {
		this(new TextBlock(text, format));
	}
	
	public TextParagraph(TextBlock textBlock) {
		this(new ArrayList<TextBlock>());
		add(textBlock);
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
	
	public TextBlock getLast() {
		if (textBlocks.size() == 0)  {
			return null;
		}
		
		return textBlocks.get(textBlocks.size() - 1);
	}
	
	public TextBlock removeLast() {
		if (textBlocks.size() == 0) {
			return null;
		}
		
		TextBlock removed = textBlocks.remove(textBlocks.size() - 1);
		update();
		return removed;		
	}

	public void update() {
		StringBuilder text = new StringBuilder();
		maxFontSize = 0;
		for (TextBlock cur : textBlocks) {
			maxFontSize = Math.max(maxFontSize, cur.getFormat().getFontSize());
			text.append(cur.getText());
		}
		this.simpleText = text.toString();
	}
	
	
	public String toString() {
		return getSimpleText();
	}
	
	
}
