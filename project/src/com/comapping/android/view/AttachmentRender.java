package com.comapping.android.view;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.comapping.android.controller.MetaMapActivity;
import com.comapping.android.controller.R;
import com.comapping.android.model.Attachment;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;

import static com.comapping.android.view.RenderHelper.getBitmap;

public class AttachmentRender extends Render {

	private static final int ICON_SIZE = 28;

	private static boolean iconLoaded = false;
	private static Bitmap attachmentIcon;

	private boolean isEmpty;

	private AlertDialog dialog;
	private int width, height;

	public AttachmentRender(Attachment attachment, Context context) {
		if (attachment != null) {
			isEmpty = false;
		} else {
			isEmpty = true;
		}

		if (!isEmpty) {
			if (!iconLoaded) {
				loadIcons();
				iconLoaded = true;
			}

			final Context fContext = context;
			final String url = "http://upload.comapping.com/" + attachment.getKey();
//			final String url = "http://stg243.ifolder.ru/download/?11604085&ah58bG1OECiK7Yk6SpaLmw%3D%3D";
			// Alert Dialog is created here
			dialog = (new AlertDialog.Builder(context)
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

			width = ICON_SIZE;
			height = ICON_SIZE;
		} else {
			width = 0;
			height = 0;
		}
	}

	@Override
	public void draw(int x, int y, int width, int height, Canvas c) {
		if (!isEmpty) {
			c.drawBitmap(attachmentIcon, x, y, null);
		} else {
			// nothing to draw
		}
	}

	@Override
	public String toString() {
		return "[AttachmentRender: width=" + getWidth() + " height=" + getHeight() + "]";
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
		dialog.show();
	}

	private void loadIcons() {
		Resources r = MetaMapActivity.getInstance().getResources();
		attachmentIcon = getBitmap(r.getDrawable(R.drawable.attachment), ICON_SIZE);
	}

	private String fileSizeFormating(int size) {
		float fSize = size;
		String res = "";
		if (size > 1024 * 1024 * 1024) {
			res = String.format("%.2f", fSize / (1024 * 1024 * 1024)) + " GB";			
		} else if (size > 1024 * 1024) {
			res = String.format("%.2f", fSize / (1024 * 1024)) + " MB";
		} else if (size > 1024) {
			res = String.format("%.2f", fSize / 1024) + " KB";
		} else if (size > 0) {
			res = fSize + " bytes";
		}
		return res;
	}

	private String dateFormating(Date date) {
		DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
		return dateFormat.format(date);
	}
}