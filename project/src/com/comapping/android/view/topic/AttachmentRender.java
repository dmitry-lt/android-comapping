package com.comapping.android.view.topic;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.comapping.android.Options;
import com.comapping.android.controller.MetaMapActivity;
import com.comapping.android.controller.R;
import com.comapping.android.model.Attachment;
import com.comapping.android.view.Render;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;

import static com.comapping.android.view.topic.RenderHelper.getBitmap;

public class AttachmentRender extends Render {

	private static final int ICON_SIZE = 30;

	private static Bitmap attachmentIcon;

	private boolean isEmpty;

	private AlertDialog dialog;
	private int width, height;
	private Context context;
	private Attachment attachment;
	
	public AttachmentRender(Attachment attachment, Context context) {		
		isEmpty = (attachment == null);

		if (!isEmpty) {
			if (attachmentIcon == null) {
				loadIcon();
			}

			this.context = context;
			this.attachment = attachment;

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
		if (!isEmpty) {
			return "[AttachmentRender: width=" + getWidth() + " height=" + getHeight() + "]";
		} else {
			return "[AttachmentRender: EMPTY]";
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
		
		if (dialog == null) {
			final String url = Options.DOWNLOAD_SERVER + attachment.getKey();
			// final String url =
			// "http://stg243.ifolder.ru/download/?11604085&ah58bG1OECiK7Yk6SpaLmw%3D%3D";
			// Alert Dialog is created here
			dialog = (new AlertDialog.Builder(context)
			.setTitle("Save attachment?")
			.setMessage("File: " + attachment.getFilename() + "\n" +
					"Upload date: " + formatDate(attachment.getDate()) + "\n" +
					"Size: " + formatFileSize(attachment.getSize()) + "\n")
			.setNegativeButton("No", new DialogInterface.OnClickListener () {
				@Override
				public void onClick(DialogInterface dialog, int which) {

				}
			})
			.setPositiveButton("Yes", new DialogInterface.OnClickListener () {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
				}
			})
			).create();
		}
		
		dialog.show();
	}

	private void loadIcon() {
		Resources r = MetaMapActivity.getInstance().getResources();
		attachmentIcon = getBitmap(r.getDrawable(R.drawable.attachment), ICON_SIZE);
	}

	private String formatFileSize(int size) {
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

	private String formatDate(Date date) {
		DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
		return dateFormat.format(date);
	}
}