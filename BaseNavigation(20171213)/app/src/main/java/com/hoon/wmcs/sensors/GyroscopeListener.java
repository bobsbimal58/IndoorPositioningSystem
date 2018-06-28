package com.hoon.wmcs.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

import com.hoon.wmcs.positioning.Mode1;
import com.hoon.wmcs.positioning.Mode2;
import com.hoon.wmcs.positioning.Mode3;

import static com.hoon.wmcs.external.Constants.MODE1;
import static com.hoon.wmcs.external.Constants.MODE2;
import static com.hoon.wmcs.external.Constants.MODE3;
import static com.hoon.wmcs.external.Constants.NS2S;

/**
 * Created by WMCS on 2017-08-21.
 */

public class GyroscopeListener implements SensorEventListener{
    private static final String TAG = GyroscopeListener.class.getSimpleName();

    private Mode1 mode1;
    private Mode2 mode2;
    private Mode3 mode3;

    private float [] values;

    private long timeStamp;

    private float [] gyroRad;
    private float [] gyroDeg;

    private float initMagHeading;
    private float curHeading;

    private int mode;
    private int orient;

    public GyroscopeListener(Mode1 mode1, int orient){
        this.mode1 = mode1;
        this.orient = orient;

        values = new float[3];

        timeStamp = 0;

        gyroRad = new float[3];
        gyroDeg = new float[3];

        mode = MODE1;
    }

    public GyroscopeListener(Mode2 mode2){
        this.mode2 = mode2;

        values = new float[3];

        timeStamp = 0;

        gyroRad = new float[3];
        gyroDeg = new float[3];

        mode = MODE2;
    }

    public GyroscopeListener(Mode3 mode3){
        this.mode3 = mode3;

        values = new float[3];

        timeStamp = 0;

        gyroRad = new float[3];
        gyroDeg = new float[3];

        mode = MODE3;
    }


    public void onSensorChanged(SensorEvent event){
        values = event.values.clone();

        // Set the Gyro Information to current Mode class
        switch(mode){
            case MODE1:
                mode1.setRawGyroIMU(values);
                if(mode1.getIsInit()){
                    initMagHeading = mode1.getInitAzimuth();
                    calcRollPitchYaw(values, event.timestamp);
                    calcHeading();

                    //mode1.setCurHeading(curHeading);
                }

                break;
            case MODE2:
                mode2.setRawGyroIMU(values);
                if(mode2.getIsInit()){
                    initMagHeading = mode2.getInitAzimuth();
                    calcRollPitchYaw(values, event.timestamp);
                    calcHeading();

                    //mode2.setCurHeading(curHeading);
                }
                break;
            case MODE3:
                mode3.setRawGyroIMU(values);
                if(mode3.getIsInit()){
                    initMagHeading = mode3.getInitAzimuth();
                    calcRollPitchYaw(values, event.timestamp);
                    calcHeading();

                    // mode3.setCurHeading(curHeading);
                }
                break;
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy){

    }

    private void calcRollPitchYaw(float[] values, long eventTime){
        if(timeStamp != 0){
            float dT = (eventTime - timeStamp) * NS2S;

            gyroRad[0] += values[0] * dT;
            gyroRad[1] += values[1] * dT;
            gyroRad[2] += values[2] * dT;

            gyroDeg[0] = (float)Math.toDegrees(gyroRad[0]);
            gyroDeg[1] = (float)Math.toDegrees(gyroRad[1]);
            gyroDeg[2] = (float)Math.toDegrees(gyroRad[2]);
        }

        timeStamp = eventTime;
    }

    private void calcHeading(){
        if(orient == 0){
            curHeading = initMagHeading - gyroDeg[0];
            if(mode == MODE1){
                mode1.setCurHeading(curHeading);
                mode1.setInitHeading(initMagHeading);
            }else if(mode == MODE2){
                mode2.setCurHeading(curHeading);
                mode2.setInitHeading(initMagHeading);
            }else if(mode == MODE3){
                mode3.setCurHeading(curHeading);
                mode3.setInitHeading(initMagHeading);
            }

        }
        else if(orient == 1)
            curHeading = initMagHeading - gyroDeg[3];
    }
}
