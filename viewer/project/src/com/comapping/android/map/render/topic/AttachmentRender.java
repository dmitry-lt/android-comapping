package com.comapping.android.map.render.topic;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Environment;
import android.webkit.MimeTypeMap;
import com.comapping.android.Log;
import com.comapping.android.Options;
import com.comapping.android.R;
import com.comapping.android.map.model.map.Attachment;
import com.comapping.android.map.render.Render;
import com.comapping.android.preferences.PreferencesStorage;
import com.comapping.android.provider.communication.Client;
import com.comapping.android.provider.communication.exceptions.ConnectionException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.comapping.android.map.render.topic.RenderHelper.getBitmap;

public class AttachmentRender extends Render {
	private static final int ICON_SIZE = 30;
	// used for downloading file name when file with the same name exists in the directory
	private static final String NEXT_FILE_NAME_FORMAT = "%s (%d)%s";

	private static Bitmap attachmentIcon;

	private boolean isEmpty;

	private AlertDialog dialog;
	private ProgressDialog downloadProgressDialog;
	private String downloadFolder;
	private String downloadedFile;
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
			return "[AttachmentRender: width=" + getWidth() + " height="
					+ getHeight() + "]";
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
	public boolean onTouch(int x, int y) {
		if (dialog == null) {
			downloadFolder = PreferencesStorage.get(
					PreferencesStorage.DOWNLOAD_FOLDER_KEY,
					PreferencesStorage.DOWNLOAD_FOLDER_DEFAULT_VALUE, context);

			dialog = (new AlertDialog.Builder(context).setIcon(
					android.R.drawable.ic_dialog_info).setTitle(R.string.AttachmentDialogTitle)
					.setMessage(
							String.format(context.getString(R.string.AttachmentDialogMessageFormat),
									attachment.getFilename(),
									formatDate(attachment.getDate()),
									formatFileSize(attachment.getSize()),
									downloadFolder))
					.setNegativeButton(R.string.AttachmentCancel,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {

								}
							})
					.setPositiveButton(R.string.AttachmentSaveAndOpen, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							setupDownloadDialog(new OnDismissListener() {
								@Override
								public void onDismiss(DialogInterface dialog) {
									if (downloadedFile == null || downloadedFile.length() == 0) {
										(new AlertDialog.Builder(context)
												.setMessage(R.string.ErrorDownloadingAndSavingFile)
												.setNeutralButton(R.string.NeutralButtonText, null)
												.create()).show();
									} else {
										open();
									}
								}
							});
							save();
						}
					})
					.setNeutralButton(R.string.AttachmentSave,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									setupDownloadDialog(new OnDismissListener() {
										@Override
										public void onDismiss(DialogInterface dialog) {
											if (downloadedFile == null || downloadedFile.length() == 0) {
												(new AlertDialog.Builder(context)
														.setMessage(R.string.ErrorDownloadingAndSavingFile)
														.setNeutralButton(R.string.NeutralButtonText, null)
														.create()).show();
											}
										}
									});
									save();
								}
							})).create();
		}
		dialog.show();
		return false;
	}

	/**
	 * Start thread with downloading task and show error messages
	 */
	private void save() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			downloadProgressDialog.show();
			final Thread downloadAndSaveThread = new Thread(
					new Runnable() {
						@Override
						public void run() {
							try {
								downloadedFile = downloadAndSaveAttachment();
							} catch (ConnectionException e) {
								downloadedFile = null;
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
		} else {
			(new AlertDialog.Builder(context)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(R.string.AttachmentAlertDialogTitle)
					.setMessage(R.string.AttachmentAlertSdNotInstalled))
					.show();
		}
	}

	/**
	 * Open just downloaded file in external application
	 */
	private void open() {
		// get file extension
		String extension = "";
		int dotIndex = downloadedFile.lastIndexOf('.');
		if (dotIndex != -1) {
			extension = downloadedFile.substring(dotIndex + 1, downloadedFile.length());
		}

		// create an intent
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
		Uri data = Uri.fromFile(new File(downloadedFile));
		String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
		if (type == null || type.length() == 0) {
			// if there is no acceptable mime type
			type = "application/octet-stream";
		}
		intent.setDataAndType(data, type);

		// get the list of the activities which can open the file
		List<ResolveInfo> resolvers = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		if (resolvers.isEmpty()) {
			(new AlertDialog.Builder(context)
					.setMessage(R.string.AttachmentUnknownFileType)
					.setNeutralButton(R.string.NeutralButtonText, null)
					.create()).show();
		} else {
			context.startActivity(intent);
		}
	}

	/**
	 * Prepare download attachment dialog for showing (but not show)
	 * @param onDismissListener to process just downloaded file
	 */
	private void setupDownloadDialog(OnDismissListener onDismissListener) {
		downloadProgressDialog = new ProgressDialog(context);
		downloadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		downloadProgressDialog.setProgress(0);
		downloadProgressDialog.setCancelable(true);
		downloadProgressDialog.setOnDismissListener(onDismissListener);
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
			res = String.format(context.getString(R.string.GBytesFloatSizeFormat), fSize / (1024 * 1024 * 1024));
		} else if (size > 1024 * 1024) {
			res = String.format(context.getString(R.string.MBytesFloatSizeFormat), fSize / (1024 * 1024));
		} else if (size > 1024) {
			res = String.format(context.getString(R.string.KBytesFloatSizeFormat), fSize / 1024);
		} else if (size > 0) {
			res = String.format(context.getString(R.string.BytesFloatSizeFormat), fSize);
		}
		return res;
	}

	private String formatDate(Date date) {
		DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
		return dateFormat.format(date);
	}

	/**
	 * Download attachment from server and save it in the downloads directory
	 * @return path to saved file or null if it hasn't been saved
	 * @throws ConnectionException if any IO or URL exceptions occurred
	 */
	private String downloadAndSaveAttachment() throws ConnectionException {
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

		String pathToFile;
		try {
			pathToFile = downloadFolder + "/" + attachment.getFilename();
			String name;
			// extension with dot
			String ext;
			int dotIndex = pathToFile.lastIndexOf('.');
			if (dotIndex != -1) {
				name = pathToFile.substring(0, dotIndex);
				ext = pathToFile.substring(dotIndex, pathToFile.length());
			} else {
				name = pathToFile;
				ext = "";
			}

			File file = new File(downloadFolder);
			file.mkdirs();

			file = new File(pathToFile);
			int counter = 1;
			while (file.exists()) {
				file = new File(String.format(NEXT_FILE_NAME_FORMAT, name, counter, ext));
				counter++;
			}
			file.createNewFile();
			pathToFile = file.getAbsolutePath();

			FileOutputStream output = new FileOutputStream(file);
			BufferedInputStream bufferedInput = new BufferedInputStream(input,
					8 * 1024);

			int sum = 0;
			int readsCount = 0;
			// TODO: bug. content length is not correct. Use InputStream instead
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
		return pathToFile;
	}
}