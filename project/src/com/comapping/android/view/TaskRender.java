package com.comapping.android.view;

import java.util.ArrayList;

import com.comapping.android.model.FormattedText;
import com.comapping.android.model.Task;
import com.comapping.android.model.TextFormat;
import com.comapping.android.model.TextParagraph;

import android.graphics.Canvas;
import android.graphics.Color;

public class TaskRender extends Render {
	private static final int MERGING = 5;

	private boolean isEmpty;

	private TextRender responsibleRender, deadlineRender;

	private int height, width;

	public TaskRender(Task task) {
		if (task != null) {
			isEmpty = false;
		} else {
			isEmpty = true;
		}

		if (!isEmpty) {
			TextFormat format = new TextFormat();
			format.setFontSize(10);
			format.setFontColor(Color.GRAY);
			format.setUnderlined(false);

			FormattedText resposible = new FormattedText(new ArrayList<TextParagraph>());
			resposible.add(new TextParagraph(task.getResponsible(), format));
			responsibleRender = new TextRender(resposible);

			FormattedText deadline = new FormattedText(new ArrayList<TextParagraph>());
			deadline.add(new TextParagraph("Deadline: " + task.getDeadline(), format));
			deadlineRender = new TextRender(deadline);

			height = Math.max(responsibleRender.getHeight(), deadlineRender.getHeight());
			width = responsibleRender.getWidth() + MERGING + deadlineRender.getWidth();
		} else {
			height = 0;
			width = 0;
		}
	}

	public void setWidth(int width) {
		if (this.width < width) {
			this.width = width;
		}
	}

	@Override
	public void draw(int x, int y, int width, int height, Canvas c) {
		if (!isEmpty) {
			responsibleRender.draw(x, y, width, height, c);
			int shift = getWidth() - deadlineRender.getWidth();
			deadlineRender.draw(x + shift, y, width - shift, height, c);
		} else {
			// nothing to draw
		}
	}

	@Override
	public String toString() {
		if (!isEmpty) {
			return "[TaskRender: width=" + getWidth() + " height=" + getHeight() + " responsible=" + responsibleRender
					+ " deadline=" + deadlineRender + "]";
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
		// TODO Auto-generated method stub

	}

}
