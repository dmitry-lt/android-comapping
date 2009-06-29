package com.comapping.android.view.topic;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;

import com.comapping.android.Log;
import com.comapping.android.model.text.FormattedText;
import com.comapping.android.model.text.TextFormat;
import com.comapping.android.view.Render;

public class NoteRender extends Render {
	private static final TextFormat FORMAT = new TextFormat(10, Color.GRAY, "", false);
	private static final int MIN_MAX_WIDTH = 100;
	private static final int MAX_LINES_COUNT = 2;

	private boolean isEmpty;

	private String note;
	private Context context;

	private TextRender textRender;

	private int width, height;
	private AlertDialog dialog;

	public NoteRender(String note, Context context) {
		isEmpty = (note == null || note.equals(""));

		if (!isEmpty) {
			this.note = note;
			this.context = context;
			
			FormattedText text = new FormattedText(note, FORMAT);
			textRender = new TextRender(text, context);
			recalcDrawingData();
		} else {

		}
	}

	
	public void draw(int x, int y, int width, int height, Canvas c) {
		if (!isEmpty) {
			textRender.draw(x, y, width, height, c);
		} else {
			// nothing to draw
		}
	}

	
	public int getHeight() {
		return height;
	}

	
	public int getWidth() {
		return width;
	}

	
	public void onTouch(int x, int y) {
		if (!isEmpty) {
			if (dialog == null) {
				dialog = (new AlertDialog.Builder(context)
				.setTitle("Note")
				.setMessage(note)
				.setNeutralButton("Ok", null)
				).create();
			}

			dialog.show();
		}
	}

	public void setMaxWidth(int maxWidth) {
		if (!isEmpty) {
			Log.d(Log.TOPIC_RENDER_TAG, "setting maxWidth=" + maxWidth + " in " + this);
			textRender.setMaxWidthAndLinesCount(Math.max(maxWidth, MIN_MAX_WIDTH), MAX_LINES_COUNT);
			recalcDrawingData();
		}
	}

	
	public String toString() {
		if (!isEmpty) {
			return "[NoteRender: note=\"" + textRender.getSimpleText() + "\" width=" + getWidth() + " height="
					+ getHeight() + "]";
		} else {
			return "[NoteRender: EMPTY]";
		}
	}

	private void recalcDrawingData() {
		width = textRender.getWidth();
		height = textRender.getHeight();
	}
}
