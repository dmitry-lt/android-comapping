package com.comapping.android.map.render.topic;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;

import com.comapping.android.map.model.text.FormattedText;
import com.comapping.android.map.model.text.TextFormat;
import com.comapping.android.map.render.Render;

public class NoteRender extends Render {
	private static final TextFormat FORMAT = new TextFormat(10, Color.GRAY, "",	false);
	private static final int MIN_MAX_WIDTH = 100;
	private static final int MAX_LINES_COUNT = 2;

	private boolean isEmpty;

	private String note;
	private Context context;

	private TextRender textRender;

	private int width, height;
	private AlertDialog dialog;

	public NoteRender(String note, Context context) {

		this.context = context;

		setText(note);

	}

	public void draw(int x, int y, int width, int height, Canvas c) {
		if (!isEmpty) {
			textRender.draw(x, y, width, height, c);
		} else {
			// nothing to draw
		}
	}

	public String getNote() {
		return note;
	}
	
	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public boolean onTouch(int x, int y) {
		if (!isEmpty) {
			if (dialog == null) {
				dialog = new AlertDialog.Builder(context)
						.setTitle("Note").setMessage(note).setNeutralButton("Ok", null).create();
			}
			dialog.show();
			return true;
		}
		return false;
	}

	public void setMaxWidth(int maxWidth) {
		if (!isEmpty) {
			// Log.d(Log.TOPIC_RENDER_TAG, "setting maxWidth=" + maxWidth +
			// " in " + this);
			textRender.setMaxWidthAndLinesCount(Math.max(maxWidth,
					MIN_MAX_WIDTH), MAX_LINES_COUNT);
			recalcDrawingData();
		}
	}

	public String toString() {
		if (!isEmpty) {
			return "[NoteRender: note=\"" + textRender.getSimpleText()
					+ "\" width=" + getWidth() + " height=" + getHeight() + "]";
		} else {
			return "[NoteRender: EMPTY]";
		}
	}

	private void recalcDrawingData() {
		width = textRender.getWidth();
		height = textRender.getHeight();
	}

	private void setText(String note) {

		isEmpty = (note == null || note.equals(""));

		if (!isEmpty) {
			this.note = note;

			FormattedText text = new FormattedText(note, FORMAT);
			textRender = new TextRender(text, context);
			recalcDrawingData();
		} else {

		}
	}
}
