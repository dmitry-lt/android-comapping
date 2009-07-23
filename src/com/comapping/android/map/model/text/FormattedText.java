package com.comapping.android.map.model.text;

import java.io.Serializable;
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
public class FormattedText implements Serializable {
	private static final long serialVersionUID = -8707558569665486055L;

	private List<TextParagraph> textParagraphs;
	private String simpleText;

	public FormattedText() {
		this(new ArrayList<TextParagraph>());
	}

	public FormattedText(String text, TextFormat format) {
		this();
		int pos = 0;
		while (pos < text.length()) {
			int lineEndPos = text.indexOf('\n', pos);
			if (lineEndPos == -1) {
				lineEndPos = text.length();
			}
			add(new TextParagraph(text.substring(pos, lineEndPos), format));
			pos = lineEndPos + 1;
		}
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
	
	public TextParagraph getLast() {
		if (textParagraphs.size() == 0) {
			return null;
		} 
		
		return textParagraphs.get(textParagraphs.size() - 1);
	}

	public void update() {
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
	
	
	public String toString() {
		return getSimpleText();
	}
}
