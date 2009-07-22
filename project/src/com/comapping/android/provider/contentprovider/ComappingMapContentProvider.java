package com.comapping.android.provider.contentprovider;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.net.Uri;

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
import com.comapping.android.provider.communication.exceptions.LoginInterruptedException;
import com.comapping.android.provider.contentprovider.exceptions.ConnectionRuntimeException;
import com.comapping.android.provider.contentprovider.exceptions.LoginInterruptedRuntimeException;
import com.comapping.android.provider.contentprovider.exceptions.MapNotFoundException;
import com.comapping.android.provider.contentprovider.exceptions.NotImplementedException;

public class ComappingMapContentProvider extends MapContentProvider {
	public static final MapContentProviderInfo INFO = new MapContentProviderInfo("www.comapping.com", "maps", true,
			true);
	public static final Uri CONTENT_URI = Uri.parse(CONTENT_PREFIX + INFO.root);

	private enum QueryType {
		MAP, GET_MAP_SIZE, START_MAP_DOWNLOADING, META_MAP, LOGIN, LOGOUT, SYNC, FINISH_WORK
	}

	private CachingClient client;
	private Map metamap;
	private MapBuilder mapBuilder = new SaxMapBuilder();
	private Context context;

	private static class MapInfo {
		Notification notification;
		int sizeInBytes;
	}

	private HashMap<String, MapInfo> mapInfos = new HashMap<String, MapInfo>();

	@Override
	public boolean onCreate() {
		context = getContext();

		// prepare client
		if (client == null) {
			client = Client.getClient(context);
		}

		setDownloadListener();

		return true;
	}

	private QueryType detectQueryType(Uri uri) {
		String uriString = uri.toString();
		if (Pattern.matches(CONTENT_PREFIX + INFO.login, uriString)) {
			return QueryType.LOGIN;

		} else if (Pattern.matches(CONTENT_PREFIX + INFO.logout, uriString)) {
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
		String action = INFO.getAction(uriString);
		uri = Uri.parse(INFO.removeParameters(uriString));

		// parse uri
		QueryType queryType = detectQueryType(uri);
		if (action.equals(INFO.startDownloadingAction) && queryType == QueryType.MAP) {
			queryType = QueryType.START_MAP_DOWNLOADING;
		} else if (action.equals(INFO.getMapSizeInBytesAction) && queryType == QueryType.MAP) {
			queryType = QueryType.GET_MAP_SIZE;
		}

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

			case GET_MAP_SIZE:
				String mapId = uri.getLastPathSegment();				
				MetaMapItem[] item = new MetaMapItem[1];
				item[0] = new MetaMapItem();
				try {
					item[0].sizeInBytes = client.getMapSizeInBytes(mapId, false);
				} catch (ConnectionException e2) {
					item[0].sizeInBytes = -1;
					e2.printStackTrace();
				}
				return new ComappingMetamapCursor(item);
				
			case START_MAP_DOWNLOADING:
				startMapDownloading(uri.getLastPathSegment());
				return null;

			case LOGIN:
				try {
					client.loginRequired();
				} catch (LoginInterruptedException e1) {
					throw new LoginInterruptedRuntimeException();
				}
				return new ComappingLoginCursor(client.getClientId());

			case LOGOUT:
				try {
					client.logout();
				} catch (ConnectionException e) {
					Log.w(Log.PROVIDER_COMAPPING_TAG, "ConnectionException while logout");
				}
				this.metamap = null;
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
				this.metamap = null;
				return null;

			default:
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}

	}

	private void setDownloadListener() {
		// TODO unused now
		client.setDownloadingListener(new CachingClient.IDownloadingListener() {
			@Override
			public void statusChanged(String mapId, int downloadedInBytes) {
				MapInfo mapInfo = mapInfos.get(mapId);
				if (mapInfo != null) {
					mapInfo.notification.setLatestEventInfo(context, "Downloading map, mapId" + mapId,
							downloadedInBytes + "/" + mapInfo.sizeInBytes, null);
				}
			}
		});
	}

	private void startMapDownloading(String mapId) {
		// TODO unused now
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);

		long when = System.currentTimeMillis();
		Notification notification = new Notification(0, "start downloading mapId=" + mapId, when);

		MapInfo mapInfo = new MapInfo();
		mapInfo.notification = notification;
		try {
			mapInfo.sizeInBytes = client.getMapSizeInBytes(mapId, false);
		} catch (Exception e) {
			Log.e(Log.CONNECTION_TAG,e.toString());
			
		}

		// Context context = this.context.getApplicationContext();
		CharSequence contentTitle = "Downloading map, mapId" + mapId;
		CharSequence contentText = "0/" + mapInfo.sizeInBytes;

		notification.setLatestEventInfo(context, contentTitle, contentText, null);
		mNotificationManager.notify(1, notification);

		client.startMapDownloading(mapId);
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
		} catch (LoginInterruptedException e) {
			throw new LoginInterruptedRuntimeException();
		} catch (ConnectionException e) {
			throw new ConnectionRuntimeException();
		}
	}

	private class ComappingLoginCursor extends AbstractCursor {

		private String cliendId;

		public ComappingLoginCursor(String clientId) {
			this.cliendId = clientId;
		}

		@Override
		public String[] getColumnNames() {
			return new String[] { "clientId" };
		}

		@Override
		public int getCount() {
			return 1;
		}

		@Override
		public double getDouble(int column) {
			throw new NotImplementedException();
		}

		@Override
		public float getFloat(int column) {
			throw new NotImplementedException();
		}

		@Override
		public int getInt(int column) {
			throw new NotImplementedException();
		}

		@Override
		public long getLong(int column) {
			throw new NotImplementedException();
		}

		@Override
		public short getShort(int column) {
			throw new NotImplementedException();
		}

		@Override
		public String getString(int column) {
			switch (column) {
				case 0:
					return cliendId;
				default:
					throw new IllegalArgumentException("No such column " + column);
			}
		}

		@Override
		public boolean isNull(int column) {
			throw new NotImplementedException();
		}

	}

	private class ComappingMetamapCursor extends MetamapCursor {
		private static final String LAST_SYNCHRONIZATION = "Last synchronization";

		private static final String MAP_DESCRIPTION = "Not syncronized yet";
		private static final String FOLDER_DESCRIPTION = "Folder";

		public ComappingMetamapCursor(Topic topic) {
			currentLevel = getItems(topic.getChildTopics());
			this.moveToFirst();
		}
		
		public ComappingMetamapCursor(MetaMapItem[] items) {
			currentLevel = items;
			this.moveToFirst();
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
						res[i].sizeInBytes = client.getMapSizeInBytes(topics[i].getMapRef(), true);
					} catch (ConnectionException e) {
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
				if (time == 1) {
					return time + " minute ago";
				} else {
					return time + " minutes ago";
				}
			}
			time /= 60;
			if (time < 24) {
				if (time == 1) {
					return time + " hour ago";
				} else {
					return time + " hours ago";
				}
			}
			time /= 24;
			if (time == 1) {
				return time + " day ago";
			} else {
				return time + " days ago";
			}
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
				return MAP_DESCRIPTION + "\nSize: " + getSize(item.sizeInBytes);
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
