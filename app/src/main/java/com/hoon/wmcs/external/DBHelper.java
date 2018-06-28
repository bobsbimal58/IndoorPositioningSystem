package com.hoon.wmcs.external;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import static com.hoon.wmcs.external.Constants.COLXAXIS;
import static com.hoon.wmcs.external.Constants.COLYAXIS;
import static com.hoon.wmcs.external.Constants.COLZAXIS;
import static com.hoon.wmcs.external.Constants.COL_1;
import static com.hoon.wmcs.external.Constants.COL_2;
import static com.hoon.wmcs.external.Constants.COL_3;
import static com.hoon.wmcs.external.Constants.COL_4;
import static com.hoon.wmcs.external.Constants.COL_5;
import static com.hoon.wmcs.external.Constants.COL_6;
import static com.hoon.wmcs.external.Constants.DB_NAME;
import static com.hoon.wmcs.external.Constants.FINAL;
import static com.hoon.wmcs.external.Constants.FLOOR;
import static com.hoon.wmcs.external.Constants.MAPX;
import static com.hoon.wmcs.external.Constants.MAPY;
import static com.hoon.wmcs.external.Constants.TABLENAME;
import static com.hoon.wmcs.external.Constants.TABLE_NAME;

/**
 * Created by WMCS on 2017-08-21.
 */

public class DBHelper extends SQLiteOpenHelper{
    private static final String TAG = DBHelper.class.getSimpleName();

    private static final int DATABASE_VERSION = 4;

    private float beacon_x, beacon_y;

    public DBHelper(Context context){
        super(context, DB_NAME, null, DATABASE_VERSION);

        beacon_x = 0.0f;
        beacon_y = 0.0f;
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        String createTable = "create table " + TABLE_NAME + " (ID INTEGER, X INTEGER, Y INTEGER, Beacon TEXT, Rssi INTEGER, Floor INTEGER)";
        String magtable = "CREATE TABLE " + TABLENAME + "("
                +FLOOR + " INTEGER ," +
                MAPX + " INTEGER, " +
                MAPY + " INTEGER, " +
                COLXAXIS + " REAL, " +
                COLYAXIS + " REAL, " +
                COLZAXIS + " REAL, " +
                FINAL + " REAL)";

        db.execSQL(createTable);
        db.execSQL(magtable);
        //onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLENAME);
        this.onCreate(db);
    }

    public void dropTable(){
        SQLiteDatabase db;

        db = this.getWritableDatabase();

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLENAME);
        this.onCreate(db);

