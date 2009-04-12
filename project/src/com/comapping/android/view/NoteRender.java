package com.comapping.android.view;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;

import com.comapping.android.Log;
import com.comapping.android.model.FormattedText;
import com.comapping.android.model.TextFormat;

public class NoteRender extends Render {
	private boolean isEmpty;

	private TextRender textRender;

	private int width, height;
	private AlertDialog infDialog;

	public NoteRender(String note, Context context) {
		if (note != null && !note.equals("")) {
			isEmpty = false;
		} else {
			isEmpty = true;
		}

		if (!isEmpty) {
			FormattedText text = new FormattedText(note, new TextFormat(10, Color.GRAY, "", false));
			textRender = new TextRender(text, context);
			recalcDrawingData();

			infDialog = (new AlertDialog.Builder(context)
			.setTitle("Note")
			.setMessage(note)
			.setNeutralButton("Ok", null)
			).create();
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
		infDialog.show();
	}

	public void setMaxWidth(int maxWidth) {
		if (!isEmpty) {
			Log.d(Log.topicRenderTag, "setting maxWidth=" + maxWidth + " in " + this);
			textRender.setMaxWidth(maxWidth);
			recalcDrawingData();
		}
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

	private void recalcDrawingData() {
		width = textRender.getWidth();
		height = textRender.getHeight();
	}
}
