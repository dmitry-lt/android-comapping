package com.comapping.android.provider.contentprovider;

import static com.comapping.android.provider.communication.ClientHelper.getBytesSum;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.comapping.android.Log;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

public class FileMapContentProvider extends MapContentProvider {
	public static final String PROVIDER_NAME = "sdcard";
	public static final Uri CONTENT_URI = Uri
			.parse("content://" + PROVIDER_NAME);

	public static final int PREFIX_LENGTH = 2;
	
	

	public static boolean ignoreCache = true;

	private static final int MAP = 1;
	private static final int META_MAP = 2;
	private static final UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(PROVIDER_NAME, "/#####", MAP);
		uriMatcher.addURI(PROVIDER_NAME, "/meta", META_MAP);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		return false;
	}

	private String URiToId(Uri uri) {
		
		return uri.getPath().substring(PREFIX_LENGTH);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		return new FileMapCursor(URiToId(uri));

	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	private class FileMapCursor extends MapCursor {

		protected String getTextFromInputStream(InputStream input)
				throws IOException {
			StringBuffer content = new StringBuffer();

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					input), 8 * 1024);

			String line = null;
			boolean first = true;

			while ((line = reader.readLine()) != null) {
				if (!first) {
					content.append(System.getProperty("line.separator"));
				} else {
					first = false;
				}

				content.append(line);
			}

			reader.close();

			return content.toString();
		}

		public FileMapCursor(String mapId) {
			this.id = mapId;

			String response = null;

			try {
				Log.e(Log.CONNECTION_TAG,mapId );
				response = getTextFromInputStream(new FileInputStream(mapId));
			} catch (FileNotFoundException e) {
				Log.e(Log.CONNECTION_TAG, "map file not found");
			} catch (IOException e) {
				Log.e(Log.CONNECTION_TAG, "map file IO exception");
			}

			Log.d(Log.CONNECTION_TAG, "file comap provider response: "
					+ response);
		//	Log.d(Log.CONNECTION_TAG, "file comap provider check sum: "
		//			+ getBytesSum(response));

			this.text = response;

		}
	}

}
