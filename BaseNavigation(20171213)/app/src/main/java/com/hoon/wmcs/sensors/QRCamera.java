package com.hoon.wmcs.sensors;


import android.hardware.Camera;
import android.hardware.camera2.*;
import android.hardware.Camera.PreviewCallback;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.hoon.wmcs.arnavigation.ARSurface;
import com.hoon.wmcs.arnavigation.ARView;
import com.hoon.wmcs.basenavigation.MainActivity;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WMCS on 2017-08-23.
 */

public class QRCamera implements Camera.AutoFocusCallback{
    private static final String TAG = QRCamera.class.getSimpleName();

    private MainActivity mainActivity;
    private Camera mCamera;
    private ARSurface arSurface;
    private ARView arView;
    private Camera.Parameters parameters;
    private List<Camera.Size> previewSizes;

    private int dWidth, dHeight;
    private int xCenter, yCenter;

    public QRCamera(MainActivity mainActivity, ARSurface arSurface, ARView arView, Camera mCamera){
        this.mainActivity = mainActivity;
        this.arSurface = arSurface;
        this.arView = arView;
        this.mCamera = mCamera;

        DisplayMetrics dm = mainActivity.getApplicationContext().getResources().getDisplayMetrics();

        dWidth = dm.widthPixels;
        dHeight = dm.heightPixels;

        xCenter = dWidth / 2;
        yCenter = dHeight / 2;

        parameters = mCamera.getParameters();
        previewSizes = parameters.getSupportedPreviewSizes();

        for(int i=0; i<previewSizes.size(); i++){
            Log.e("해상도", previewSizes.get(i).width + ", " + previewSizes.get(i).height);
        }

        arView.setCenter(xCenter, yCenter);

        this.mCamera.stopPreview();
        this.mCamera.setPreviewCallback(_previewCallback);
        this.mCamera.startPreview();

    }

    public PreviewCallback _previewCallback = new PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            // Read Range
            Log.d("onPreviewFrame", "onPreviewFrame Called");

            Camera.Size size = camera.getParameters().getPreviewSize();

            // Create BinaryBitmap
            PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(
                    data, size.width, size.height, 0, 0, size.width, size.height, false);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            // Read QR Code
            Reader reader = new QRCodeReader();
            Result result = null;

            try {
                result = reader.decode(bitmap);
                String text = result.getText();

                ResultPoint[] resultPoints = result.getResultPoints();

                String pointText = "";

                ArrayList<Float> list = new ArrayList<Float>();

                float[] xArray = new float[resultPoints.length];
                float[] yArray = new float[resultPoints.length];

                for (int i = 0; i < resultPoints.length; i++) {
                    xArray[i] = (int) (resultPoints[i].getX());
                    yArray[i] = (int) (resultPoints[i].getY());

                    pointText += "point" + (i + 1) + "(" + (xArray[i] - xCenter) + ", " + (yCenter - yArray[i]) + ")  ";
                    list.add(xArray[i]);
                    list.add(yArray[i]);


                }

                float[] distanceArray = new float[xArray.length];

                for (int i = 0; i < xArray.length; i++) {
                    if (i + 1 < xArray.length) {
                        distanceArray[i] = (float) (Math.sqrt(Math.pow(xArray[i + 1] - xArray[i], 2)
                                + Math.pow(yArray[i + 1] - yArray[i], 2)) / 515 * 2.54);
                    } else {
                        distanceArray[i] = (float) (Math.sqrt(Math.pow(xArray[0] - xArray[i], 2)
                                + Math.pow(yArray[0] - yArray[i], 2)) / 515 * 2.54);
                    }
                }
                String distanceStr = "";

                if (distanceArray.length == 4) {
                    distanceStr = String.format("좌변 : %.3fcm  윗변 : %.3fcm  우변 : %.3fcm  밑변 : %.3fcm",
                            distanceArray[0], distanceArray[1], distanceArray[2], distanceArray[3]);
                }

                // 무게중심 계산
                float gX, gY;
                gX = ((xArray[1] - xArray[3]) * ((yArray[0] - yArray[2]) * (xArray[1] + xArray[3]) + (yArray[0] * xArray[0]) - (yArray[2] * xArray[2]))
                        - (xArray[0] - xArray[2]) * ((yArray[1] - yArray[3]) * (xArray[0] + xArray[2]) + yArray[1] * xArray[1] - yArray[3] * xArray[3])) /
                        (3 * (yArray[0] - yArray[2]) * (xArray[1] - xArray[3]) - 3 * (yArray[1] - yArray[3]) * (xArray[0] - xArray[2]));
                gY = ((yArray[0] - yArray[2]) * ((xArray[1] - xArray[3]) * (yArray[0] + yArray[2]) + (xArray[1] * yArray[1]) - (xArray[3] * yArray[3]))
                        - (yArray[1] - yArray[3]) * ((xArray[0] - xArray[2]) * (yArray[1] + yArray[3]) + xArray[0] * yArray[0] - xArray[2] * yArray[2])) /
                        (3 * (xArray[1] - xArray[3]) * (yArray[0] - yArray[2]) - 3 * (xArray[0] - xArray[2]) * (yArray[1] - yArray[3]));

                arView.setPoint(list, gX, gY);

                Log.e(TAG, "What happen!!!" + list.toString());

                float diagonalLength = (float) (Math.sqrt(Math.pow(xArray[2] - xArray[0], 2)
                        + Math.pow(yArray[2] - yArray[0], 2)) / 515 * 2.54);
                float area = areaOfTriangle(distanceArray[0], distanceArray[1], diagonalLength) +
                        areaOfTriangle(distanceArray[2], distanceArray[3], diagonalLength);

                double distance;

                distance = (2.87) / distanceArray[0];

                String areaStr = String.format("넓이 : %.3f㎠ \n거리 : %.2fm", area, distance);

                arView.setQrDistance((float)distance);

                Log.e(TAG, "HELLO8");

            } catch (NotFoundException e) {
                e.printStackTrace();
            } catch (ChecksumException e) {
                e.printStackTrace();
            } catch (FormatException e) {
                e.printStackTrace();
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            }


        }
    };

    // 헤론의 공식으로 삼각형 넓이 구하는 메소드
    public static float areaOfTriangle(float a, float b, float c){
        float s = (a+b+c)/2;
        float A = (float)Math.sqrt(s*(s-a)*(s-b)*(s-c));
        return A;
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {

    }
}
