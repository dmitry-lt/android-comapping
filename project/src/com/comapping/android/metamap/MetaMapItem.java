package com.comapping.android.metamap;

import java.sql.Timestamp;

public class MetaMapItem
{
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_DESCRIPTION = "description";
	public static final String COLUMN_IS_FOLDER = "isFolder";
	public static final String COLUMN_REFERENCE = "reference";	
	public static final String COLUMN_LAST_SYNCHRONIZATION_DATE = "lastSynchronizationDate";
	public static final String COLUMN_SIZE_IN_BYTES = "sizeInBytes";	
	
	public String name;	
	public String description;
	public boolean isFolder;
	public String reference;
	public Timestamp lastSynchronizationDate;
	public int sizeInBytes;
	
}