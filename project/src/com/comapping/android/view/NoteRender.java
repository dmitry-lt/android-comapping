package com.comapping.android.view;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;

import com.comapping.android.model.FormattedText;
import com.comapping.android.model.TextFormat;
import com.comapping.android.model.TextParagraph;

public class NoteRender extends Render {
	private boolean isEmpty;

	private TextRender textRender;

	private int width, height;

	public NoteRender(String note) {
		if (note != null && !note.equals("")) {
			isEmpty = false;
		} else {
			isEmpty = true;
		}

		if (!isEmpty) {
			TextFormat format = new TextFormat();
			format.setFontSize(10);
			format.setFontColor(Color.GRAY);
			format.setUnderlined(false);

			FormattedText text = new FormattedText(new ArrayList<TextParagraph>());
			text.add(new TextParagraph(note, format));

			textRender = new TextRender(text);

			width = textRender.getWidth();
			height = textRender.getHeight();
		} else {

		}
	}

	@Override
	public void draw(int x, int y, int width, int height, Canvas c) {
		if (!isEmpty) {
			textRender.draw(x, y, width, height, c);
		} else {
			// nothing to draw
		}
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public void onTouch(int x, int y) {
		// TODO Auto-generated method stub

	}

	@Override
	public String toString() {
		if (!isEmpty) {
			return "[NoteRender: note=\"" + textRender.getText() + "\" width=" + getWidth() + " height=" + getHeight()
					+ "]";
		} else {
			return "[NoteRender: EMPTY]";
		}
	}
}
