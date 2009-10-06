package com.comapping.android.provider.contentprovider;

import java.io.InputStream;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.net.Uri;

import com.comapping.android.metamap.MetaMapItem;
import com.comapping.android.provider.ioHelper;
import com.comapping.android.provider.contentprovider.exceptions.*;

public abstract class MapContentProvider extends ContentProvider {
	public static final String CONTENT_PREFIX = "content://";
	
	public static final String ID = "id";
	public static final String CONTENT = "content";
	
	private static MapContentProviderInfo infoForParameters = new MapContentProviderInfo();
	
	public static String getComap(String mapRef, Context context) {
		Uri fullUri = Uri.parse(mapRef);
		// Log.d(Log.PROVIDER_TAG, "getComap: uri=" + fullUri);
		Cursor cursor = context.getContentResolver().query(fullUri, null, null, null, null);
		
		return cursor.getString(cursor.getColumnIndex(CONTENT));
	}
	
	public static String getComap(String mapRef, boolean ignoreCache, boolean ignoreInternet, Context context) {
		if (mapRef.equals("content://resources/welcome")) {
			// TODO: create normal Resource map provider
			InputStream input = context.getResources().openRawResource(com.comapping.android.R.raw.welcome);
			return ioHelper.getTextFromInputStream(input);
		} else {
			mapRef = infoForParameters.setIgnoreCache(mapRef, ignoreCache);
			mapRef = infoForParameters.setIgnoreInternet(mapRef, ignoreInternet);
			return getComap(mapRef, context);
		}
	}
	
	public static class MapContentProviderInfo {
		public final String authorities;
		public final String separator;
		public final String root;
		public final String relRoot;
		public final String login;
		public final String relLogin;
		public final String logout;
		public final String relLogout;
		public final String sync;
		public final String relSync;
		public boolean canLogout;
		public final boolean canSync;
		public final String relFinishWork = "finish_work";
		public final String finishWork;
		
		public final String ignoreCacheParameter = "ignoreCache";
		public final String ignoreInternetParameter = "ignoreInternet";
		public final String actionParameter = "action";
		public final boolean defaultIgnoreCache = false;
		public final boolean defaultIgnoreInternet = false;
		public final String defaultAction = "download";
		public final String startDownloadingAction = "startDownloading";
		public final String getMapSizeInBytesAction = "getMapSizeInBytes";
		
		private MapContentProviderInfo() {
			this("", "", true, true);
		}
		
		public MapContentProviderInfo(String authorities, String relRoot, boolean canLogout, boolean canSync) {
			this(authorities, "/", relRoot, "login", "logout", "sync", canLogout, canSync);
		}
		
		public MapContentProviderInfo(String authorities, String separator, String relRoot, String relLogin,
				String relLogout, String relSync, boolean canLogout, boolean canSync) {
			this.authorities = authorities;
			this.separator = separator;
			
			if (relRoot.equals("")) {
				this.root = authorities + separator;
			} else {
				this.root = authorities + separator + relRoot + separator;
			}
			
			this.relRoot = relRoot;
			this.login = authorities + separator + relLogin;
			this.relLogin = relLogin;
			this.logout = authorities + separator + relLogout;
			this.relLogout = relLogout;
			this.sync = authorities + separator + relSync;
			this.relSync = relSync;
			this.canLogout = canLogout;
			this.canSync = canSync;
			
			this.finishWork = authorities + separator + relFinishWork;
		}
		
		public void setLogout(boolean canLogout) {
			this.canLogout = canLogout;
		}
		
		private String setParameter(String uriString, String parameter, String value) {
			int queryStart = uriString.lastIndexOf('?');
			if (queryStart > -1) {
				int parameterStart = uriString.indexOf(parameter, queryStart);
				if (parameterStart > -1) {
					String before = uriString.substring(0, parameterStart + parameter.length() + 1);
					String after;
					int parameterEnd = uriString.indexOf('&', parameterStart);
					if (parameterEnd > -1) {
						after = uriString.substring(parameterEnd, uriString.length());
					} else {
						after = "";
					}
					
					return before + value + after;
				} else {
					return uriString + "&" + parameter + "=" + value;
				}
			} else {
				return uriString + "?" + parameter + "=" + value;
			}
		}
		
