package com.comapping.android.provider.contentprovider;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

import com.comapping.android.Log;
import com.comapping.android.map.model.map.Map;
import com.comapping.android.map.model.map.Topic;
import com.comapping.android.map.model.map.builder.MapBuilder;
import com.comapping.android.map.model.map.builder.SaxMapBuilder;
import com.comapping.android.metamap.MetaMapItem;
import com.comapping.android.provider.communication.CachingClient;
import com.comapping.android.provider.communication.Client;
import com.comapping.android.provider.communication.exceptions.ConnectionException;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

public class ComappingMapContentProvider extends MapContentProvider {
	public static final MapContentProviderInfo INFO = new MapContentProviderInfo("www.comapping.com", "maps", true, true);
	public static final Uri CONTENT_URI = Uri.parse("content://" + INFO.root);

	private enum QueryType {
		MAP, META_MAP, LOGOUT, SYNC
	}

	private static final UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(INFO.authorities, INFO.separator + INFO.relLogout, QueryType.LOGOUT.ordinal());
		uriMatcher.addURI(INFO.authorities, INFO.separator + INFO.relSync, QueryType.SYNC.ordinal());
		uriMatcher.addURI(INFO.authorities, INFO.relRoot + INFO.separator + "*", QueryType.MAP.ordinal());
//		uriMatcher.addURI(INFO.authorities, INFO.separator + INFO.relRoot + INFO.separator + "#####", QueryType.MAP.ordinal());
		uriMatcher.addURI(INFO.authorities, INFO.separator + INFO.relRoot + INFO.separator + "*" + INFO.separator, QueryType.META_MAP
				.ordinal());
	}

	private CachingClient client;
	private Map metamap;
	private MapBuilder mapBuilder = new SaxMapBuilder();
	private Context context;

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
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		Log.i(Log.PROVIDER_COMAPPING_TAG, "received uri: " + uri.toString());

		// parse uri
		switch (QueryType.values()[uriMatcher.match(uri)]) {
			case META_MAP:
				List<String> pathSegments = uri.getPathSegments();
				if (!INFO.relRoot.equals("")) {
					pathSegments.remove(0);
				}
				return new ComappingMetamapCursor(getTopic(pathSegments));
			case MAP:
				return new ComappingMapCursor(getComap(uri.getLastPathSegment(), true));
			case LOGOUT:
				try {
					client.logout();
				} catch (ConnectionException e) {
					Log.w(Log.PROVIDER_COMAPPING_TAG, "ConnectionException while logout");
				}
				return null;
			case SYNC:
				updateMetamap();
				return null;
			default:
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}

	}

	private Topic getTopic(List<String> pathSegments) {
		if (metamap == null) {
			updateMetamap();
		}

		Topic curTopic = metamap.getRoot();
		for (String segment : pathSegments) {
			boolean folderFound = false;
			for (Topic child : curTopic.getChildTopics()) {
				if (child.getText().equals(segment) && child.isFolder()) {
					curTopic = child;
					folderFound = true;
					break;
				}
			}
			if (!folderFound) {
				Log.w(Log.PROVIDER_COMAPPING_TAG, "Folder " + segment + " is't exist!");
				// stop at curTopic
				return curTopic;
			}
		}

		return curTopic;
	}

	private void updateMetamap() {
		try {
			metamap = mapBuilder.buildMap(getComap("meta", true));
		} catch (Exception e) {
			Log.w(Log.PROVIDER_COMAPPING_TAG, "Error while synchronizing: cannot parse metamap");
		}
	}

	private String getComap(String mapId, boolean ignoreCache) {
		try {
			return client.getComap(mapId, ignoreCache, false);
		} catch (Exception e) {
			Log.w(Log.PROVIDER_COMAPPING_TAG, "error while receiving map: " + mapId);
			return "";
		}
	}

	private class ComappingMetamapCursor extends MetamapCursor {
		private static final String LAST_SYNCHRONIZATION = "Last synchronization";

		private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

		private static final String MAP_DESCRIPTION = "Map";
		private static final String FOLDER_DESCRIPTION = "Folder";

		public ComappingMetamapCursor(Topic topic) {
			currentLevel = getItems(topic.getChildTopics());
		}

		private MetaMapItem[] getItems(Topic[] topics) {
			MetaMapItem[] res = new MetaMapItem[topics.length];

			for (int i = 0; i < topics.length; i++) {
				res[i] = new MetaMapItem();
				res[i].name = topics[i].getText();

				res[i].isFolder = topics[i].isFolder();

				if (res[i].isFolder) {
					res[i].description = getFolderDescription(topics[i]);
				} else {
					res[i].description = getMapDescription(topics[i]);
				}

				res[i].reference = "content://" + INFO.root + INFO.separator + topics[i].getMapRef();
			}

			return res;
		}

		private String getMapDescription(Topic topic) {
			Timestamp lastSynchronizationDate = client.getLastSynchronizationDate(topic.getMapRef());

			if (lastSynchronizationDate == null) {
				return MAP_DESCRIPTION;
			} else {
				SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

				return LAST_SYNCHRONIZATION + ": " + dateFormat.format(lastSynchronizationDate);
			}
		}

		private String getFolderDescription(Topic topic) {
			return FOLDER_DESCRIPTION;
		}
	}

	private class ComappingMapCursor extends MapCursor {
		public ComappingMapCursor(String mapText) {
			this.text = mapText;
		}
	}

}
