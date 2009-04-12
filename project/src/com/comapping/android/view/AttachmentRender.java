package com.comapping.android.view;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.comapping.android.controller.MetaMapActivity;
import com.comapping.android.controller.R;
import com.comapping.android.model.Attachment;
import com.comapping.android.Options;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;

public class AttachmentRender extends Render {

	private static final int ICON_SIZE = 36;
	private static final int HORISONTAL_MERGING = 5;

	private Attachment attachment;
	private AlertDialog infDialog;
	private int width, height;

	private static boolean iconsLoaded = false;
	private static Bitmap iconsAttachment;

	public AttachmentRender(Attachment attachment, Context context) {
		if (!iconsLoaded) {
			loadIcons();
			iconsLoaded = true;
		}

		this.attachment = attachment;

		int iconsCount = 0;

		if (this.attachment != null) {
			iconsCount++;

			final Context fContext = context;
			final String url = "http://upload.comapping.com/" + attachment.getKey();
			// Alert Dialog is created here
			infDialog = (new AlertDialog.Builder(context)
			.setTitle("Save attachment?")
			.setMessage("File: " + attachment.getFilename() + "\n" +
					"Upload date: " + dateFormating(attachment.getDate()) + "\n" +
					"Size: " + fileSizeFormating(attachment.getSize()) + "\n")
			.setNegativeButton("No", new DialogInterface.OnClickListener () {
				@Override
				public void onClick(DialogInterface dialog, int which) {

				}
			})
			.setPositiveButton("Yes", new DialogInterface.OnClickListener () {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					fContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
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

	private String fileSizeFormating(int size) {
		String res = "";
		if (size > 1024 * 1024 * 1024) {
			res += size / (1024 * 1024 * 1024) + " GB\n";
			size = size % (1024 * 1024 * 1024);
		}
		if (size > 1024 * 1024) {
			res += size / (1024 * 1024) + " MB\n";
			size = size % (1024 * 1024);
		}
		if (size > 1024) {
			res += size / 1024 + " KB\n";
			size = size % 1024;
		}
		if (size > 0) {
			res += size + " bytes";
		}
		return res;
	}

	private String dateFormating(Date date) {
		DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss dd.MM.yyyy");
		return dateFormat.format(date);
	}
}