		private String getParameter(String uriString, String parameter) {
			int queryStart = uriString.lastIndexOf('?');
			if (queryStart > -1) {
				int parameterStart = uriString.indexOf(parameter, queryStart);
				if (parameterStart > -1) {
					int valueStart = parameterStart + parameter.length() + 1;
					int valueEnd;
					int parameterEnd = uriString.indexOf('&', parameterStart);
					if (parameterEnd > -1) {
						valueEnd = parameterEnd;
					} else {
						valueEnd = uriString.length();
					}
					
					return uriString.substring(valueStart, valueEnd);
				} else {
					return null;
				}
			} else {
				return null;
			}
		}
		
		public String setIgnoreCache(String uriString, boolean ignoreCache) {
			return setParameter(uriString, ignoreCacheParameter, String.valueOf(ignoreCache));
		}
		
		public boolean isIgnoreCache(String uriString) {
			String value = getParameter(uriString, ignoreCacheParameter);
			if (value != null) {
				return Boolean.parseBoolean(value);
			} else {
				return defaultIgnoreCache;
			}
		}
		
		public String setIgnoreInternet(String uriString, boolean ignoreInternet) {
			return setParameter(uriString, ignoreInternetParameter, String.valueOf(ignoreInternet));
		}
		
		public boolean isIgnoreInternet(String uriString) {
			String value = getParameter(uriString, ignoreInternetParameter);
			if (value != null) {
				return Boolean.parseBoolean(value);
			} else {
				return defaultIgnoreInternet;
			}
		}
		
		public String setAction(String uriString, String action) {
			return setParameter(uriString, actionParameter, action);
		}
		
		public String getAction(String uriString) {
			String value = getParameter(uriString, actionParameter);
			if (value != null) {
				return value;
			} else {
				return defaultAction;
			}
		}
		
		public String removeParameters(String uriString) {
			int queryStart = uriString.lastIndexOf('?');
			if (queryStart > -1) {
				return uriString.substring(0, queryStart);
			} else {
				return uriString;
			}
			
		}
	}
	
	abstract class MetamapCursor extends AbstractCursor {
		protected MetaMapItem[] currentLevel;
		private MetaMapItem currentItem;
		
		@Override
		public String[] getColumnNames() {
			return new String[] { MetaMapItem.COLUMN_NAME, MetaMapItem.COLUMN_DESCRIPTION,
					MetaMapItem.COLUMN_IS_FOLDER, MetaMapItem.COLUMN_REFERENCE,
					MetaMapItem.COLUMN_LAST_SYNCHRONIZATION_DATE, MetaMapItem.COLUMN_SIZE_IN_BYTES };
		}
		
		@Override
		public int getCount() {
			return currentLevel.length;
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
			switch (column) {
				case 5:
					return currentItem.sizeInBytes;
				default:
					throw new IllegalArgumentException("No such column or illegal column type, column: " + column);
			}
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
					return currentItem.name;
				case 1:
					return currentItem.description;
				case 2:
					return String.valueOf(currentItem.isFolder);
				case 3:
					return currentItem.reference;
				case 4:
					if (currentItem.lastSynchronizationDate != null) {
						return currentItem.lastSynchronizationDate.toString();
					} else {
						return null;
					}
				case 5:
					return currentItem.sizeInBytes + "";
				default:
					throw new IllegalArgumentException("No such column or illegal column type, column: " + column);
			}
		}
		
		@Override
		public boolean onMove(int oldPosition, int newPosition) {
			if (newPosition >= 0 && newPosition < currentLevel.length) {
				currentItem = currentLevel[newPosition];
				return true;
			} else {
				return false;
			}
		}
		
		@Override
		public boolean isNull(int column) {
			throw new NotImplementedException();
		}
		
	}
	
	abstract class MapCursor extends AbstractCursor {
		protected boolean mapNotFound;
		protected String id;
		protected String text;
		
		@Override
		public String[] getColumnNames() {
			return new String[] { ID, CONTENT };
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
			if (mapNotFound)
				throw new MapNotFoundException();
			
			switch (column) {
				case 0:
					return id;
				case 1:
					return text;
				default:
					throw new IllegalArgumentException("No such column " + column);
			}
		}
		
		@Override
		public boolean isNull(int column) {
			throw new NotImplementedException();
		}
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new NotImplementedException();
	}
	
	@Override
	public String getType(Uri uri) {
		throw new NotImplementedException();
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new NotImplementedException();
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		throw new NotImplementedException();
	}
}
