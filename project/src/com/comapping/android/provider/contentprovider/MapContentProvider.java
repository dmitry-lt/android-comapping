package com.comapping.android.provider.contentprovider;

import android.app.Activity;
import android.content.ContentProvider;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.net.Uri;

public abstract class MapContentProvider extends ContentProvider {
	public static final String ID = "id";
	public static final String TEXT = "content";
	
	public static Activity currentContext;
	
	public static String getComap(String mapId, Uri providerUri, Activity context) {
		currentContext = context;
		
		Uri fullUri = Uri.withAppendedPath(providerUri, mapId);
		Cursor cursor = context.managedQuery(fullUri, null, null, null, null);
				
		return cursor.getString(cursor.getColumnIndex(TEXT));
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
			return 2;
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
}
