package com.hoon.wmcs.basenavigation;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.hoon.wmcs.arnavigation.ARSurface;
import com.hoon.wmcs.arnavigation.ARView;
import com.hoon.wmcs.positioning.InitLib;

import static com.hoon.wmcs.external.Constants.AR;
import static com.hoon.wmcs.external.Constants.MODE1;
import static com.hoon.wmcs.external.Constants.MODE2;
import static com.hoon.wmcs.external.Constants.MODE3;

public class MainActivity extends AppCompatActivity{
    private static final String TAG = MainActivity.class.getSimpleName();

    private InitLib initLib;
    private ARView arView;
    private Window win;
    private ARSurface arSurface;

    private int ARor3D = AR;
    private int Orient = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        win = getWindow();
        win.setContentView(R.layout.activity_main);

        arView = new ARView(this);
        win.addContentView(arView, new ActionBar.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

        LayoutInflater inflater = (LayoutInflater)getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        arSurface = (ARSurface)findViewById(R.id.arPreView);

        // positioning LIb. (MainActivity, View Class, SurfaceView, int Positioning Mode, int View Mode)
        // Positioning Mode and View Mode is integer in the Constants class
        initLib = new InitLib(this, arView, arSurface, MODE3, ARor3D, Orient);
       // float heading = initLib.getCurrentHeading();
        //float loc_x = initLib.getFinalLocationX();
        //float loc_y = initLib.getFinalLocationY();

        // if View is AR, Camera and display are open
        if(ARor3D == AR) {
            initLib.checkCAMERAPermission();
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig){
        // to remain the display orientation
        super.onConfigurationChanged(newConfig);
        Log.e(TAG, "Lotate");
        switch(newConfig.orientation)
        {
            case Configuration.ORIENTATION_LANDSCAPE:
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        initLib.pauseSensor();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initLib.resumeSensor();
    }

    protected  void onStop(){
        super.onStop();
    }
}
