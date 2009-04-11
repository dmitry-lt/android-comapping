package com.comapping.android.view;

import com.comapping.android.controller.MetaMapActivity;
import com.comapping.android.controller.R;
import com.comapping.android.model.Attachment;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

public class AttachmentRender extends Render {

	private static final int ICON_SIZE = 36;
	private static final int HORISONTAL_MERGING = 5;

	private Attachment attachment;
	private AlertDialog infDialog;
	private int width, height;

	private static boolean iconsLoaded = false;

	private static Bitmap iconsAttachment;

	private static Bitmap getBitmap(Drawable image) {
		Bitmap bitmap = Bitmap.createBitmap(ICON_SIZE, ICON_SIZE, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		image.setBounds(0, 0, ICON_SIZE, ICON_SIZE);
		image.draw(canvas);
		return bitmap;
	}

	private void loadIcons() {
		Resources r = MetaMapActivity.getInstance().getResources();
		iconsAttachment = getBitmap(r.getDrawable(R.drawable.attachment));
	}

	public AttachmentRender(Attachment attachment, Context context) {
		if (!iconsLoaded) {
			loadIcons();
			iconsLoaded = true;
		}

		this.attachment = attachment;

		int iconsCount = 0;
		
		if (this.attachment != null) {
			iconsCount++;
			
			// Alert Dialog is created here
			infDialog = (new AlertDialog.Builder(context)
			.setTitle("Save attachment?")
			.setMessage("File: " + attachment.getFilename() + "\n" +
					"Last modify: " + attachment.getDate().toString() + "\n" +
					"Size: " + attachment.getSize() + "KB\n" +						
					"http://upload.comapping.com/" + attachment.getKey() + "\n")
			.setNegativeButton("No", new DialogInterface.OnClickListener () {
				@Override
				public void onClick(DialogInterface dialog, int which) {

				}
			})
			.setPositiveButton("Yes", new DialogInterface.OnClickListener () {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// Need to save file		
				}
			})
			).create();
		}
		
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
		if (attachment != null) {
			c.drawBitmap(iconsAttachment, x, y, null);
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
		infDialog.show();
	}
}