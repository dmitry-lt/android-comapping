package com.comapping.android.view.topic;

import com.comapping.android.Log;
import com.comapping.android.controller.MetaMapActivity;
import com.comapping.android.controller.R;
import com.comapping.android.model.map.Icon;
import com.comapping.android.model.map.Topic;
import com.comapping.android.view.Render;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

public class IconRender extends Render {

	private static final int ICON_SIZE = 30;
	private static final int HORISONTAL_MERGING = 0;
	private static final int VERTICAL_MERGING = 5;

	private static boolean iconsLoaded = false;

	private boolean isEmpty;

	private static Bitmap[] priorityIcons;
	private static Bitmap[] smileyIcons;
	private static Bitmap[] taskCompletionIcons;
	private static Bitmap[] flagIcons;
	private static Bitmap[] icons;

	private Topic topic;
	private int iconsCount;
	private int width, height;

	public IconRender(Topic topic) {
		if (!iconsLoaded) {
			loadIcons();
			iconsLoaded = true;
		}

		this.topic = topic;

		iconsCount = 0;
		if (topic.getPriority() != 0) {
			iconsCount++;
		}
		if (topic.getSmiley() != null) {
			iconsCount++;
		}
		if (topic.getTaskCompletion() != null) {
			iconsCount++;
		}
		if (topic.getFlag() != null) {
			iconsCount++;
		}
		iconsCount += topic.getIconCount();

		if (iconsCount > 0) {
			isEmpty = false;
		} else {
			isEmpty = true;
		}

		if (!isEmpty) {
			width = ICON_SIZE * iconsCount + HORISONTAL_MERGING * (iconsCount - 1);
			height = ICON_SIZE;
		} else {
			width = 0;
			height = 0;
		}
	}

	@Override
	public void draw(int x, int y, int width, int height, Canvas c) {
		if (!isEmpty) {
			int curX = x;
			int curY = y;

			if (topic.getPriority() >= 1 && topic.getPriority() <= 9) {
				c.drawBitmap(priorityIcons[topic.getPriority()], curX, curY, null);
				curX += ICON_SIZE + HORISONTAL_MERGING;
			}

			if (curX - x + ICON_SIZE > this.width) {
				curX = x;
				curY += ICON_SIZE + VERTICAL_MERGING;
			}

			if (topic.getSmiley() != null) {
				c.drawBitmap(smileyIcons[topic.getSmiley().ordinal()], curX, curY, null);
				curX += ICON_SIZE + HORISONTAL_MERGING;
			}

			if (curX - x + ICON_SIZE > this.width) {
				curX = x;
				curY += ICON_SIZE + VERTICAL_MERGING;
			}

			if (topic.getTaskCompletion() != null) {
				c.drawBitmap(taskCompletionIcons[topic.getTaskCompletion().ordinal()], curX, curY, null);
				curX += ICON_SIZE + HORISONTAL_MERGING;
			}

			if (curX - x + ICON_SIZE > this.width) {
				curX = x;
				curY += ICON_SIZE + VERTICAL_MERGING;
			}

			if (topic.getFlag() != null) {
				c.drawBitmap(flagIcons[topic.getFlag().ordinal()], curX, curY, null);
				curX += ICON_SIZE + HORISONTAL_MERGING;
			}

			for (Icon icon : topic.getIcons()) {
				if (curX - x + ICON_SIZE > this.width) {
					curX = x;
					curY += ICON_SIZE + VERTICAL_MERGING;
				}

				c.drawBitmap(icons[icon.ordinal()], curX, curY, null);
				curX += ICON_SIZE + HORISONTAL_MERGING;
			}
		}
	}

	@Override
	public String toString() {
		if (!isEmpty) {
			return "[IconRender: width=" + getWidth() + " height=" + getHeight() + "]";
		} else {
			return "[IconRender: EMPTY]";
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

	public void setMaxWidth(int maxWidth) {
		if (!isEmpty && maxWidth >= ICON_SIZE) {
			Log.d(Log.TOPIC_RENDER_TAG, "setting maxWidth=" + maxWidth + " in " + this);

			int iconsInLineCount = (maxWidth + HORISONTAL_MERGING) / (ICON_SIZE + HORISONTAL_MERGING);

			int linesCount = iconsCount / iconsInLineCount;
			linesCount += (iconsCount % iconsInLineCount) == 0 ? 0 : 1;

			iconsInLineCount = iconsCount / linesCount;
			iconsInLineCount += (iconsCount % linesCount) == 0 ? 0 : 1;

			this.width = (ICON_SIZE * iconsInLineCount) + (HORISONTAL_MERGING * (iconsInLineCount - 1));
			this.height = (ICON_SIZE * linesCount) + (VERTICAL_MERGING * (linesCount - 1));
		}
	}

	private static Bitmap getBitmap(Drawable image) {
		return RenderHelper.getBitmap(image, ICON_SIZE);
	}

	private void loadIcons() {
		Resources r = MetaMapActivity.getInstance().getResources();

		priorityIcons = new Bitmap[] { null, getBitmap(r.getDrawable(R.drawable.priority1)),
				getBitmap(r.getDrawable(R.drawable.priority2)), getBitmap(r.getDrawable(R.drawable.priority3)),
				getBitmap(r.getDrawable(R.drawable.priority4)), getBitmap(r.getDrawable(R.drawable.priority5)),
				getBitmap(r.getDrawable(R.drawable.priority6)), getBitmap(r.getDrawable(R.drawable.priority7)),
				getBitmap(r.getDrawable(R.drawable.priority8)), getBitmap(r.getDrawable(R.drawable.priority9)) };

		smileyIcons = new Bitmap[] { getBitmap(r.getDrawable(R.drawable.smiley_happy)),
				getBitmap(r.getDrawable(R.drawable.smiley_normal)), getBitmap(r.getDrawable(R.drawable.smiley_sad)),
				getBitmap(r.getDrawable(R.drawable.smiley_furious)) };

		taskCompletionIcons = new Bitmap[] { getBitmap(r.getDrawable(R.drawable.task_completion_todo)),
				getBitmap(r.getDrawable(R.drawable.task_completion_25)),
				getBitmap(r.getDrawable(R.drawable.task_completion_50)),
				getBitmap(r.getDrawable(R.drawable.task_completion_75)),
				getBitmap(r.getDrawable(R.drawable.task_completion_complete)) };

		flagIcons = new Bitmap[] { getBitmap(r.getDrawable(R.drawable.flag_go)),
				getBitmap(r.getDrawable(R.drawable.flag_for_discussion)),
				getBitmap(r.getDrawable(R.drawable.flag_possibility)), getBitmap(r.getDrawable(R.drawable.flag_risk)),
				getBitmap(r.getDrawable(R.drawable.flag_progress)), getBitmap(r.getDrawable(R.drawable.flag_carefull)),
				getBitmap(r.getDrawable(R.drawable.flag_caution)) };

		icons = new Bitmap[] { getBitmap(r.getDrawable(R.drawable.icon_question_mark)),
				getBitmap(r.getDrawable(R.drawable.icon_exclamation_mark)),
				getBitmap(r.getDrawable(R.drawable.icon_bomb)), getBitmap(r.getDrawable(R.drawable.icon_thumbs_up)),
				getBitmap(r.getDrawable(R.drawable.icon_thumbs_down)),
				getBitmap(r.getDrawable(R.drawable.icon_magnifier)), getBitmap(r.getDrawable(R.drawable.icon_dollar)),
				getBitmap(r.getDrawable(R.drawable.icon_heart)), getBitmap(r.getDrawable(R.drawable.icon_clock)) };
	}
}
