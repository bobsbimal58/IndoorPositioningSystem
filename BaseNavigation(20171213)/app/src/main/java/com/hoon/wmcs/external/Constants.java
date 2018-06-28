package com.hoon.wmcs.external;

import android.hardware.SensorManager;

/**
 * Created by WMCS on 2017-07-28.
 */

public class Constants {
    // Camera request code
    public static final int MY_PERMISSION_REQUEST_CODE = 100;

    // IMU Sensor samplingrate
    public static final int RATE = SensorManager.SENSOR_DELAY_GAME;

    //Socket IP, port number
    public static final String IP = "117.16.23.124";
    public static final int PORT = 5545;

    //Database
    public static final String DB_NAME = "ARnavigation";
    public static final String TABLE_NAME = "BeaconData";
    public static final String TABLENAME = "Fingerprint";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "X";
    public static final String COL_3 = "Y";
    public static final String COL_4 = "Beacon";
    public static final String COL_5 = "Rssi";
    public static final String COL_6 = "Floor";
    public static final String ID = "id";
    public static final String FLOOR = "FLOOR";
    public static final String COLXAXIS = "COLXAXIS";
    public static final String COLYAXIS = "COLYAXIS";
    public static final String COLZAXIS = "COLZAXIS";
    public static final String MAPX = "COORX";
    public static final String MAPY = "COORY";
    public static final String MAPXM = "COORXM";
    public static final String MAPYM = "COORYM";
    public static final String FINAL = "NORM";


    //filters Values
    public static final float ALPHA = 0.8f;
    public static final float MAX_THRES = 10.5f;
    public static final float MIN_THRES = 9.5f;
    public static final float K = 0.35f;
    public static final float NS2S = 1.0f / 1000000000.0f;

    //Socket
    public static final String END = "end";

    //Mode & AR or 3D Map
    public static final int MODE1 = 1;
    public static final int MODE2 = 2;
    public static final int MODE3 = 3;

    public static final int AR = 1;
    public static final int MAP3D = 2;
}
