package com.hoon.wmcs.arnavigation;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import java.io.IOException;
import java.util.List;

import static com.hoon.wmcs.external.Constants.MY_PERMISSION_REQUEST_CODE;

/**
 * Created by WMCS on 2017-07-28.
 */

public class ARSurface extends SurfaceView implements SurfaceHolder.Callback{
    private SurfaceHolder mHolder;
    private Camera mCamera;

    int width, height;

    public ARSurface(Context context, AttributeSet attrs){
        super(context, attrs);
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        /*
        try{
            if(mCamera != null){
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        */

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if(mCamera != null){

            try{
                mCamera.stopPreview();
            }catch (Exception e){
                e.printStackTrace();
            }

            Camera.Parameters parameters = mCamera.getParameters();

            List<String> focusModes = parameters.getSupportedFocusModes();
            if(focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)){
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
            mCamera.setParameters(parameters);

            try{
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        if(mCamera != null){
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

    }

    public void setCameraInstant(Camera _camera){

        mCamera = _camera;

        mCamera.setDisplayOrientation(0);

        try{
            mCamera.setPreviewDisplay(mHolder);
        }catch (IOException e){
            e.printStackTrace();
            mCamera.release();
        }
    }
}
