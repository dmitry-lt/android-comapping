package com.comapping.android.map.render.topic;

import com.comapping.android.R;
import com.comapping.android.map.model.map.Icon;
import com.comapping.android.map.model.map.Topic;
import com.comapping.android.map.render.Render;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

public class IconRender extends Render {

	private static final int ICON_SIZE = 30;
	private static final int HORISONTAL_MERGING = 5;
	private static final int VERTICAL_MERGING = 5;

	private static boolean iconsLoaded = false;

	private boolean isEmpty;

	private static Bitmap[] priorityIcons;
	private static Bitmap[] smileyIcons;
	private static Bitmap[] taskCompletionIcons;
	private static Bitmap[] flagIcons;
	private static Bitmap[] icons;
	private static Bitmap[] arrowIcons;
	private static Bitmap[] starIcons;

	private Context context;
	
	private Topic topic;
	private int iconsCount;
	private int width, height;

	public IconRender(Topic topic, Context context) {		
		this.topic = topic;
		this.context = context;
		
		if (!iconsLoaded) {
			loadIcons();
			iconsLoaded = true;
		}

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
		
		if (topic.getArrow() != null) {
			iconsCount++;
		}

		if (topic.getStar() != null) {
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

			if (curX - x + ICON_SIZE > this.width) {
				curX = x;
				curY += ICON_SIZE + VERTICAL_MERGING;
			}
			
			if (topic.getStar() != null) {
				c.drawBitmap(starIcons[topic.getStar().ordinal()], curX, curY, null);
				curX += ICON_SIZE + HORISONTAL_MERGING;
			}
			
			if (curX - x + ICON_SIZE > this.width) {
				curX = x;
				curY += ICON_SIZE + VERTICAL_MERGING;
			}
			
			if (topic.getArrow() != null) {
				c.drawBitmap(arrowIcons[topic.getArrow().ordinal()], curX, curY, null);
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

	
	public String toString() {
		if (!isEmpty) {
			return "[IconRender: width=" + getWidth() + " height=" + getHeight() + "]";
		} else {
			return "[IconRender: EMPTY]";
		}
	}

	
	public int getHeight() {
		return height;
	}

	
	public int getWidth() {
		return width;
	}

	
	public boolean onTouch(int x, int y) {
		return false;
	}

	public void setMaxWidth(int maxWidth) {
		if (!isEmpty && maxWidth >= ICON_SIZE) {
			//Log.d(Log.TOPIC_RENDER_TAG, "setting maxWidth=" + maxWidth + " in " + this);

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
		Resources r = context.getResources();

		priorityIcons = new Bitmap[] { null, getBitmap(r.getDrawable(R.drawable.topic_priority1)),
				getBitmap(r.getDrawable(R.drawable.topic_priority2)), getBitmap(r.getDrawable(R.drawable.topic_priority3)),
				getBitmap(r.getDrawable(R.drawable.topic_priority4)), getBitmap(r.getDrawable(R.drawable.topic_priority5)),
				getBitmap(r.getDrawable(R.drawable.topic_priority6)), getBitmap(r.getDrawable(R.drawable.topic_priority7)),
				getBitmap(r.getDrawable(R.drawable.topic_priority8)), getBitmap(r.getDrawable(R.drawable.topic_priority9)) };

		smileyIcons = new Bitmap[] { getBitmap(r.getDrawable(R.drawable.topic_smiley_happy)),
				getBitmap(r.getDrawable(R.drawable.topic_smiley_normal)), getBitmap(r.getDrawable(R.drawable.topic_smiley_sad)),
				getBitmap(r.getDrawable(R.drawable.topic_smiley_furious)) };

		taskCompletionIcons = new Bitmap[] { getBitmap(r.getDrawable(R.drawable.topic_task_completion_todo)),
				getBitmap(r.getDrawable(R.drawable.topic_task_completion_25)),
				getBitmap(r.getDrawable(R.drawable.topic_task_completion_50)),
				getBitmap(r.getDrawable(R.drawable.topic_task_completion_75)),
				getBitmap(r.getDrawable(R.drawable.topic_task_completion_complete)),
				getBitmap(r.getDrawable(R.drawable.topic_task_completion_cancelled)) };

		flagIcons = new Bitmap[] { getBitmap(r.getDrawable(R.drawable.topic_flag_go)),
				getBitmap(r.getDrawable(R.drawable.topic_flag_for_discussion)),
				getBitmap(r.getDrawable(R.drawable.topic_flag_possibility)),
				getBitmap(r.getDrawable(R.drawable.topic_flag_risk)),
				getBitmap(r.getDrawable(R.drawable.topic_flag_progress)),
				getBitmap(r.getDrawable(R.drawable.topic_flag_carefull)),
				getBitmap(r.getDrawable(R.drawable.topic_flag_caution)) };
				
		icons = new Bitmap[] { getBitmap(r.getDrawable(R.drawable.topic_icon_question_mark)),
				getBitmap(r.getDrawable(R.drawable.topic_icon_exclamation_mark)),
				getBitmap(r.getDrawable(R.drawable.topic_icon_lock)),
				getBitmap(r.getDrawable(R.drawable.topic_icon_tea_time)),
				getBitmap(r.getDrawable(R.drawable.topic_icon_puzzle)),
				getBitmap(r.getDrawable(R.drawable.topic_icon_magnifier)),
				getBitmap(r.getDrawable(R.drawable.topic_icon_thumbs_up)),
				getBitmap(r.getDrawable(R.drawable.topic_icon_thumbs_down)),
				getBitmap(r.getDrawable(R.drawable.topic_icon_idea)),
				getBitmap(r.getDrawable(R.drawable.topic_icon_dollar)),
				getBitmap(r.getDrawable(R.drawable.topic_icon_heart)),
				getBitmap(r.getDrawable(R.drawable.topic_icon_needs_chat)),
				getBitmap(r.getDrawable(R.drawable.topic_icon_clock)),
				getBitmap(r.getDrawable(R.drawable.topic_icon_reminder)),
				getBitmap(r.getDrawable(R.drawable.topic_icon_bomb)),
				getBitmap(r.getDrawable(R.drawable.topic_icon_test)),
				getBitmap(r.getDrawable(R.drawable.topic_icon_homework)),
				getBitmap(r.getDrawable(R.drawable.topic_icon_needs_feedback)) };

		arrowIcons = new Bitmap[] { getBitmap(r.getDrawable(R.drawable.topic_arrow_blue)),
				getBitmap(r.getDrawable(R.drawable.topic_arrow_purple)),
				getBitmap(r.getDrawable(R.drawable.topic_arrow_black)),
				getBitmap(r.getDrawable(R.drawable.topic_arrow_red)),
				getBitmap(r.getDrawable(R.drawable.topic_arrow_orange)),
				getBitmap(r.getDrawable(R.drawable.topic_arrow_yellow)),
				getBitmap(r.getDrawable(R.drawable.topic_arrow_green)) };
		
		starIcons = new Bitmap[] { getBitmap(r.getDrawable(R.drawable.topic_star_blue)),
				getBitmap(r.getDrawable(R.drawable.topic_star_purple)),
				getBitmap(r.getDrawable(R.drawable.topic_star_black)),
				getBitmap(r.getDrawable(R.drawable.topic_star_red)),
				getBitmap(r.getDrawable(R.drawable.topic_star_orange)),
				getBitmap(r.getDrawable(R.drawable.topic_star_yellow)),
				getBitmap(r.getDrawable(R.drawable.topic_star_green)) };
	}
}
