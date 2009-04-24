package com.comapping.android.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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
		if (database == null) {
			return;
		}
		
		ContentValues values = new ContentValues();

		values.put(ID_ATTR_NAME, getIdAttribute(mapId));
		values.put(MAP_ID_ATTR_NAME, mapId);
		values.put(DATA_ATTR_NAME, data);

		if (database.insert(TABLE_NAME, null, values) == -1) {
			// insert not successful, update required
			database.update(TABLE_NAME, values, "mapId=?", new String[] { mapId });
		}
	}

	public String get(String mapId) {
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
        		return result.getString(result.getColumnIndex(DATA_ATTR_NAME));
        	}
        }
        
        return null;
	}

	public String getSetDate(String mapId) {
		return "date!";
	}

	// private methods
	private Integer getIdAttribute(String mapId) {
		if (mapId.equals("meta")) {
			return 0;
		} else {
			try {
				return Integer.parseInt(mapId);
			} catch (NumberFormatException e) {
				return null;
			}
		}
	}

	public void clear() {
		if (database != null) {
			database.execSQL(DELETE_TABLE_QUERY);
			initDatabase();
		}
	}
	
	private void initDatabase() {
		if (database != null) {
			// database successfully opened or created
			database.execSQL(CREATE_TABLE_QUERY);
		}
	}
}
