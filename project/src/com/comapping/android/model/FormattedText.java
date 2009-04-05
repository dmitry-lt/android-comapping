package com.comapping.android.model;

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
	private String simpleText;

	public FormattedText(List<TextParagraph> textParagraphs) {
		this.textParagraphs = textParagraphs;
		update();
	}

	public String getSimpleText() {
		return simpleText;
	}

	public List<TextParagraph> getTextParagraphs() {
		return textParagraphs;
	}
	
	public void add(TextParagraph paragraph) {
		textParagraphs.add(paragraph);
		update();
	}
	
	private void update() {
		StringBuilder text = new StringBuilder();
		for (TextParagraph cur : textParagraphs) {
			text.append(cur.getSimpleText());
		}
		this.simpleText = text.toString();
	}
}
