package com.hoon.wmcs.arnavigation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.hoon.wmcs.communication.PositioningData;

import java.util.ArrayList;

/**
 * Created by WMCS on 2017-08-21.
 */

public class ARView extends View{
    private int width, height;

    private int mode;
    private double mod1_pos_x, mod1_pos_y;
    private double mod2_pos_x, mod2_pos_y;
    private double mod3_pos_x, mod3_pos_y;

    private float pdr_pos_x, pdr_pos_y;
    private float beacon_pos_x, beacon_pos_y;
    private float mag_pos_x, mag_pos_y;
    private float finalPosX, finalPosY;

    private int stepCnt;
    private float stepLength;
    private float heading1, heading2, heading3, heading;
    private float init;
    private float xCenter, yCenter;
    private boolean isCenter;
    private boolean isPoint;
    private float qrDistance;
    private int beaSize, magSize;

    private ArrayList<Float> list;

    private float x, y;

    public ARView(Context context){
        super(context);

        mHandler.sendEmptyMessageDelayed(0, 10);

        stepCnt = 0;
        stepLength = 0.0f;
        heading1 = 0.0f;
        heading2 = 0.0f;
        heading3 = 0.0f;
        heading = 0.0f;
        init = 0;

        xCenter = 0.0f;
        yCenter = 0.0f;
        isCenter = false;
        isPoint = false;

        pdr_pos_x = 0.0f;
        pdr_pos_y = 0.0f;
        beacon_pos_x = 0.0f;
        beacon_pos_y = 0.0f;
        mag_pos_x = 0.0f;
        mag_pos_y = 0.0f;
        finalPosX = 0.0f;
        finalPosY = 0.0f;

        qrDistance = 0.0f;
    }

    public void onDraw(Canvas canvas){
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(20);

        canvas.drawText("Step Count : " + stepCnt + ", " + "Step Length : " + stepLength , 10, 30, paint);
        canvas.drawText("Data Size : " + PositioningData.bea + ", " + PositioningData.mag, 10, 70, paint);
        //canvas.drawText("Heading : " + Math.toDegrees(heading1) + ", " + Math.toDegrees(heading2) + ", " + Math.toDegrees(heading3), 10, 110, paint);

        paint.setTextSize(30);

        canvas.drawText("PDR Pos : " + pdr_pos_x + ", " + pdr_pos_y, 10, 170, paint);
        canvas.drawText("Beacon Pos : " + beacon_pos_x + ", " + beacon_pos_y, 10, 210, paint);
        canvas.drawText("Mag Pos : " + mag_pos_x + ", " + mag_pos_y, 10, 250, paint);
        canvas.drawText("Final Pos : " + finalPosX + ", " + finalPosY, 10, 290, paint);
        canvas.drawText("Final Pos[m] : " + (finalPosX * 0.45f) + ", " + (finalPosY * 0.45f), 10, 330, paint);
        canvas.drawText("QR Distance : " + qrDistance, 10, 370, paint);

        canvas.drawText("Heading : " + heading, 10, 400, paint);
        canvas.drawText("Init Heading : " + init, 10, 440, paint);


        if(isCenter == true){
            paint.setColor(Color.GREEN);
            paint.setStrokeWidth(10);

            canvas.drawPoint(xCenter, yCenter, paint);
        }

        if(isPoint == true){

            paint.setStrokeWidth(2);
            for (int i = 0; i < list.size(); i += 2) { // 좌표 번호 표시
                canvas.drawText(Integer.toString(i/2+1),
                        list.get(i)+20,
                        list.get(i+1)-20, paint);
            }

            paint.setStrokeWidth(10);
            for (int i = 0; i < list.size(); i += 2) { // 점찍기
                canvas.drawPoint(list.get(i),
                        list.get(i+1), paint);
            }

            //무게중심 점 찍기
            canvas.drawPoint(x,
                    y, paint);

            paint.setColor(Color.BLUE);

            paint.setStrokeWidth(2);
            for(int i=0; i<list.size(); i+=2){  // 선 긋기
                if(i>=6) {
                    canvas.drawLine(list.get(i),
                            list.get(i + 1),
                            list.get(0),
                            list.get(1), paint);
                }
                else {
                    canvas.drawLine(list.get(i),
                            list.get(i + 1),
                            list.get(i + 2),
                            list.get(i + 3), paint);
                }
            }

            //무게중심 선 긋기
            canvas.drawLine(x,0,
                    x,2*yCenter,paint);

        }

    }

    Handler mHandler = new Handler(){
        public void handleMessage(Message msg){
            invalidate();
            mHandler.sendEmptyMessageDelayed(0, 10);
        }
    };

    public void setMode(int mode){
        this.mode = mode;
    }

    public void setMod1Pos(double x, double y){
        this.mod1_pos_x = x;
        this.mod1_pos_y = y;
    }

    public void setInitHeading(float heading){
        init = heading;
    }

    public void setMod2Pos(double x, double y){
        this.mod2_pos_x = x;
        this.mod2_pos_y = y;
    }

    public void setMod3Pos(double x, double y){
        this.mod3_pos_x = x;
        this.mod3_pos_y = y;
    }

    public void setStepCnt(int stepCnt){
        this.stepCnt = stepCnt;
    }

    public void setStepLength(float stepLength){
        this.stepLength = stepLength;
    }

    public void setHeading(float heading){
        this.heading = heading;
    }

    public void setHeading(float [] heading){
        this.heading1 = heading[0];
        this.heading2 = heading[1];
        this.heading3 = heading[2];
     }

    public void setCenter(float _x, float _y){
        xCenter = _x;
        yCenter = _y;

        isCenter = true;
    }

    public void setPoint(ArrayList<Float> list, float x, float y){
        this.list = list;
        this.x = x;
        this.y = y;
        isPoint = true;

        invalidate();
    }

    public void setPDRPos(float x, float y){
        this.pdr_pos_x = x;
        this.pdr_pos_y = y;
    }

    public void setBeaconPos(float x, float y){
        this.beacon_pos_x = x;
        this.beacon_pos_y = y;
    }

    public void setMagPos(float x, float y){
        this.mag_pos_x = x;
        this.mag_pos_y = y;
    }

    public void setQrDistance(float distance){
        this.qrDistance = distance;
    }

    public void setFinalPos(float x, float y){
        this.finalPosX = x;
        this.finalPosY = y;
    }

}
