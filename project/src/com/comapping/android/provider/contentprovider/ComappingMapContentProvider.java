package com.comapping.android.provider.contentprovider;

import java.sql.Timestamp;
import java.util.List;
import java.util.regex.Pattern;

import com.comapping.android.Log;
import com.comapping.android.map.model.map.Map;
import com.comapping.android.map.model.map.Topic;
import com.comapping.android.map.model.map.builder.MapBuilder;
import com.comapping.android.map.model.map.builder.SaxMapBuilder;
import com.comapping.android.metamap.MetaMapItem;
import com.comapping.android.provider.communication.CachingClient;
import com.comapping.android.provider.communication.Client;
import com.comapping.android.provider.communication.exceptions.ConnectionException;
import com.comapping.android.provider.communication.exceptions.InvalidCredentialsException;
import com.comapping.android.provider.contentprovider.exceptions.MapNotFoundException;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class ComappingMapContentProvider extends MapContentProvider {
	public static final MapContentProviderInfo INFO = new MapContentProviderInfo("www.comapping.com", "maps", true,
			true);
	public static final Uri CONTENT_URI = Uri.parse(CONTENT_PREFIX + INFO.root);

	private enum QueryType {
		MAP, META_MAP, LOGOUT, SYNC, FINISH_WORK
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

	private QueryType detectQueryType(Uri uri) {
		String uriString = uri.toString();
		if (Pattern.matches(CONTENT_PREFIX + INFO.logout, uriString)) {
			return QueryType.LOGOUT;
			
		} else if (uriString.matches(CONTENT_PREFIX + INFO.sync)) {
			return QueryType.SYNC;
			
		} else if (uriString.matches(CONTENT_PREFIX + INFO.finishWork)) {
			return QueryType.FINISH_WORK;
			
		} else if (uriString.matches(CONTENT_PREFIX + INFO.root + "\\d+")) {
			return QueryType.MAP;
			
		} else if (uriString.equals(CONTENT_PREFIX + INFO.root)
				|| uriString.matches(CONTENT_PREFIX + INFO.root + ".*" + INFO.separator)) {
			return QueryType.META_MAP;
			
		} else {
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		String uriString = uri.toString();
		Log.i(Log.PROVIDER_COMAPPING_TAG, "received uri: " + uriString);

		boolean ignoreCache = INFO.isIgnoreCache(uriString);
		boolean ignoreInternet = INFO.isIgnoreInternet(uriString);
		uri = Uri.parse(INFO.removeParameters(uriString));

		// parse uri
		QueryType queryType = detectQueryType(uri);
		Log.d(Log.PROVIDER_COMAPPING_TAG, "QueryType is " + queryType.toString());
		switch (queryType) {
			case META_MAP:
				List<String> pathSegments = uri.getPathSegments();
				if (!INFO.relRoot.equals("")) {
					pathSegments = pathSegments.subList(1, pathSegments.size());
				}

				Map metamap = getMetamap(ignoreCache, ignoreInternet);
				if (metamap == null) {
					return null;
				} else {
					return new ComappingMetamapCursor(getTopic(pathSegments, metamap));
				}

			case MAP:
				return new ComappingMapCursor(getComap(uri.getLastPathSegment(), ignoreCache, ignoreInternet));

			case LOGOUT:
				try {
					client.logout();
				} catch (ConnectionException e) {
					Log.w(Log.PROVIDER_COMAPPING_TAG, "ConnectionException while logout");
				}
				return null;

			case SYNC:
				getMetamap(true, false);
				return null;

			case FINISH_WORK:
				try {
					client.applicationClose();
				} catch (ConnectionException e) {
					Log.w(Log.PROVIDER_COMAPPING_TAG, "ConnectionException in client.applicationClose()");
				}
				return null;

			default:
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}

	}

	private Topic getTopic(List<String> pathSegments, Map metamap) {
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

	private Map getMetamap(boolean ignoreCache, boolean ignoreInternet) {
		if (ignoreCache || metamap == null)
		try {
			metamap = mapBuilder.buildMap(getComap("meta", ignoreCache, ignoreInternet));
		} catch (Exception e) {
			Log.w(Log.PROVIDER_COMAPPING_TAG, "Error while synchronizing: cannot parse metamap");
			metamap = null;
		}
		
		return metamap;
	}

	private String getComap(String mapId, boolean ignoreCache, boolean ignoreInternet) {
		try {
			return client.getComap(mapId, ignoreCache, ignoreInternet);
		} catch (InvalidCredentialsException e) {
			throw new MapNotFoundException();
		} catch (Exception e) {
			Log.w(Log.PROVIDER_COMAPPING_TAG, "error " + e.toString() + " while receiving map: " + mapId);
			return "";
		}
	}

	private class ComappingMetamapCursor extends MetamapCursor {
		private static final String LAST_SYNCHRONIZATION = "Last synchronization";

		private static final String MAP_DESCRIPTION = "Not syncronized yet";
		private static final String FOLDER_DESCRIPTION = "Folder";

		public ComappingMetamapCursor(Topic topic) {
			if (topic == null) {
				currentLevel = null;
			} else {
				currentLevel = getItems(topic.getChildTopics());
			}
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
					res[i].reference = CONTENT_PREFIX + INFO.root + topics[i].getMapRef();

					res[i].lastSynchronizationDate = client.getLastSynchronizationDate(topics[i].getMapRef());

					try {
						res[i].sizeInBytes = client.getComap(topics[i].getMapRef(), false, true).length();
					} catch (Exception e) {
						res[i].sizeInBytes = -1;
					}

					res[i].description = getMapDescription(res[i]);
				}
			}

			return res;
		}

		private String getLastSynchronization(Timestamp date) {
			long time = (System.currentTimeMillis() - date.getTime()) / 1000;
			if (time < 5 * 60) {
				return "just now";
			}
			time /= 60;
			if (time < 60) {
				return time + " minutes ago";
			}
			time /= 60;
			if (time < 24) {
				return time + " hours ago";
			}
			time /= 24;
			return time + " days ago";
		}

		private String getSize(int size) {
			if (size == -1)
				return "-";

			if (size < 1024) {
				return size + " bytes";
			}
			size /= 1024;
			return size + " Kbytes";
		}

		private String getMapDescription(MetaMapItem item) {
			Timestamp lastSynchronizationDate = item.lastSynchronizationDate;

			if (lastSynchronizationDate == null) {
				return MAP_DESCRIPTION;
			} else {
				String result = LAST_SYNCHRONIZATION + ": " + getLastSynchronization(lastSynchronizationDate)
						+ "\nSize: ";
				result = result + getSize(item.sizeInBytes);
				return result;
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
