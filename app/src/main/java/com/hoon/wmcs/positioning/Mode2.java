package com.hoon.wmcs.positioning;

import android.util.Log;

import com.hoon.wmcs.arnavigation.ARView;
import com.hoon.wmcs.arnavigation.Navigation;
import com.hoon.wmcs.external.DBHelper;

import static com.hoon.wmcs.external.Constants.MODE2;

/**
 * Created by WMCS on 2017-08-21.
 */

public class Mode2 {
    private static final String TAG = Mode2.class.getSimpleName();

    private ARView arView;
    private Navigation navi;
    private DBHelper dbHelper;

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

    private float magPos_X, magPos_Y;
    private float PDRPos_X, PDRPos_Y;
    private float beaconLocationX;
    private float beaconLocationY;
    private float finalLocationX;
    private float finalLocationY;
    private boolean isPos = false;


    public Mode2(ARView arView, Navigation navigation, DBHelper dbHelper){
        this.arView = arView;
        this.navi = navigation;
        this.dbHelper = dbHelper;

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
    }

    public void setLocation(double x, double y){
        this.pos_x = x;
        this.pos_y = y;
    }

    public void setMod1Pos(){
        arView.setMod1Pos(pos_x, pos_y);
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
        this.isStep = isStep;
        this.stepCount++;

        if(stepCount % 5 == 0){
            setInitPDRPos(magPos_X, magPos_Y);
            Log.e(TAG, "Init Position with mag");
        }

        arView.setStepCnt(this.stepCount);
    }

    public void setStepLength(float stepLength){
        this.stepLength = stepLength;

        arView.setStepLength(this.stepLength);

        EstimatePDRPosition();

        arView.setPDRPos(PDRPos_X, PDRPos_Y);

        isStep = false;
    }

    public float getCurHeading(){
        return this.heading;
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

    public void setInitHeading(float heading){
        arView.setInitHeading(heading);
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

    public void setMagPosition(int x, int y){
        magPos_X = x;
        magPos_Y = y;

        arView.setMagPos(magPos_X, magPos_Y);
    }

    private void EstimatePDRPosition(){
        PDRPos_X = PDRPos_X + stepLength * (float)-Math.cos(Math.toRadians(heading));
        PDRPos_Y = PDRPos_Y + stepLength * (float)-Math.sin(Math.toRadians(heading));

        float minX = dbHelper.getMinX(PDRPos_X, PDRPos_Y);
        float maxX = dbHelper.getMaxX(PDRPos_X, PDRPos_Y);
        float minY = dbHelper.getMinY(PDRPos_X, PDRPos_Y);
        float maxY = dbHelper.getMaxY(PDRPos_X, PDRPos_Y);

        if(PDRPos_X < minX)
            PDRPos_Y = minX;
        else if(PDRPos_X > maxX)
            PDRPos_Y = maxX;

        if(PDRPos_Y < minX)
            PDRPos_Y = minY;
        else if(PDRPos_Y > maxY)
            PDRPos_Y = maxY;

        setFinalLocation(PDRPos_X, PDRPos_Y);

    }

    public void setInitPDRPos(float x, float y){
        PDRPos_X = x;
        PDRPos_Y = y;
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

        arView.setFinalPos(finalLocationX, finalLocationY);
    }

    public void setMagLocation(float x, float y){
        magPos_X = x;
        magPos_X = y;

        arView.setMagPos(x, y);
    }

    public float getIMULocationX(){
        return PDRPos_X;
    }

    public float getIMULocationY(){
        return PDRPos_Y;
    }

    public void setIsPos(boolean isPos){
        this.isPos = isPos;
    }

    public boolean getIsPos(){
        return isPos;
    }

    public float getFinalLocationX(){
        return finalLocationX;
    }

    public float getFinalLocationY(){
        return finalLocationY;
    }
}
