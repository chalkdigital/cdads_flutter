package com.chalkdigital.common.db;

import android.provider.BaseColumns;

/**
 * Created by arungupta on 27/12/16.
 */

public class CDLocationEntry implements BaseColumns {
    public static final String TABLE_NAME = "cdlocations";
    public static final String COLUMN_NAME_LATITUDE = "lat";
    public static final String COLUMN_NAME_LONGITUDE = "lon";
    public static final String COLUMN_NAME_DATE = "date";
    public static final String COLUMN_NAME_HORIZONTAL_ACCURACY = "horizotalaccuracy";
    public static final String COLUMN_NAME_PROVIDER = "provider";
    public static final String COLUMN_NAME_ALTITUDE = "altitude";
    public static final String COLUMN_NAME_CONNECTIONTYPE = "connectiontype";
    public static final String COLUMN_NAME_DWELLTIME = "dwelltime";
    public static final String COLUMN_NAME_EPOCTIME = "epoctime";
    public static final String COLUMN_NAME_HORIZONTALACCURACY = "horizontalaccuracy";
    public static final String COLUMN_NAME_IPADDRESS = "ipaddress";
    public static final String COLUMN_NAME_SPEED = "speed";
    public static final String COLUMN_NAME_TIMEZONE = "timezone";
    public static final String COLUMN_NAME_VERTICALACCURACY = "verticalaccuracy";

}
