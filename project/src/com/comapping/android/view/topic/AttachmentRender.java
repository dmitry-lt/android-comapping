package com.comapping.android.view.topic;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.comapping.android.Log;
import com.comapping.android.Options;
import com.comapping.android.communication.exceptions.ConnectionException;
import com.comapping.android.controller.MetaMapActivity;
import com.comapping.android.controller.R;
import com.comapping.android.model.Attachment;
import com.comapping.android.view.Render;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import static com.comapping.android.view.topic.RenderHelper.getBitmap;

public class AttachmentRender extends Render {

	private static final int ICON_SIZE = 30;

	private static Bitmap attachmentIcon;

	private boolean isEmpty;

	private AlertDialog dialog;
	private ProgressDialog downloadProgressDialog;
	private boolean dowloadedSuccessfully;
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
			dialog = (new AlertDialog.Builder(context).setTitle("Save attachment in " + Options.DOWNLOAD_FOLDER + "?")
					.setMessage(
							"File: " + attachment.getFilename() + "\n" + "Upload date: "
									+ formatDate(attachment.getDate()) + "\n" + "Size: "
									+ formatFileSize(attachment.getSize()) + "\n").setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {

								}
							}).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					downloadProgressDialog = new ProgressDialog(context);
					downloadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					downloadProgressDialog.setOnDismissListener(new OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							if (!dowloadedSuccessfully) {
								(new AlertDialog.Builder(context).setMessage("Error while downloading and saving file")
										.setNeutralButton("Ok", null).create()).show();
							}
						}
					});
					downloadProgressDialog.show();
					downloadProgressDialog.setProgress(0);

					final Thread downloadAndSaveThread = new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								downloadAndSaveAttachment();
								dowloadedSuccessfully = true;
							} catch (ConnectionException e) {
								dowloadedSuccessfully = false;
								e.printStackTrace();
							}
							downloadProgressDialog.dismiss();
						}
					});
					downloadAndSaveThread.start();

					downloadProgressDialog.setOnCancelListener(new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							downloadAndSaveThread.interrupt();
						}
					});
					downloadProgressDialog.setCancelable(true);
				}
			})).create();
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

	private void downloadAndSaveAttachment() throws ConnectionException {
		URL url;
		try {
			url = new URL(Options.DOWNLOAD_SERVER + attachment.getKey());
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new ConnectionException();
		}

		int code = 200;
		InputStream input;
		int contentLength;

		HttpURLConnection connection = null;
		try {
			if (Options.USE_PROXY) {
				connection = (HttpURLConnection) url.openConnection(Options.proxy);
			} else {
				connection = (HttpURLConnection) url.openConnection();
			}

			connection.setDoOutput(true);

			code = connection.getResponseCode();
			contentLength = connection.getContentLength();

			input = connection.getInputStream();
		} catch (IOException e) {
			throw new ConnectionException();
		}

		Log.d(Log.topicRenderTag, "contentLength=" + contentLength + " code=" + code);

		downloadProgressDialog.setMax(contentLength);

		try {
			// TODO problem: if file is already exist
			FileOutputStream output = new FileOutputStream(Options.DOWNLOAD_FOLDER + "\\" + attachment.getFilename());
			BufferedInputStream bInput = new BufferedInputStream(input, 8 * 1024);

			int sum = 0;
			int trysCount = 0;
			while (sum < contentLength) {
				if (Thread.interrupted()) {
					connection.disconnect();
					break;
				}

				int count = bInput.available();
				sum += count;
				trysCount++;
				if (count > 0) {
					byte[] buffer = new byte[count];
					bInput.read(buffer);
					output.write(buffer);
				}
				downloadProgressDialog.setProgress(sum);
			}
			bInput.close();
			output.close();

			Log.d(Log.topicRenderTag, "sum bytes=" + sum + " trysCount=" + trysCount);
		} catch (IOException e) {
			throw new ConnectionException();
		}
	}
}