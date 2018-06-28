package com.hoon.wmcs.positioning;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import com.hoon.wmcs.arnavigation.ARSurface;
import com.hoon.wmcs.arnavigation.ARView;
import com.hoon.wmcs.arnavigation.IndoorGML;
import com.hoon.wmcs.arnavigation.Navigation;
import com.hoon.wmcs.basenavigation.MainActivity;
import com.hoon.wmcs.external.DBHelper;
import com.hoon.wmcs.sensors.AccelerometerListener;
import com.hoon.wmcs.sensors.BLEScan;
import com.hoon.wmcs.sensors.GyroscopeListener;
import com.hoon.wmcs.sensors.MagneticfieldListener;
import com.hoon.wmcs.sensors.QRCamera;

import static android.Manifest.permission.CAMERA;
import static android.content.Context.SENSOR_SERVICE;
import static com.hoon.wmcs.external.Constants.AR;
import static com.hoon.wmcs.external.Constants.MODE1;
import static com.hoon.wmcs.external.Constants.MODE2;
import static com.hoon.wmcs.external.Constants.MODE3;
import static com.hoon.wmcs.external.Constants.MY_PERMISSION_REQUEST_CODE;
import static com.hoon.wmcs.external.Constants.RATE;


/**
 * Created by WMCS on 2017-08-21.
 */

public class InitLib implements View.OnClickListener{
    private static final String TAG = InitLib.class.getSimpleName();

    private MainActivity mainActivity;
    private QRCamera qrCamera;
    private ARSurface arSurface;
    private ARView arView;
    private Mode1 mode1;
    private Mode2 mode2;
    private Mode3 mode3;
    private Navigation navi;
    private IndoorGML indoorGML;
    private BLEScan bleScan;
    private DBHelper dbHelper;

    private AccelerometerListener accListener;
    private GyroscopeListener gyroListener;
    private MagneticfieldListener magListener;

    private SensorManager sensorManager;
    private Camera mCamera;

    private int orient;
    private int Mode;

    public InitLib(MainActivity mainActivity, int Mode, int ARor3D, int orient){
        this.mainActivity = mainActivity;
        this.arView = null;
        this.arSurface = null;
        this.orient = orient;

        dbHelper = new DBHelper(mainActivity);
        this.Mode = Mode;
    }

    @Override
    public void onClick(View v) {
        if (mCamera != null) {
            if(Mode == MODE3)
                mCamera.autoFocus(qrCamera);
        }
    }

    public InitLib(MainActivity mainActivity, ARView arView, ARSurface arSurface, int Mode, int ARor3D, int orient){
        this.mainActivity = mainActivity;
        this.arView = arView;
        this.arSurface = arSurface;
        this.orient = orient;

        navi = new Navigation();
        indoorGML = new IndoorGML();
        dbHelper = new DBHelper(mainActivity);
        this.Mode = Mode;

        // Position Class Instants through the Positioning Mode
        switch(Mode){
            case MODE1:
                mode1 = new Mode1(arView, navi, dbHelper);
                bleScan = new BLEScan(this.mainActivity, dbHelper, mode1);
                break;
            case MODE2:
                mode2 = new Mode2(arView, navi, dbHelper);
                bleScan = new BLEScan(this.mainActivity, dbHelper, mode2);
                break;
            case MODE3:
                mode3 = new Mode3(arView, navi);
                bleScan = new BLEScan(this.mainActivity, dbHelper, mode3);
                break;
        }

        // If View mode is AR, Camera is Open
        if(ARor3D == AR){
            cameraOpen(Mode);
        }

        // Call the IMU Sensor Listeners
        startSensorListener(Mode);
    }

