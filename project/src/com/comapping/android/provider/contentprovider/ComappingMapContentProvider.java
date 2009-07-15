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
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class ComappingMapContentProvider extends MapContentProvider {
	public static final MapContentProviderInfo INFO = new MapContentProviderInfo("www.comapping.com", "maps", true, true);
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
		} else if (Pattern.matches(CONTENT_PREFIX + INFO.sync, uriString)) {
			return QueryType.SYNC;
		} else if (Pattern.matches(CONTENT_PREFIX + INFO.finishWork, uriString)) {
			return QueryType.FINISH_WORK;			
		} else if (Pattern.matches(CONTENT_PREFIX + INFO.root + "\\d\\d\\d\\d\\d", uriString)) {
			return QueryType.MAP;		
		} else { 
			return QueryType.META_MAP;
		}		
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		String uriString = uri.toString();
		Log.i(Log.PROVIDER_COMAPPING_TAG, "received uri: " + uriString);
		
		boolean ignoreCache = false;		
		if (uriString.endsWith(INFO.ignoreCacheSuffix)) {
			ignoreCache = true;
			uri = Uri.parse(uriString.substring(0, uriString.length() - INFO.ignoreCacheSuffix.length()));
		}
		
		boolean ignoreInternet = false;		
		if (uriString.endsWith(INFO.ignoreInternetSuffix)) {
			ignoreInternet = true;
			uri = Uri.parse(uriString.substring(0, uriString.length() - INFO.ignoreInternetSuffix.length()));
		}		
		
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
		if (metamap != null && !ignoreCache) {
			return metamap;
		}		
		
		try {
			return mapBuilder.buildMap(getComap("meta", ignoreCache, ignoreInternet));
		} catch (Exception e) {
			Log.w(Log.PROVIDER_COMAPPING_TAG, "Error while synchronizing: cannot parse metamap");
			return null;
		}
	}

	private String getComap(String mapId, boolean ignoreCache, boolean ignoreInternet) {		
		try {
			return client.getComap(mapId, ignoreCache, ignoreInternet);
		} catch (Exception e) {
			Log.w(Log.PROVIDER_COMAPPING_TAG, "error while receiving map: " + mapId);
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
					res[i].description = getMapDescription(topics[i]);
				}

				res[i].reference = CONTENT_PREFIX + INFO.root + topics[i].getMapRef();
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
			if (size < 1024) {
				return size + " bytes";
			}
			size /= 1024;
			return size + " Kbytes";
		}
		
		private String getMapDescription(Topic topic) {
			Timestamp lastSynchronizationDate = client.getLastSynchronizationDate(topic.getMapRef());

			if (lastSynchronizationDate == null) {
				return MAP_DESCRIPTION;
			} else {
				String result = LAST_SYNCHRONIZATION + ": "
				+ getLastSynchronization(lastSynchronizationDate) + "\nSize: ";
				try {
					result = result 
							+ getSize(client.getComap(topic.getMapRef()).length());
				} catch (Exception e) {
					
				}
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
