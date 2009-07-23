package com.comapping.android.map.model.text;

import java.io.Serializable;

public class TextBlock implements Serializable {
	private static final long serialVersionUID = -155561696789833530L;

	private String text;
	private TextFormat format;

	public TextBlock(String text, TextFormat format) {
		setText(text);
		setFormat(format);
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = processHtmlString((text != null) ? text : "");
	}

	public void setFormat(TextFormat format) {
		this.format = (format != null) ? format : new TextFormat();
	}

	public TextFormat getFormat() {
		return format;
	}

	/**
	 * @param count
	 *            of chars in first part
	 * @return array of two TextBlocks, which have format of current TextBlock
	 *         and text of first TextBlock is substring of current TextBlock
	 *         from 0 to count-1 and second from count to the end respectively
	 */
	public TextBlock[] split(int count) {
		TextBlock[] blocks = new TextBlock[2];

		if (count > 0) {
			blocks[0] = new TextBlock(getText().substring(0, count), getFormat().clone());
		} else {
			blocks[0] = new TextBlock("", getFormat().clone());
		}

		if (count < getText().length()) {
			blocks[1] = new TextBlock(getText().substring(count, getText().length()), getFormat().clone());
		} else {
			blocks[1] = new TextBlock("", getFormat().clone());
		}
	
//		// debug checking
//		if (!getText().equals(blocks[0].getText() + blocks[1].getText())) {
//			Log.w(Log.modelTag, "TextBlock: wrong split, count=" + count + ", all text=" + getText() + "\n text[0]="
//					+ blocks[0].getText() + "\n text[1]=" + blocks[1].getText());
//		}

		return blocks;
	}
	
	
	public String toString() {
		return getText();
	}
	
	public static String processHtmlString(String htmlString)
	{
		String res = htmlString
			.replace("&gt;", ">")
			.replace("&lt;", "<")
			.replace("&apos;", "\"")
			.replace("&quot;", "'")
			.replace("&amp;", "&");
		
		return res;
	}
}
