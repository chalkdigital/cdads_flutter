package com.chalkdigital.common.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.os.Build;

import com.chalkdigital.ads.CDAds;
import com.chalkdigital.common.CDAdConstants;
import com.chalkdigital.common.CDAdDeviceInfo;
import com.chalkdigital.common.CDAdLocation;
import com.chalkdigital.common.Constants;
import com.chalkdigital.common.DataKeys;
import com.chalkdigital.common.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by arungupta on 27/12/16.
 */

public class CDDatabaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "CDAds.db";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + CDLocationEntry.TABLE_NAME + " (" +
                    CDLocationEntry._ID + " INTEGER PRIMARY KEY," +
                    CDLocationEntry.COLUMN_NAME_LATITUDE + " REAL," +
                    CDLocationEntry.COLUMN_NAME_LONGITUDE + " REAL,"+
                    CDLocationEntry.COLUMN_NAME_ALTITUDE + " REAL," +
                    CDLocationEntry.COLUMN_NAME_VERTICALACCURACY + " REAL," +
                    CDLocationEntry.COLUMN_NAME_IPADDRESS + " VARCHAR," +
                    CDLocationEntry.COLUMN_NAME_SPEED + " REAL," +
                    CDLocationEntry.COLUMN_NAME_EPOCTIME + " LONGINTEGER," +
                    CDLocationEntry.COLUMN_NAME_TIMEZONE + " VARCHAR," +
                    CDLocationEntry.COLUMN_NAME_CONNECTIONTYPE + " INTEGER," +
                    CDLocationEntry.COLUMN_NAME_DWELLTIME + " LONGINTEGER," +
                    CDLocationEntry.COLUMN_NAME_HORIZONTAL_ACCURACY + " VARCHAR," +
                    CDLocationEntry.COLUMN_NAME_PROVIDER + " VARCHAR" + ")";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + CDLocationEntry.TABLE_NAME;

    public CDDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (db.isOpen()){
            db.execSQL(SQL_CREATE_ENTRIES);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void saveLocation(Location location, String loctype, Context context){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        if (sqLiteDatabase.isOpen()){

            long dwellTime = 0;
            Cursor cursor = sqLiteDatabase.query(CDLocationEntry.TABLE_NAME, new String[] {CDLocationEntry._ID,
                    CDLocationEntry.COLUMN_NAME_LATITUDE,
                    CDLocationEntry.COLUMN_NAME_LONGITUDE,
                    CDLocationEntry.COLUMN_NAME_EPOCTIME,
                    CDLocationEntry.COLUMN_NAME_HORIZONTAL_ACCURACY,
                    CDLocationEntry.COLUMN_NAME_VERTICALACCURACY,
                    CDLocationEntry.COLUMN_NAME_CONNECTIONTYPE,
                    CDLocationEntry.COLUMN_NAME_IPADDRESS,
                    CDLocationEntry.COLUMN_NAME_SPEED,
                    CDLocationEntry.COLUMN_NAME_ALTITUDE,
                    CDLocationEntry.COLUMN_NAME_DWELLTIME,
                    CDLocationEntry.COLUMN_NAME_TIMEZONE,
                    CDLocationEntry.COLUMN_NAME_PROVIDER}, null, null, null, null, CDLocationEntry.COLUMN_NAME_EPOCTIME+" DESC", ""+1);
            CDAdLocation lastLocation = null;
            if (cursor.moveToFirst()) {
                do {
                    lastLocation = new CDAdLocation(new Location(cursor.getString(12)));
                    lastLocation.setLatitude(cursor.getDouble(1));
                    lastLocation.setLongitude(cursor.getDouble(2));
                    lastLocation.setAccuracy((float) cursor.getDouble(4));
                    lastLocation.setDwellTime(cursor.getLong(10));
                    lastLocation.setTime(cursor.getLong(3)*1000);
                } while (cursor.moveToNext());
            }

            if (lastLocation != null){
                if (new CDAdLocation(location).distanceTo(lastLocation) < CDAds.runningInstance().getCdAdsInitialisationParams().getDistanceFilter()){
                    dwellTime = (location.getTime() - lastLocation.getTime())/1000;

                    if (dwellTime >0){
                        if (dwellTime < CDAdConstants.CDAdMaxDwellTime){
                            if (lastLocation.getDwellTime()>0)
                                dwellTime = dwellTime + lastLocation.getDwellTime();

                        }else{
                            return;
                        }
                    }

                }
            }

            cursor = sqLiteDatabase.rawQuery("select COUNT(*) from "+CDLocationEntry.TABLE_NAME, null);
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            cursor.close();
            if (count>500){
                sqLiteDatabase.execSQL("DELETE FROM "+CDLocationEntry.TABLE_NAME+" WHERE "+CDLocationEntry._ID+" IN (SELECT "+CDLocationEntry._ID+" FROM "+CDLocationEntry.TABLE_NAME+" ORDER BY "+CDLocationEntry.COLUMN_NAME_EPOCTIME+" ASC LIMIT 1)");
            }
            ContentValues values = new ContentValues();
            values.put(CDLocationEntry.COLUMN_NAME_LATITUDE, location.getLatitude());
            values.put(CDLocationEntry.COLUMN_NAME_LONGITUDE, location.getLongitude());
            values.put(CDLocationEntry.COLUMN_NAME_EPOCTIME, location.getTime()/1000);
            values.put(CDLocationEntry.COLUMN_NAME_HORIZONTAL_ACCURACY, loctype.equals(Constants.CDLocTypeIP)?"IP": String.format("%.2f", location.getAccuracy()));
            values.put(CDLocationEntry.COLUMN_NAME_PROVIDER, Utils.getLocationType(location));
            values.put(CDLocationEntry.COLUMN_NAME_ALTITUDE, location.getAltitude());
            values.put(CDLocationEntry.COLUMN_NAME_HORIZONTAL_ACCURACY, location.getAccuracy());
            if (Build.VERSION.SDK_INT >= 26)
                values.put(CDLocationEntry.COLUMN_NAME_VERTICALACCURACY, location.getVerticalAccuracyMeters());
            else
                values.put(CDLocationEntry.COLUMN_NAME_VERTICALACCURACY, 0);
            values.put(CDLocationEntry.COLUMN_NAME_HORIZONTAL_ACCURACY, location.getAccuracy());
            values.put(CDLocationEntry.COLUMN_NAME_IPADDRESS, "");
            values.put(CDLocationEntry.COLUMN_NAME_CONNECTIONTYPE, CDAdDeviceInfo.deviceInfo(context).getConnectiontype());
            values.put(CDLocationEntry.COLUMN_NAME_DWELLTIME, dwellTime);
            sqLiteDatabase.insert(CDLocationEntry.TABLE_NAME, null, values);
        }
    }

    public HashMap<String, Object> getLocations(int limit){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        if (sqLiteDatabase.isOpen()){
            Cursor cursor = sqLiteDatabase.query(CDLocationEntry.TABLE_NAME, new String[] {CDLocationEntry._ID,
                    CDLocationEntry.COLUMN_NAME_LATITUDE,
                    CDLocationEntry.COLUMN_NAME_LONGITUDE,
                    CDLocationEntry.COLUMN_NAME_EPOCTIME,
                    CDLocationEntry.COLUMN_NAME_HORIZONTAL_ACCURACY,
                    CDLocationEntry.COLUMN_NAME_VERTICALACCURACY,
                    CDLocationEntry.COLUMN_NAME_CONNECTIONTYPE,
                    CDLocationEntry.COLUMN_NAME_IPADDRESS,
                    CDLocationEntry.COLUMN_NAME_SPEED,
                    CDLocationEntry.COLUMN_NAME_ALTITUDE,
                    CDLocationEntry.COLUMN_NAME_DWELLTIME,
                    CDLocationEntry.COLUMN_NAME_TIMEZONE,
                    CDLocationEntry.COLUMN_NAME_PROVIDER}, null, null, null, null, CDLocationEntry.COLUMN_NAME_EPOCTIME+" DESC", ""+limit);
            HashMap<String, Object> params = new HashMap<>();
            ArrayList<Object[]> locations = new ArrayList<>();
            String[] ids = new String[cursor.getCount()];
            if (cursor.moveToFirst()) {
                do {
                    Object[] location = {cursor.getDouble(1), cursor.getDouble(2), cursor.getDouble(9), cursor.getDouble(8), cursor.getDouble(4), cursor.getDouble(5), cursor.getLong(3), cursor.getString(11), cursor.getInt(6), cursor.getInt(12), cursor.getLong(10), ""};
                    locations.add(location);
                    ids[cursor.getPosition()] = cursor.getString(0);
                } while (cursor.moveToNext());
            }
            params.put(DataKeys.LOCATION_DATA, locations);
            params.put(DataKeys.IDS, ids);
            return params;
        }
        return null;
    }

    public int deleteLocations(String[] ids){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        if (sqLiteDatabase.isOpen()) {
           return sqLiteDatabase.delete(CDLocationEntry.TABLE_NAME,
                    CDLocationEntry._ID+" IN (" + new String(new char[ids.length - 1]).replace("\0", "?,") + "?)",
                    ids);
        }
        return 0;
    }

    public void close(){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        if (sqLiteDatabase.isOpen())
            sqLiteDatabase.close();
    }

}
