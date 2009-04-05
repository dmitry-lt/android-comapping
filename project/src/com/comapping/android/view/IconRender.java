package com.comapping.android.view;

import com.comapping.android.controller.MetaMapActivity;
import com.comapping.android.controller.R;
import com.comapping.android.model.Icon;
import com.comapping.android.model.Topic;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

public class IconRender extends Render {

	private static final int ICON_SIZE = 36;
	private static final int HORISONTAL_MERGING = 5;

	private Topic topic;
	private int width, height;
	
	private static boolean iconsLoaded = false;

	private static Bitmap[] priorityIcons;
	private static Bitmap[] smileyIcons;
	private static Bitmap[] taskCompletionIcons;
	private static Bitmap[] flagIcons;
	private static Bitmap[] icons;
	
	private static Bitmap getBitmap(Drawable image) {
		Bitmap bitmap = Bitmap.createBitmap(ICON_SIZE, ICON_SIZE, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		image.setBounds(0, 0, ICON_SIZE, ICON_SIZE);
		image.draw(canvas);
		return bitmap;
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

	public IconRender(Topic topic) {
		if (!iconsLoaded) {
			loadIcons();
			iconsLoaded = true;
		}

		this.topic = topic;

		int iconsCount = 0;
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
			width = ICON_SIZE * iconsCount + HORISONTAL_MERGING * (iconsCount - 1);
			height = ICON_SIZE;
		} else {
			width = 0;
			height = 0;
		}
	}

	@Override
	public void draw(int x, int y, int width, int height, Canvas c) {

		if (topic.getPriority() >= 1 && topic.getPriority() <= 9) {
			c.drawBitmap(priorityIcons[topic.getPriority()], x, y, null);
			x += ICON_SIZE + HORISONTAL_MERGING;
		}

		if (topic.getSmiley() != null) {
			c.drawBitmap(smileyIcons[topic.getSmiley().ordinal()], x, y, null);
			x += ICON_SIZE + HORISONTAL_MERGING;
		}

		if (topic.getTaskCompletion() != null) {
			c.drawBitmap(taskCompletionIcons[topic.getTaskCompletion().ordinal()], x, y, null);
			x += ICON_SIZE + HORISONTAL_MERGING;
		}

		if (topic.getFlag() != null) {
			c.drawBitmap(flagIcons[topic.getFlag().ordinal()], x, y, null);
			x += ICON_SIZE + HORISONTAL_MERGING;
		}

		for (Icon icon : topic.getIcons()) {
			c.drawBitmap(icons[icon.ordinal()], x, y, null);
			x += ICON_SIZE + HORISONTAL_MERGING;
		}
	}
	
	@Override
	public String toString() {
		return "[IconRender: width=" + getWidth() + " height=" + getHeight() + "]";
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
