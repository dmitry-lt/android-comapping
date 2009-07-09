package com.comapping.android.provider.contentprovider;

import com.comapping.android.Log;
import com.comapping.android.provider.communication.CachingClient;
import com.comapping.android.provider.communication.Client;
import com.comapping.android.provider.communication.exceptions.ConnectionException;
import com.comapping.android.provider.communication.exceptions.InvalidCredentialsException;
import com.comapping.android.provider.communication.exceptions.LoginInterruptedException;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

public class ComappingMapContentProvider extends MapContentProvider {
	public static final String PROVIDER_NAME = "comapping.com";

	public static final Uri CONTENT_URI = Uri.parse("content://"
			+ PROVIDER_NAME + "/maps");

	public static boolean ignoreCache = true;

	private static final int MAP = 1;
	private static final int META_MAP = 2;

	private static final UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(PROVIDER_NAME, "maps/#####", MAP);
		uriMatcher.addURI(PROVIDER_NAME, "*/", META_MAP);
	}

	private CachingClient client;
	private Context context;

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
		context = getContext();

		// prepare client
		if (client == null) {
			client = Client.getClient(context);
		}

		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {						
		 // parse uri
		 switch (uriMatcher.match(uri)) {
		 	case META_MAP:
		 		
		 	case MAP:
		 		return new ComappingMapCursor(uri.getLastPathSegment(), client, context);
		 	default:
		 		throw new IllegalArgumentException("Unsupported URI: " + uri);
		 }
				
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	private class ComappingMetamapCursor extends MetamapCursor {
		
	}
	
	private class ComappingMapCursor extends MapCursor {
		public ComappingMapCursor(String mapId, CachingClient client,
				Context context) {
			this.id = mapId;

			try {
				this.text = client.getComap(mapId, context, ignoreCache, false);
				Log.d("InternetMapContentProvider", "text received: " + text);
			} catch (ConnectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (LoginInterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidCredentialsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
