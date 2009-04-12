package com.comapping.android.view;

import com.comapping.android.Log;
import com.comapping.android.model.FormattedText;
import com.comapping.android.model.Task;
import com.comapping.android.model.TextFormat;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;

public class TaskRender extends Render {
	private static final int MIN_MERGING = 5;

	private boolean isEmpty;

	private TextRender responsibleRender;
	private TextRender startRender;
	private TextRender deadlineRender;

	private Point responsibleCoords;
	private Point startCoords;
	private Point deadlineCoords;

	private int height, width;
	private int merging = MIN_MERGING;
	private int linesCount = 1;

	private AlertDialog infDialog;

	public TaskRender(Task task, Context context) {
		if (task != null) {
			isEmpty = false;
		} else {
			isEmpty = true;
		}

		if (!isEmpty) {
			TextFormat format = new TextFormat(10, Color.GRAY, "", false);

			FormattedText responsible = new FormattedText(task.getResponsible(), format);
			responsibleRender = new TextRender(responsible, context);

			FormattedText start = new FormattedText("Start: " + task.getStart(), format);
			startRender = new TextRender(start, context);

			FormattedText deadline = new FormattedText("Deadline: " + task.getDeadline(), format);
			deadlineRender = new TextRender(deadline, context);

			recalcDrawingData();

			infDialog = (new AlertDialog.Builder(context)
			.setTitle("Task")
			.setMessage(task.getResponsible() + "\n" + "Start: " + task.getStart() + "\n" + "Deadline: " + task.getDeadline())
			.setNeutralButton("Ok", null)
			).create();
		} else {
			height = 0;
			width = 0;
		}
	}

	public void setWidth(int width) {
		if (!isEmpty) {
			Log.d(Log.topicRenderTag, "setting width=" + width + " in " + this);

			responsibleRender.setMaxWidth(width);
			startRender.setMaxWidth(width);
			deadlineRender.setMaxWidth(width);
			int sumWidth = responsibleRender.getWidth() + startRender.getWidth() + deadlineRender.getWidth();
			if (sumWidth + MIN_MERGING * 2 <= width) {
				linesCount = 1;
				merging = (width - sumWidth) / 2;
			} else if (startRender.getWidth() + deadlineRender.getWidth() + MIN_MERGING <= width) {
				linesCount = 2;
				merging = width - startRender.getWidth() + deadlineRender.getWidth();
			} else {
				linesCount = 3;
			}
			recalcDrawingData();
		}
	}

	@Override
	public void draw(int x, int y, int width, int height, Canvas c) {
		if (!isEmpty) {
			responsibleRender.draw(x + responsibleCoords.x, y + responsibleCoords.y, 0, 0, c);
			startRender.draw(x + startCoords.x, y + startCoords.y, 0, 0, c);
			deadlineRender.draw(x + deadlineCoords.x, y + deadlineCoords.y, 0, 0, c);
		} else {
			// nothing to draw
		}
	}

	@Override
	public String toString() {
		if (!isEmpty) {
			return "[TaskRender: width=" + getWidth() + " height=" + getHeight() + " responsible=" + responsibleRender
					+ " start=" + startRender + " deadline=" + deadlineRender + "]";
		} else {
			return "[TaskRender: EMPTY]";
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

	private void recalcDrawingData() {
		switch (linesCount) {
		case 1: // 1: resp start deadline
			height = responsibleRender.getHeight();
			width = responsibleRender.getWidth() + startRender.getWidth() + deadlineRender.getWidth() + merging * 2;

			responsibleCoords = new Point(0, 0);
			startCoords = new Point(responsibleRender.getWidth() + merging, 0);
			deadlineCoords = new Point(width - deadlineRender.getWidth(), 0);
			break;
		case 2: // 1: resp 2: start deadline
			height = responsibleRender.getHeight() + startRender.getHeight();
			width = Math
					.max(responsibleRender.getWidth(), startRender.getWidth() + deadlineRender.getWidth() + merging);

			responsibleCoords = new Point(0, 0);
			startCoords = new Point(0, responsibleRender.getHeight());
			deadlineCoords = new Point(width - deadlineRender.getWidth(), responsibleRender.getHeight());
			break;
		case 3: // 1: resp 2: start 3: deadline
			height = responsibleRender.getHeight() + startRender.getHeight() + deadlineRender.getHeight();
			width = Math.max(responsibleRender.getWidth(), startRender.getWidth());
			width = Math.max(width, deadlineRender.getWidth());

			responsibleCoords = new Point(0, 0);
			startCoords = new Point(0, responsibleRender.getHeight());
			deadlineCoords = new Point(0, responsibleRender.getHeight() + startRender.getHeight());
			break;
		}

	}
}
