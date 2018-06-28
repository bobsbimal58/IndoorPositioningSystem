package com.hoon.wmcs.positioning;

import com.hoon.wmcs.arnavigation.ARView;
import com.hoon.wmcs.arnavigation.Navigation;

/**
 * Created by WMCS on 2017-08-21.
 */

public class Mode3 {
    private static final String TAG = Mode3.class.getSimpleName();

    private ARView arView;
    private Navigation navi;

    private double pos_x, pos_y;

    private float [] rawAccIMU;
    private float [] rawGyroIMU;
    private float [] rawMagIMU;

    private boolean isStep;

    private int stepCount;
    private float stepLength;

    private float initRadAzimuth;
    private float initDegAzimuth;
    private boolean isInit;

    private float tempAzimuth;
    private float heading;

    private float PDRPos_X, PDRPos_Y;
    private float beaconLocationX;
    private float beaconLocationY;
    private float finalLocationX;
    private float finalLocationY;

    private boolean isFinal;

    public Mode3(ARView arView, Navigation navigation){
        this.arView = arView;
        this.navi = navigation;

        rawAccIMU = new float [3];
        rawGyroIMU = new float [3];
        rawMagIMU = new float [3];

        isStep = false;

        stepCount = 0;
        stepLength = 0.0f;

        initRadAzimuth = 0.0f;
        initDegAzimuth = 0.0f;

        PDRPos_X = 0.0f;
        PDRPos_Y = 0.0f;

        isInit = false;
        isFinal = false;
    }

    public void setRawAccIMU(float [] raw){
        rawAccIMU = raw;
        //Log.e("Acc", rawAccIMU[0] + ", " + rawAccIMU[1] + ", " + rawAccIMU[2]);
    }

    public void setRawGyroIMU(float [] raw){
        rawGyroIMU = raw;
        //Log.e("Gyro", rawGyroIMU[0] + ", " + rawGyroIMU[1] + ", " + rawGyroIMU[2]);
    }

    public void setMagIMU(float [] raw){
        rawMagIMU = raw;
        //Log.e("Mag", rawMagIMU[0] + ", " + rawMagIMU[1] + ", " + rawMagIMU[2]);
    }

    public boolean getIsStep(){
        return isStep;
    }

    public void setIsStep(boolean isStep){
        //
        this.isStep = isStep;
        if(this.isStep == true){
            this.stepCount++;

            if(stepCount % 10 == 0){
                setInitPDRPos(beaconLocationX, beaconLocationY);
                //Log.e(TAG, "STEP is 10 times");
            }

            arView.setStepCnt(this.stepCount);
            isFinal = true;
        }
    }

    public void setStepLength(float stepLength){
        this.stepLength = stepLength / 0.45f;

        arView.setStepLength(this.stepLength);

        EstimatePDRPosition();

        arView.setPDRPos(PDRPos_X, PDRPos_Y);

        isStep = false;
    }

    public void setIsFinal(boolean isFinal){
        this.isFinal = isFinal;
    }

    public boolean getIsFinal(){
        return this.isFinal;
    }

    public float [] getAccIMU(){
        return rawAccIMU;
    }

    public void setInitAzimuth(float rad, float deg){
        initRadAzimuth = rad;
        initDegAzimuth = deg;

        isInit = true;

        arView.setInitHeading(deg);
    }

    public float getInitAzimuth(){
        return initDegAzimuth;
    }

    public void setTempAzimuth(float tmp){
        tempAzimuth = tmp;
    }

    public void setIsInit(boolean isInit){
        this.isInit = isInit;
    }

    public boolean getIsInit(){
        return isInit;
    }

    public void setCurHeading(float heading){
        this.heading = heading;

        arView.setHeading(this.heading);
    }

    private void EstimatePDRPosition(){
        PDRPos_X = PDRPos_X + stepLength * (float)-Math.cos(Math.toRadians(-heading));
        PDRPos_Y = PDRPos_Y + stepLength * (float)-Math.sin(Math.toRadians(-heading));
    }

    public void setInitPDRPos(float x, float y){
        PDRPos_X = x;
        PDRPos_Y = y;

        arView.setPDRPos(x, y);
    }

    public float getCurHeading(){
        return this.heading;
    }

    public void setInitHeading(float heading){
        arView.setInitHeading(heading);
    }

    public void setHeading(float[] heading){
        arView.setHeading(heading);
    }

    public int getStepCount(){
        return stepCount;
    }

    public void setBeaconLocation(float x, float y){
        beaconLocationX = x;
        beaconLocationY = y;

        arView.setBeaconPos(beaconLocationX, beaconLocationY);
    }

    public void setFinalLocation(float x, float y){
        finalLocationX = x;
        finalLocationY = y;

        arView.setFinalPos(x, y);
    }

    public float getIMULocationX(){
        return PDRPos_X;
    }

    public float getIMULocationY(){
        return PDRPos_Y;
    }

    public float getFinalLocationX(){
        return finalLocationX;
    }

    public float getFinalLocationY(){
        return finalLocationY;
    }
}
