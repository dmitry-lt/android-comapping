package com.comapping.android.map.render.topic;

import java.io.BufferedInputStream;
import java.io.File;
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
import com.comapping.android.preferences.PreferencesStorage;
import com.comapping.android.provider.communication.Client;
import com.comapping.android.provider.communication.exceptions.ConnectionException;
import com.comapping.android.controller.R;
import com.comapping.android.map.model.map.Attachment;
import com.comapping.android.map.render.Render;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import static com.comapping.android.map.render.topic.RenderHelper.getBitmap;

public class AttachmentRender extends Render {
	private static final int ICON_SIZE = 30;

	private static Bitmap attachmentIcon;

	private boolean isEmpty;

	private AlertDialog dialog;
	private ProgressDialog downloadProgressDialog;
	private String downloadFolder;
	private boolean downloadedSuccessfully;
	private int width, height;
	private Context context;
	private Attachment attachment;

	public AttachmentRender(Attachment attachment, Context context) {
		isEmpty = (attachment == null);

		if (!isEmpty) {
			this.context = context;
			this.attachment = attachment;

			if (attachmentIcon == null) {
				loadIcon();
			}

			width = ICON_SIZE;
			height = ICON_SIZE;
		} else {
			width = 0;
			height = 0;
		}
	}

	public void draw(int x, int y, int width, int height, Canvas c) {
		if (!isEmpty) {
			c.drawBitmap(attachmentIcon, x, y, null);
		} else {
			// nothing to draw
		}
	}

	public String toString() {
		if (!isEmpty) {
			return "[AttachmentRender: width=" + getWidth() + " height="
					+ getHeight() + "]";
		} else {
			return "[AttachmentRender: EMPTY]";
		}
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public boolean onTouch(int x, int y) {
		if (dialog == null) {
			downloadFolder = PreferencesStorage.get(
					PreferencesStorage.DOWNLOAD_FOLDER_KEY,
					PreferencesStorage.DOWNLOAD_FOLDER_DEFAULT_VALUE, context);

			dialog = (new AlertDialog.Builder(context).setTitle("Attachment")
					.setMessage(
							"File: " + attachment.getFilename() + "\n"
									+ "Upload date: "
									+ formatDate(attachment.getDate()) + "\n"
									+ "Size: "
									+ formatFileSize(attachment.getSize())
									+ "\n\n" + "Save in " + downloadFolder
									+ " ?").setNegativeButton("No",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {

								}
							}).setPositiveButton("Yes",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							if (android.os.Environment
									.getExternalStorageState()
									.equals(
											android.os.Environment.MEDIA_MOUNTED)) {
								downloadProgressDialog = new ProgressDialog(
										context);
								downloadProgressDialog
										.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
								downloadProgressDialog
										.setOnDismissListener(new OnDismissListener() {

											public void onDismiss(
													DialogInterface dialog) {
												if (!downloadedSuccessfully) {
													(new AlertDialog.Builder(
															context)
															.setMessage(
																	"Error while downloading and saving file")
															.setNeutralButton(
																	"Ok", null)
															.create()).show();
												}
											}
										});
								downloadProgressDialog.show();
								downloadProgressDialog.setProgress(0);

								final Thread downloadAndSaveThread = new Thread(
										new Runnable() {

											public void run() {
												try {
													downloadAndSaveAttachment();
													downloadedSuccessfully = true;
												} catch (ConnectionException e) {
													downloadedSuccessfully = false;
													e.printStackTrace();
												}
												downloadProgressDialog
														.dismiss();
											}
										});
								downloadAndSaveThread.start();

								downloadProgressDialog
										.setOnCancelListener(new OnCancelListener() {

											public void onCancel(
													DialogInterface dialog) {
												downloadAndSaveThread
														.interrupt();
											}
										});
								downloadProgressDialog.setCancelable(true);
							} else {
								(new AlertDialog.Builder(context)
										.setTitle("Alert Message")
										.setMessage("You can't download this file because SD card is not installed."))
										.show();
							}
						}
					})).create();
		}
		dialog.show();
		return false;
	}

	private void loadIcon() {
		Resources r = context.getResources();
		attachmentIcon = getBitmap(r.getDrawable(R.drawable.topic_attachment),
				ICON_SIZE);
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
			connection = Client.getClient(context).getHttpURLConnection(url);

			connection.setDoOutput(true);

			code = connection.getResponseCode();
			contentLength = connection.getContentLength();

			input = connection.getInputStream();
		} catch (IOException e) {
			throw new ConnectionException();
		}

		Log.d(Log.TOPIC_RENDER_TAG, "contentLength=" + contentLength + " code="
				+ code);

		downloadProgressDialog.setMax(contentLength);

		try {
			String path = downloadFolder + "\\" + attachment.getFilename();
			String name;
			String ext;
			int dotIndex = path.lastIndexOf('.');
			if (dotIndex != -1) {
				name = path.substring(0, dotIndex);
				ext = path.substring(dotIndex + 1, path.length());
			} else {
				name = path;
				ext = "";
			}

			File file = new File(downloadFolder);
			file.mkdirs();

			file = new File(path);
			int counter = 1;
			while (file.exists()) {
				file = new File(name + "(" + counter + ")" + ext);
				counter++;
			}
			file.createNewFile();

			FileOutputStream output = new FileOutputStream(file);
			BufferedInputStream bufferedInput = new BufferedInputStream(input,
					8 * 1024);

			int sum = 0;
			int readsCount = 0;
			while (sum < contentLength) {
				if (Thread.interrupted()) {
					connection.disconnect();
					break;
				}

				int count = bufferedInput.available();
				sum += count;
				readsCount++;
				if (count > 0) {
					byte[] buffer = new byte[count];
					bufferedInput.read(buffer);
					output.write(buffer);
				}
				downloadProgressDialog.setProgress(sum);
			}
			bufferedInput.close();
			output.close();

			Log.d(Log.TOPIC_RENDER_TAG, "sum bytes=" + sum + " readsCount="
					+ readsCount);
		} catch (IOException e) {
			throw new ConnectionException();
		}
	}
}