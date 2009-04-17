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
	private String simpleText;

	public FormattedText() {
		this(new ArrayList<TextParagraph>());
	}

	public FormattedText(String text, TextFormat format) {
		this();
		add(new TextParagraph(text, format));
	}

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
		for (int i = 0; i < textParagraphs.size(); i++) {
			TextParagraph cur = textParagraphs.get(i);
			if (i < textParagraphs.size() - 1) {
				text.append(cur.getSimpleText() + "\n");
			} else {
				text.append(cur.getSimpleText());
			}
		}
		this.simpleText = text.toString();
	}
}
