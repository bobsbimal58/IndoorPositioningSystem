package com.hoon.wmcs.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

import com.hoon.wmcs.positioning.Mode1;
import com.hoon.wmcs.positioning.Mode2;
import com.hoon.wmcs.positioning.Mode3;

import static com.hoon.wmcs.external.Constants.ALPHA;
import static com.hoon.wmcs.external.Constants.K;
import static com.hoon.wmcs.external.Constants.MAX_THRES;
import static com.hoon.wmcs.external.Constants.MIN_THRES;
import static com.hoon.wmcs.external.Constants.MODE1;
import static com.hoon.wmcs.external.Constants.MODE2;
import static com.hoon.wmcs.external.Constants.MODE3;

/**
 * Created by WMCS on 2017-08-21.
 */

public class AccelerometerListener implements SensorEventListener{
    private static final String TAG = AccelerometerListener.class.getSimpleName();

    private Mode1 mode1;
    private Mode2 mode2;
    private Mode3 mode3;
      BLEScan ble;

    private float [] values;
    private float [] filteredValues;
    private float norm;
    private float maxNorm;
    private float minNorm;
    private float stepLength;

    private boolean stepFlag;
    private boolean minFlag;

    private int mode;

    public AccelerometerListener(Mode1 mode1, Mode2 mode2, Mode3 mode3){
        this.mode1 = mode1;
        this.mode2 = mode2;
        this.mode3 = mode3;


        values = new float [3];
        filteredValues = new float[3];
        norm = 0.0f;
        maxNorm = 0.0f;
        minNorm = 0.0f;

        stepFlag = false;
        minFlag = false;
    }

    public AccelerometerListener(Mode1 mode1){
        this.mode1 = mode1;

        values = new float [3];
        filteredValues = new float[3];
        norm = 0.0f;
        maxNorm = 0.0f;
        minNorm = 0.0f;

        stepFlag = false;
        minFlag = false;

        mode = MODE1;
    }

    public AccelerometerListener(Mode2 mode2){
        this.mode2 = mode2;

        values = new float [3];
        filteredValues = new float[3];
        norm = 0.0f;
        maxNorm = 0.0f;
        minNorm = 0.0f;

        stepFlag = false;
        minFlag = false;

        mode = MODE2;
    }

    public AccelerometerListener(Mode3 mode3){
        this.mode3 = mode3;

        values = new float [3];
        filteredValues = new float[3];
        norm = 0.0f;
        maxNorm = 0.0f;
        minNorm = 0.0f;

        stepFlag = false;
        minFlag = false;

        mode = MODE3;
    }



    public void onSensorChanged(SensorEvent event){

        values = event.values.clone();

        if(mode == MODE1){
            mode1.setRawAccIMU(filteredValues);
        }else if(mode == MODE2){
            mode2.setRawAccIMU(filteredValues);
        }else if(mode == MODE3){
            mode3.setRawAccIMU(filteredValues);
        }


        filteredValues = lowPassFilter(values);

        norm = nomalrization(filteredValues);

        stepDetection();
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy){
        Log.d(TAG, "Accuracy Chan ged : " + accuracy);
    }

    private float[] lowPassFilter(float[] values) {
        float[] value = new float[3];

        value[0] = ALPHA * filteredValues[0] + (1 - ALPHA) * values[0];
        value[1] = ALPHA * filteredValues[1] + (1 - ALPHA) * values[1];
        value[2] = ALPHA * filteredValues[2] + (1 - ALPHA) * values[2];

        return value;


    }

    private float nomalrization(float [] acc){
        float sumOfSquares = (acc[0] * acc[0]) + (acc[1] * acc[1])
                + (acc[2] * acc[2]);
        float acceleration = (float)Math.sqrt(sumOfSquares);

        return acceleration;
    }

    private boolean stepDetection(){

        if (norm > MAX_THRES && stepFlag == false){
            stepFlag = true;
            maxNorm = norm;
        } else if(norm > maxNorm && stepFlag == true){
            if(maxNorm > norm)
                maxNorm = norm;
        }

        if(norm < MIN_THRES && stepFlag == true){
            minFlag = true;
            if(minNorm > norm)
                minNorm = norm;
        }

        if(norm < maxNorm && norm > minNorm) {
            if (stepFlag == true && minFlag == true) {

                // Set the step to current Mode class
                switch (mode) {
                    case MODE1:
                        mode1.setIsStep(true);
                        break;
                    case MODE2:
                        mode2.setIsStep(true);
                        break;
                    case MODE3:
                        mode3.setIsStep(true);
                        break;
                }

                setStepLength();

                // Set the stepLength to current Mode class
                switch (mode) {
                    case MODE1:
                        mode1.setStepLength(stepLength);
                        break;
                    case MODE2:
                        mode2.setStepLength(stepLength);
                        break;
                    case MODE3:
                        mode3.setStepLength(stepLength);
                        break;
                }

                stepFlag = false;
                minFlag = false;

                maxNorm = 0.0f;
                minNorm = 0.0f;
            }

        }

        return false;
    }

    private void setStepLength() {
        stepLength = (float) (K * Math.sqrt(Math.sqrt(maxNorm - minNorm)));
    }
}

