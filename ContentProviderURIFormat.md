# Content Provider uri format #


## Comapping Provider: ##

`"content://www.comapping.com/maps/{path}/"` - to receive comaps list in {path} directory. It returns the cursor with records that represents files and folders in {path} folder. Each record represents MetaMapItem object and have corresponding column named as special constants in MetaMapItem class. This is example to receive `name` field: `cursor.getString(cursor.getColumnIndex(MetaMapItem.COLUMN_NAME))`.

`"content://www.comapping.com/{mapid}"` - to receive comap with id {mapid}. It returns the cursor with one record that consist of two column "id" and "content" that represents mapId and map's content as `String`. `cursor.getString(cursor.getColumnIndex("content"))` - to receive map's content as `String` from retrieved cursor.
`LoginInterruptedRuntimeException`, `ConnectionRuntimeException`, `MapNotFoundException` exceptions can be thrown.

`"content://www.comapping.com/{mapid}?action=getMapSizeInBytes"` - to receive map size. It returns the cursor with one record that have column "sizeInBytes".
`cursor.getString(cursor.getColumnIndex(MetaMapItem.COLUMN_SIZE_IN_BYTES))`.

// now it doesn't working
`"content://www.comapping.com/{mapid}?action=startDownloading"` - to start downloading map from server, progress bar will be shown in notifications panel. It returns `null`.

`"content://www.comapping.com/login"` - to login. At first autologin attempt will be performed, if it will fail the login dialog will be shown. If user is already logged in nothing will happen. It returns the cursor with one record that consist of one column "clientId". `cursor.getString(cursor.getColumnIndex("clientId"))` - to receive clientId from retrieved cursor.
`LoginInterruptedRuntimeException` exception can be thrown.

`"content://www.comapping.com/logout"` - to logout. All cached data will be cleared. It returns `null`.

`"content://www.comapping.com/sync"` - to update metamap from server. It returns `null`.

`"content://www.comapping.com/finish_work"` - on application close.
(If user set "remember me" only memory cache will be cleared, otherwise logout will be performed. It returns `null`.

Parameters: `ignoreCache`, `ignoreInternet`

Parameters can be added after ? token. Parameters must be separated by & token.

For example: `"content://www.comapping.com/maps/?ignoreCache=true&ignoreInternet=false"`

## SDCard Provider: ##

`"content://sdcard/{path}/"` - to receive comaps list in {path} directory. It returns the cursor with records that represents files and folders in {path} folder. Each record represents MetaMapItem object and have corresponding column named as special constants in MetaMapItem class. This is example to receive `name` field: `cursor.getString(cursor.getColumnIndex(MetaMapItem.COLUMN_NAME))`.

`"content://sdcard/{path}/{mapname}.comap"` - to receive comap with id {mapid}. It returns the cursor with one record that consist of two column "id" and "content" that represents mapId and map's content as `String`. `cursor.getString(cursor.getColumnIndex("content"))` - to receive map's content as `String` from retrieved cursor.
> `MapNotFoundException` exception can be thrown.

`"content://sdcard/{path}/{mapname}.comap?action=getMapSizeInBytes"` - to receive map size. It returns the cursor with one record that have column "sizeInBytes".
`cursor.getString(cursor.getColumnIndex(MetaMapItem.COLUMN_SIZE_IN_BYTES))`.