    public void startSensorListener(int Mode){
        sensorManager = (SensorManager)mainActivity.getSystemService(SENSOR_SERVICE);

        switch(Mode){
            case MODE1:
                accListener = new AccelerometerListener(mode1);
                gyroListener = new GyroscopeListener(mode1, orient);
                magListener = new MagneticfieldListener(mode1, dbHelper);
                break;
            case MODE2:
                accListener = new AccelerometerListener(mode2);
                gyroListener = new GyroscopeListener(mode2);
                magListener = new MagneticfieldListener(mode2, dbHelper);
                break;
            case MODE3:
                accListener = new AccelerometerListener(mode3);
                gyroListener = new GyroscopeListener(mode3);
                magListener = new MagneticfieldListener(mode3, dbHelper);
                break;
        }

        sensorManager
                .registerListener(accListener, sensorManager
                        .getDefaultSensor(Sensor.TYPE_ACCELEROMETER), RATE);
        sensorManager.registerListener(gyroListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), RATE);
        sensorManager.registerListener(magListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                RATE);
    }

    public void pauseSensor(){
        sensorManager.unregisterListener(accListener);
        sensorManager.unregisterListener(gyroListener);
        sensorManager.unregisterListener(magListener);

    }

    public void resumeSensor(){
        sensorManager
                .registerListener(accListener, sensorManager
                        .getDefaultSensor(Sensor.TYPE_ACCELEROMETER), RATE);
        sensorManager.registerListener(gyroListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), RATE);
        sensorManager.registerListener(magListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                RATE);
    }

    public void cameraOpen(int Mode){
        int APIVersion = android.os.Build.VERSION.SDK_INT;
        if (APIVersion >= Build.VERSION_CODES.M){
            if(checkCAMERAPermission()) {
                mCamera = Camera.open();


                // If Positioning Mode is QR Code Position Mode, make the QRCode class instaant
                if(Mode == MODE3) {
                    Log.e(TAG, "HELLO1");
                    qrCamera = new QRCamera(this.mainActivity, this.arSurface, this.arView, this.mCamera);
                }
            }else{
                ActivityCompat.requestPermissions(mainActivity, new String[]{CAMERA}, MY_PERMISSION_REQUEST_CODE);
            }

            arSurface.setCameraInstant(mCamera);
            /*
            if(mCamera != null){
                mCamera = Camera.open();
                arSurface.setCameraInstant(mCamera);

                // If Positioning Mode is QR Code Position Mode, make the QRCode class instaant
                if(Mode == MODE3){
                    Log.e(TAG, "HELLO2");
                    //qrCamera = new QRCamera(this.mainActivity, this.arSurface, this.arView, this.mCamera);
                }
            }
            */
        }
    }

    // Call the camera permission dialog
    public boolean checkCAMERAPermission(){
        int result = ContextCompat.checkSelfPermission(mainActivity,
                CAMERA);

        return result == PackageManager.PERMISSION_GRANTED;
    }

    public void changeOrient(int orient){
        this.orient = orient;
    }

    public float getFinalLocationX(){
        if(Mode == MODE1)
            return mode1.getFinalLocationX();
        else if(Mode == MODE2)
            return mode2.getFinalLocationX();
        else if(Mode == MODE3){
            return mode3.getFinalLocationX();
        }

        return 0;
    }

    public float getFinalLocationY(){
        if(Mode == MODE1)
            return mode1.getFinalLocationY();
        else if(Mode == MODE2)
            return mode2.getFinalLocationY();
        else if(Mode == MODE3)
            return mode3.getFinalLocationY();
        return 0;
    }

    public float getFinalLocationXm(){
        if(Mode == MODE1)
            return mode1.getFinalLocationX() * 0.45f;
        else if(Mode == MODE2)
            return mode2.getFinalLocationX() * 0.45f;
        else if(Mode == MODE3)
            return mode3.getFinalLocationX() * 0.45f;

        return 0;
    }

    public float getFinalLocationYm(){
        if(Mode == MODE1)
            return mode1.getFinalLocationY() * 0.45f;
        else if(Mode == MODE2)
            return mode2.getFinalLocationY() * 0.45f;
        else if(Mode == MODE3)
            return mode3.getFinalLocationY() * 0.45f;

        return 0;
    }

    public float getCurrentHeading(){
        if(Mode == MODE1)
            return mode1.getCurHeading();
        else if(Mode == MODE2)
            return mode2.getCurHeading();
        else if(Mode == MODE3)
            return mode3.getCurHeading();

        return 0;
    }
}
