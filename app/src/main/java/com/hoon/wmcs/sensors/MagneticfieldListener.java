package com.hoon.wmcs.sensors;

import android.database.Cursor;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

import com.hoon.wmcs.external.DBHelper;
import com.hoon.wmcs.positioning.Mode1;
import com.hoon.wmcs.positioning.Mode2;
import com.hoon.wmcs.positioning.Mode3;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static com.hoon.wmcs.external.Constants.MODE1;
import static com.hoon.wmcs.external.Constants.MODE2;
import static com.hoon.wmcs.external.Constants.MODE3;

/**
 * Created by WMCS on 2017-08-21.
 */

public class MagneticfieldListener implements SensorEventListener {
    private static final String TAG = MagneticfieldListener.class.getSimpleName();

    private Mode1 mode1;
    private Mode2 mode2;
    private Mode3 mode3;
    private DBHelper dbHelper;

    private float [] values;

    private float[] rotationMatrix;
    private float[] orientationValues;

    private float initRadAzimuth;
    private float initDegAzimuth;

    private float tmpHeading;

    private boolean isInit;
    private boolean isInitial;
    private HashMap<PointF, Float> diff;

    private int x, y;

    private int mode;

    public MagneticfieldListener(Mode1 mode1, DBHelper dbHelper){
        this.mode1 = mode1;

        this.dbHelper = dbHelper;

        diff = new HashMap<>();

        values = new float[3];

        rotationMatrix = new float[16];
        orientationValues = new float[3];

        initDegAzimuth = 0.0f;
        initRadAzimuth = 0.0f;

        isInit = false;
        isInitial = false;

        mode = MODE1;
    }

    public MagneticfieldListener(Mode2 mode2, DBHelper dbHelper){
        this.mode2 = mode2;

        this.dbHelper = dbHelper;

        diff = new HashMap<>();

        values = new float[3];

        rotationMatrix = new float[16];
        orientationValues = new float[3];

        initDegAzimuth = 0.0f;
        initRadAzimuth = 0.0f;

        isInit = false;

        mode = MODE2;
    }

    public MagneticfieldListener(Mode3 mode3, DBHelper dbHelper){
        this.mode3 = mode3;

        this.dbHelper = dbHelper;

        diff = new HashMap<>();

        values = new float[3];

        rotationMatrix = new float[16];
        orientationValues = new float[3];

        initDegAzimuth = 0.0f;
        initRadAzimuth = 0.0f;

        isInit = false;

        mode = MODE3;
    }

    public MagneticfieldListener(Mode1 mode1, Mode2 mode2, Mode3 mode3, DBHelper dbHelper){
        this.mode1 = mode1;
        this.mode2 = mode2;
        this.mode3 = mode3;

        this.dbHelper = dbHelper;

        diff = new HashMap<>();

        values = new float[3];

        rotationMatrix = new float[16];
        orientationValues = new float[3];

        initDegAzimuth = 0.0f;
        initRadAzimuth = 0.0f;

        isInit = false;
    }

    public void onSensorChanged(SensorEvent event){
        values = event.values.clone();
        // Set the Mag raw datas to current Mode class
        switch(mode){
            case MODE1:
                mode1.setMagIMU(values);
                break;
            case MODE2:
                mode2.setMagIMU(values);
                break;
            case MODE3:
                mode3.setMagIMU(values);
                break;
        }

        setOrientation();

        if(mode == MODE2){
            estimotePositioning(values);
            mode2.setMagPosition(x, y);
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy){

    }

    private void setOrientation() {
        float[] acc = new float[3];

        switch(mode){
            case MODE1:
                acc = mode1.getAccIMU();
                break;
            case MODE2:
                acc = mode2.getAccIMU();
                break;
            case MODE3:
                acc = mode3.getAccIMU();
                break;
        }

        if(acc != null) {
            SensorManager.getRotationMatrix(rotationMatrix, null, acc, values);
            SensorManager.getOrientation(rotationMatrix, orientationValues);
            // test value
            tmpHeading = (float) Math.toDegrees(orientationValues[0]);

            tmpHeading = tmpHeading + 90;

            if (tmpHeading > 180)
                tmpHeading -= 360;
            if (tmpHeading != 90) {
                //Log.d(TAG, "TmpHeading : " + tmpHeading);
                //Log.d(TAG, "Azimuth : " + orientationValues[0] + ", " + orientationValues[1] + "," + orientationValues[2]);
                //mode1.setHeading(orientationValues);

                // Set the Mag heading to current Mode class
                switch (mode) {
                    case MODE1:
                        mode1.setTempAzimuth(tmpHeading);
                        break;
                    case MODE2:
                        mode2.setTempAzimuth(tmpHeading);
                        break;
                    case MODE3:
                        mode3.setTempAzimuth(tmpHeading);
                        break;
                }

                // Set the init heading to current Mode class
                if (isInit == false) {
                    //setInitAzimuth();

                    switch (mode) {
                        case MODE1:
                            mode1.setInitAzimuth((float) Math.toRadians(tmpHeading), tmpHeading);
                            break;
                        case MODE2:
                            mode2.setInitAzimuth((float) Math.toRadians(tmpHeading), tmpHeading);
                            break;
                        case MODE3:
                            mode3.setInitAzimuth((float) Math.toRadians(tmpHeading), tmpHeading);
                            break;
                    }

                    isInit = true;
                }
            }
        }
    }

    public void setIsInit(boolean isInit){
        this.isInit = isInit;
    }

    public void estimotePositioning(float [] values){
        float valTotal;

        float dbVal1;
        float dbVal2;
        float dbVal3;
        float Fin;
        float total;

        if(mode2.getIsPos()){
            Cursor cur = dbHelper.getResult();
            cur.moveToFirst();

            valTotal = (float)Math.sqrt(Math.pow(values[0], 2) + Math.pow(values[1], 2) + Math.pow(values[2], 2));

            while (cur.moveToNext()){
                PointF location = new PointF(cur.getInt(1), cur.getInt(2));
                //Log.e(TAG, "111111111111111111111111111111111111");

                dbVal1 = (float)cur.getDouble(5);
                dbVal2 = (float)cur.getDouble(6);
                dbVal3 = (float)cur.getDouble(7);
                Fin = (float)cur.getDouble(8);

                total = (float)Math.sqrt((Math.pow(values[0] - dbVal1, 2) + Math.pow(values[1] - dbVal2, 2) + Math.pow(values[2] - dbVal3, 2) + Math.pow(valTotal - Fin, 2)));

                diff.put(location, total);

                Map.Entry<PointF, Float> min = Collections.min(diff.entrySet(), new Comparator<Map.Entry<PointF, Float>>() {
                    @Override
                    public int compare(Map.Entry<PointF, Float> entry1, Map.Entry<PointF, Float> entry2) {
                        return entry1.getValue().compareTo(entry2.getValue());
                    }
                });

                PointF get = min.getKey();

                x = (int)get.x;
                y = (int)get.y;

                if(!isInitial){
                    mode2.setInitPDRPos(x, y);
                    Log.e(TAG, "init loc =>" + x + "," + y);
                    isInitial = true;
                }
                Log.e(TAG, "mag loc =>" + x + "," + y);
                mode2.setMagPosition(x, y);
            }
        }
    }
}
