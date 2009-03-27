package com.comapping.android.model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents text, that can consist of differently formatted parts,
 * it contains list of TextParagraph, each of them consist of TextBlocks which
 * can have different format
 * 
 * @author Passichenko Victor
 * @author Yuri Zemlyanskiy
 * 
 */
public class FormattedText {

	private List<TextParagraph> textParagraphs;
	private List<TextBlock> textBlocks;
	private int maxFontSize;
	private String simpleText;

	public FormattedText(List<TextParagraph> textParagraphs) {
		this.textParagraphs = textParagraphs;

		textBlocks = new ArrayList<TextBlock>();
		StringBuilder text = new StringBuilder();
		for (TextParagraph cur : textParagraphs) {
			maxFontSize = Math.max(maxFontSize, cur.getMaxFontSize());
			text.append(cur.getSimpleText());
			textBlocks.addAll(cur.getTextBlocks());
		}
		this.simpleText = text.toString();
	}

	public int getMaxFontSize() {
		return maxFontSize;
	}

	public String getSimpleText() {
		return simpleText;
	}

	public List<TextParagraph> getTextParagraphs() {
		return textParagraphs;
	}

	public List<TextBlock> getTextBlocks() {
		return textBlocks;
	}
}