        db.close();
    }

    public void insertData(String sql){
        SQLiteDatabase db;

        db = this.getWritableDatabase();

        db.execSQL("");
    }

    public Cursor getSelectedData(float imuX, float imuY){
        Cursor cursor;
        SQLiteDatabase db;
        int limitX =16;
        int limitY = 16;


        float beaconXcorMin = imuX-limitX;
        float beaconXcorMax = imuX+limitX;
        float beaconYcorMin = imuY-limitY;
        float beaconYcorMax = imuY+limitY;

        db = this.getReadableDatabase();

        String que = "SELECT * FROM BeaconData WHERE X BETWEEN '" +beaconXcorMin+"' AND '"+beaconXcorMax+"' "+ "AND " +"Y BETWEEN '" +beaconYcorMin+"' AND '"+beaconYcorMax+"'";

        cursor = db.rawQuery(que, null);

        Log.e(TAG, que);


        return cursor;
    }

    public void setBeaconPos(float x, float y){
        beacon_x = x;
        beacon_y = y;
    }

    public Cursor getResult(){
        int n = 2;

        SQLiteDatabase db = this.getWritableDatabase();
        float xmin = beacon_x - n;
        float xmax= beacon_x + n;
        float ymin= beacon_y - n;
        float ymax= beacon_y + n;

        Cursor mag = db.rawQuery("SELECT * FROM " + TABLENAME + " WHERE X_CORD BETWEEN '" +xmin+"' AND '"+xmax+"' "+ "AND " +"Y_CORD BETWEEN '" +ymin+"' AND '"+ymax+"' ",null);

        return mag;
    }

    public void setBeaconXY(float x, float y){
        beacon_x = x;
        beacon_y = y;
    }

    public void showBeacon(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.rawQuery("select * from " + TABLE_NAME, null);

        while(cur.moveToNext()){
            //Log.e(TAG, cur.getInt(0) + "," + cur.getInt(1) + "," + cur.getInt(2) + "," + cur.getString(3) + "," + cur.getInt(4));
        }
    }

    public void showMag(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.rawQuery("select * from " + TABLENAME, null);

        while(cur.moveToNext()){
            //Log.e(TAG, cur.getInt(0) + "," + cur.getInt(1) + "," + cur.getInt(2) + "," + cur.getInt(3) + "," + cur.getInt(4) + "," + cur.getInt(5) + "," + cur.getInt(6)+ "," + cur.getDouble(7));
        }
    }

    public Cursor getBeaconData() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.rawQuery("select * from " + TABLE_NAME, null);

        return cur;
    }
    public Cursor getMagdata() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.rawQuery("select * from " + TABLENAME, null);

        return cur;
    }

    public boolean insertBeacon(int id, int x, int y, String beacon, int rssi, int floor) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, id);
        contentValues.put(COL_2, x);
        contentValues.put(COL_3, y);
        contentValues.put(COL_4, beacon);
        contentValues.put(COL_5, rssi);
        contentValues.put(COL_6, floor);


        long result = db.insert(TABLE_NAME, null, contentValues);
        db.close();

        if(result == -1)
            return false;
        else
            return true;

    }

    public  boolean insertMag(int floor, int b, int c, float x, float y, float z, float d) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentvalues = new ContentValues();
        contentvalues.put(FLOOR, floor);
        contentvalues.put(COLXAXIS, b);
        contentvalues.put(COLYAXIS, c);
        contentvalues.put(COLZAXIS, x);
        contentvalues.put(MAPX, y);
        contentvalues.put(MAPY, z);
        contentvalues.put(FINAL, d);


        long result = db.insert(TABLENAME, null, contentvalues);
        db.close();



        if(result == -1)
            return false;
        else
            return true;
    }

    public boolean insertMag(String msg){
        String [] tmp = msg.split(",");

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentvalues = new ContentValues();
        contentvalues.put(FLOOR, Integer.parseInt(tmp[0]));
        contentvalues.put(COLXAXIS, Double.parseDouble(tmp[5]));
        contentvalues.put(COLYAXIS, Double.parseDouble(tmp[6]));
        contentvalues.put(COLZAXIS, Double.parseDouble(tmp[7]));
        contentvalues.put(MAPX, Integer.parseInt(tmp[1]));
        contentvalues.put(MAPY, Integer.parseInt(tmp[2]));
        contentvalues.put(FINAL, Double.parseDouble(tmp[8]));



        long result = db.insert(TABLENAME, null, contentvalues);
        db.close();

        return false;
    }

    public boolean insertBeacon(String msg){
        String [] tmp = msg.split(",");

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, Integer.parseInt(tmp[1]));
        contentValues.put(COL_2, Integer.parseInt(tmp[2]));
        contentValues.put(COL_3, Integer.parseInt(tmp[3]));
        contentValues.put(COL_4, tmp[6]);
        contentValues.put(COL_5, Integer.parseInt(tmp[7]));
        contentValues.put(COL_6, Integer.parseInt(tmp[0]));


        long result = db.insert(TABLE_NAME, null, contentValues);
        db.close();

        if(result == -1)
            return false;
        else
            return true;
    }

    //DB MAX
    public long getMaxY(float imuX, float imuY) {

        int limitX = 3;
        int limitY = 3;
        float beaconXcorMin = imuX - limitX;
        float beaconXcorMax = imuX + limitX;
        float beaconYcorMin = imuY - limitY;
        float beaconYcorMax = imuY + limitY;
        SQLiteDatabase db = this.getReadableDatabase();
        final SQLiteStatement stmt = db.compileStatement("SELECT MAX(Y) FROM BeaconData WHERE Y BETWEEN '" + beaconYcorMin + "' AND '" + beaconYcorMax + "' ");


        //float maxV = max.getFloat(2);

        // Log.e("Max Data", " Data is :----------"+maxV);

        return stmt.simpleQueryForLong();
    }


    public long getMaxX(float imuX, float imuY) {

        int limitX = 3;
        int limitY = 3;
        float beaconXcorMin = imuX - limitX;
        float beaconXcorMax = imuX + limitX;
        float beaconYcorMin = imuY - limitY;
        float beaconYcorMax = imuY + limitY;
        SQLiteDatabase db = this.getReadableDatabase();
        final SQLiteStatement stmt = db.compileStatement("SELECT MAX(X) FROM BeaconData WHERE Y BETWEEN '" + beaconXcorMin + "' AND '" + beaconXcorMax + "' ");


        //float maxV = max.getFloat(2);

        // Log.e("Max Data", " Data is :----------"+maxV);

        return stmt.simpleQueryForLong();
    }


//DB MIN

    public long getMinY(float imuX, float imuY) {

        int limitX = 3;
        int limitY = 3;
        float beaconXcorMin = imuX - limitX;
        float beaconXcorMax = imuX + limitX;
        float beaconYcorMin = imuY - limitY;
        float beaconYcorMax = imuY + limitY;
        SQLiteDatabase db = this.getReadableDatabase();
        final SQLiteStatement stmt = db.compileStatement("SELECT MAX(Y) FROM BeaconData WHERE Y BETWEEN '" + beaconYcorMin + "' AND '" + beaconYcorMax + "' ");


        //float maxV = max.getFloat(2);

        // Log.e("Max Data", " Data is :----------"+maxV);

        return stmt.simpleQueryForLong();
    }


    public long getMinX(float imuX, float imuY) {

        int limitX = 3;
        int limitY = 3;
        float beaconXcorMin = imuX - limitX;
        float beaconXcorMax = imuX + limitX;
        float beaconYcorMin = imuY - limitY;
        float beaconYcorMax = imuY + limitY;
        SQLiteDatabase db = this.getReadableDatabase();
        final SQLiteStatement stmt = db.compileStatement("SELECT MAX(X) FROM BeaconData WHERE Y BETWEEN '" + beaconXcorMin + "' AND '" + beaconXcorMax + "' ");


        //float maxV = max.getFloat(2);

        // Log.e("Max Data", " Data is :----------"+maxV);

        return stmt.simpleQueryForLong();
    }

}
