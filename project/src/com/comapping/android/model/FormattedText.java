package com.comapping.android.model;

public class FormattedText {

	private TextBlock first;
	private int maxFontSize;

	public FormattedText(TextBlock first) {
		this.first = first;

		TextBlock cur = first;
		while (cur != null) {
			maxFontSize = Math.max(maxFontSize, cur.getFormat().getFontSize());
			cur = cur.getNext();
		}
	}

	public int getMaxFontSize() {
		return maxFontSize;
	}

	public String getSimpleText() {
		StringBuilder text = new StringBuilder();

		TextBlock cur = first;
		while (cur != null) {
			text.append(cur.getText());
			cur = cur.getNext();
		}

		return text.toString();
	}

	public TextBlock getFirst() {
		return first;
	}
}
