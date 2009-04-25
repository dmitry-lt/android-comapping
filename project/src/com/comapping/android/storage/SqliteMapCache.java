package com.comapping.android.storage;

import com.comapping.android.Log;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class SqliteMapCache {
	private static final String DATABASE_NAME = "mapCache";
	private static final String TABLE_NAME = "cache";

	private static final String ID_ATTR_NAME = "id";
	private static final String LAST_UPDATE_ATTR_NAME = "lastUpdate";
	private static final String MAP_ID_ATTR_NAME = "mapId";
	private static final String DATA_ATTR_NAME = "data";

	private static final String CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + 
			ID_ATTR_NAME + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
			MAP_ID_ATTR_NAME + " TEXT, " + 
			LAST_UPDATE_ATTR_NAME+ " DATE DEFAULT CURRENT_DATE, " + 
			DATA_ATTR_NAME + " TEXT);";
	private static final String DELETE_TABLE_QUERY = "DROP TABLE IF EXISTS "+TABLE_NAME;
	
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

		Log.d(Log.sqliteCacheTag, "set attributes "+values);
		
		long result = -1;
		try {
			result = database.insertOrThrow(TABLE_NAME, null, values);
		} catch(SQLException e) {
			result = -1;
			Log.e(Log.sqliteCacheTag, "sql exception while insert");
		}
		
		if (result == -1) {
			// insert not successful, update required
			database.update(TABLE_NAME, values, "mapId=?", new String[] { mapId });
		}
	}

	public String get(String mapId) {
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
        
        Cursor result = database.query(TABLE_NAME, new String[]{DATA_ATTR_NAME}, whereCondition, new String[]{ mapId }, null, null, null);
        
        if (result != null) {
        	if (result.moveToFirst()) {
        		String res = result.getString(result.getColumnIndex(DATA_ATTR_NAME));
        		Log.d(Log.sqliteCacheTag, "getting result "+res);
        		return res;
        	}
        }
        
        return null;
	}

	public String getSetDate(String mapId) {
		return "date!";
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