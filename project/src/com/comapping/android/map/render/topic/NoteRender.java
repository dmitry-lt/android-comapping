package com.comapping.android.map.render.topic;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.comapping.android.controller.R;
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
				LayoutInflater factory = LayoutInflater.from(context);
				final View textEntryView = factory.inflate(R.layout.topic_edit,	null);
				
				((EditText) textEntryView.findViewById(R.id.topic_text_edit)).setText(note);
				((TextView) textEntryView.findViewById(R.id.topic_text_hint)).setText("Notes");
				
				dialog = new AlertDialog.Builder(context)
						.setView(textEntryView).setPositiveButton("Save",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,	int whichButton) {
										String note = ((EditText) textEntryView
												.findViewById(R.id.topic_text_edit))
												.getText().toString();

										setText(note);
									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,	int whichButton) {
										// nothing to do
									}
								}).create();
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
