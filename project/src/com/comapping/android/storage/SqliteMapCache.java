package com.comapping.android.storage;

import java.sql.Timestamp;
import java.util.Calendar;

import com.comapping.android.Log;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class SqliteMapCache {
	final static private String DATABASE_NAME = "mapCache";
	final static private String TABLE_NAME = "cache_v2";

	final static private String ID_ATTR_NAME = "id";
	final static private String LAST_UPDATE_ATTR_NAME = "lastUpdate";
	final static private String MAP_ID_ATTR_NAME = "mapId";
	final static private String DATA_ATTR_NAME = "data";

	final static private String CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + 
			ID_ATTR_NAME + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
			MAP_ID_ATTR_NAME + " TEXT, " + 
			LAST_UPDATE_ATTR_NAME+ " TIMESTAMP, " + 
			DATA_ATTR_NAME + " TEXT);";
	final static private String DELETE_TABLE_QUERY = "DROP TABLE IF EXISTS "+TABLE_NAME;
	
	private SQLiteDatabase database = null;

	public SqliteMapCache(Context context) {
		database = context.openOrCreateDatabase(DATABASE_NAME, Context.MODE_WORLD_READABLE, null);

		// id = mapId if mapId is number, and id = 0 if mapId == "metamap"
		initDatabase();
	}

	public void set(String mapId, String data) {
		Log.d(Log.sqliteCacheTag, "set ["+mapId+"] = "+data);
		if (database == null) {
			return;
		}
		
		ContentValues values = new ContentValues();

		values.put(ID_ATTR_NAME, getIdAttribute(mapId));
		values.put(MAP_ID_ATTR_NAME, mapId);
		values.put(DATA_ATTR_NAME, data);
		values.put(LAST_UPDATE_ATTR_NAME, new Timestamp(Calendar.getInstance().getTimeInMillis()).toString());

		Log.d(Log.sqliteCacheTag, "set attributes "+values);
		
		database.delete(TABLE_NAME, "mapId=?", new String[]{ mapId });
		
		try {
			database.insertOrThrow(TABLE_NAME, null, values);
		} catch(SQLException e) {
			Log.e(Log.sqliteCacheTag, "sql exception while insert");
		}
	}

	private Cursor getMapCursor(String mapId) {
		Log.d(Log.sqliteCacheTag, "get ["+mapId+"]");
		
		if (database == null) {
			return null;
		}
		
        String whereCondition = null;
        
        if (getIdAttribute(mapId) != null) {
        	whereCondition = ID_ATTR_NAME+"=?";
        } else {
        	whereCondition = MAP_ID_ATTR_NAME+"=?";
        }
        
        return database.query(TABLE_NAME, new String[]{DATA_ATTR_NAME, LAST_UPDATE_ATTR_NAME}, whereCondition, new String[]{ mapId }, null, null, null);
	}
	
	public String get(String mapId) {
		Cursor result = getMapCursor(mapId);
		
        if (result != null) {
        	if (result.moveToFirst()) {
        		String res = result.getString(result.getColumnIndex(DATA_ATTR_NAME));
        		Log.d(Log.sqliteCacheTag, "getting result "+res);
        		return res;
        	}
        }
        
        return null;
	}

	public Timestamp getLastSynchronizationDate(String mapId) {
		Cursor result = getMapCursor(mapId);
		
        if (result != null) {
        	if (result.moveToFirst()) {
        		String res = result.getString(result.getColumnIndex(LAST_UPDATE_ATTR_NAME));
        		Log.d(Log.sqliteCacheTag, "getting result "+res);
        		
        		return Timestamp.valueOf(res);
        	}
        }
        
        return null;
	}

	// private methods
	private Integer getIdAttribute(String mapId) {
		return null;
		/*try {
			return Integer.parseInt(mapId);
		} catch (NumberFormatException e) {
			return null;
		}*/	
	}

	public void clear() {
		Log.d(Log.sqliteCacheTag, "clear database");
		if (database != null) {
			database.execSQL(DELETE_TABLE_QUERY);
			initDatabase();
		}
	}
	
	public void close() {
		database.close();
	}
	
	private void initDatabase() {
		if (database != null) {
			// database successfully opened or created
			database.execSQL(CREATE_TABLE_QUERY);
		}
	}
}