package com.comapping.android.provider.contentprovider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.net.Uri;

import com.comapping.android.metamap.MetaMapItem;

public abstract class MapContentProvider extends ContentProvider {
	public static final String ID = "id";
	public static final String TEXT = "content";

	public static String getComap(String mapRef, Context context) {
		Uri fullUri = Uri.parse(mapRef);
		Cursor cursor = context.getContentResolver().query(fullUri, null, null, null, null);

		return cursor.getString(1);
	}

	public static class MapContentProviderInfo {
		public final String authorities;
		public final String separator;
		public final String root;
		public final String relRoot;
		public final String logout;
		public final String relLogout;
		public final String sync;
		public final String relSync;
		public final boolean canLogout;
		public final boolean canSync;

		public MapContentProviderInfo(String authorities, String relRoot, boolean canLogout, boolean canSync) {
			this(authorities, "/", relRoot, "logout", "sync", canLogout, canSync);
		}

		public MapContentProviderInfo(String authorities, String separator, String relRoot, String relLogout,
				String relSync, boolean canLogout, boolean canSync) {
			this.authorities = authorities;
			this.separator = separator;
			
			if (relRoot.equals("")) {
				this.root = authorities + separator;
			} else {
				this.root = authorities + separator + relRoot + separator;
			}
			
			this.relRoot = relRoot;
			this.logout = authorities + separator + relLogout;
			this.relLogout = relLogout;
			this.sync = authorities + separator + relSync;
			this.relSync = relSync;
			this.canLogout = canLogout;
			this.canSync = canSync;
		}
	}

	abstract class MetamapCursor extends AbstractCursor {
		protected MetaMapItem[] currentLevel;
		private MetaMapItem currentItem;

		@Override
		public String[] getColumnNames() {
			return new String[] { MetaMapItem.COLUMN_NAME, MetaMapItem.COLUMN_DESCRIPTION,
					MetaMapItem.COLUMN_IS_FOLDER, MetaMapItem.COLUMN_REFERENCE };
		}

		@Override
		public int getCount() {
			return currentLevel.length;
		}

		@Override
		public double getDouble(int column) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public float getFloat(int column) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getInt(int column) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public long getLong(int column) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public short getShort(int column) {
			// TODO Auto-generated method stub
			return 0;
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
				default:
					throw new IllegalArgumentException("No such column " + column);
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
			// TODO Auto-generated method stub
			return false;
		}

	}

	abstract class MapCursor extends AbstractCursor {
		protected String id;
		protected String text;

		@Override
		public String[] getColumnNames() {
			return new String[] { ID, TEXT };
		}

		@Override
		public int getCount() {
			return 1;
		}

		@Override
		public double getDouble(int column) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public float getFloat(int column) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getInt(int column) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public long getLong(int column) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public short getShort(int column) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public String getString(int column) {
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
			// TODO Auto-generated method stub
			return false;
		}		
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
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
}
