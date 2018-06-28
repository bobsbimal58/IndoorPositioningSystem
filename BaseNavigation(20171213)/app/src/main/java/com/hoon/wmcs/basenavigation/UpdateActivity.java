package com.hoon.wmcs.basenavigation;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;

import com.hoon.wmcs.communication.PositioningData;
import com.hoon.wmcs.external.DBHelper;

public class UpdateActivity extends AppCompatActivity {
    private static final String TAG = UpdateActivity.class.getSimpleName();

    private DBHelper dbHelper;

    private PositioningData positioningData;

    private ProgressBar mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        mProgress = (ProgressBar)findViewById(R.id.prog);

        positioningData = new PositioningData(this, mProgress);
        //sceepDown();
    }

    public void sceepDown(){
        Handler handlerSplash = new Handler(){
            public void handleMessage(Message msg){
                Intent intent = new Intent(getApplication(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        };
        handlerSplash.sendEmptyMessageDelayed(0, 0);
    }
}
