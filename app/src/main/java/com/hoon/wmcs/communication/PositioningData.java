package com.hoon.wmcs.communication;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.hoon.wmcs.basenavigation.MainActivity;
import com.hoon.wmcs.basenavigation.UpdateActivity;
import com.hoon.wmcs.external.DBHelper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import static com.hoon.wmcs.external.Constants.END;
import static com.hoon.wmcs.external.Constants.IP;
import static com.hoon.wmcs.external.Constants.PORT;

/**
 * Created by WMCS on 2017-09-26.
 */

public class PositioningData {
    private static final String TAG = PositioningData.class.getSimpleName();

    private UpdateActivity updateActivity;

    private Socket socket;
    private DataInputStream is;
    private DataOutputStream os;
    private Handler hd;
    private SocketClient socketClient;
    private DBHelper dbHelper;

    private final ProgressBar pb;

    public static int bea, mag;

    public PositioningData(final UpdateActivity updateActivity, ProgressBar pb){
        this.updateActivity = updateActivity;

        dbHelper = new DBHelper(updateActivity);

        this.pb = pb;

        hd = new Handler();

        pb.setVisibility(View.VISIBLE);

        bea = 0;
        mag = 0;

        new Thread(){
            public void run(){
                try{
                    socket = new Socket(InetAddress.getByName(IP), PORT);
                    is = new DataInputStream(socket.getInputStream());
                    os = new DataOutputStream(socket.getOutputStream());

                    hd.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(updateActivity.getApplicationContext(), "서버에 연결하였습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });

                    socketClient = new SocketClient(socket, is, os);
                    socketClient.start();
                }catch (IOException e){
                    e.printStackTrace();
                    hd.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(updateActivity.getApplicationContext(), "서버 연결에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                            updateActivity.sceepDown();
                        }
                    });
                }
            }
        }.start();
    }

    class SocketClient extends Thread{
        private DataInputStream is;
        private DataOutputStream os;
        private Socket socket;

        private int cnt;
        private boolean state;
        private String [] require = {"mag\n", "beacon\n"};

        public SocketClient(Socket socket, DataInputStream is, DataOutputStream os){
            this.socket = socket;
            this.is = is;
            this.os = os;

            cnt = 0;
            state = false;

            dbHelper.dropTable();
        }

        public void run(){
            while(cnt < 2){
                if(state == false){
                    try{
                        os.writeUTF(require[cnt]);
                        os.flush();

                        state = true;
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }

                while(true){
                    try{
                        String tmp = is.readUTF();
                        if(!tmp.equals(END)){
                            if(cnt == 0){
                                mag++;
                                dbHelper.insertMag(tmp);
                            }else if(cnt == 1){
                                bea++;
                                dbHelper.insertBeacon(tmp);
                            }
                        } else {
                            cnt++;
                            state = false;
                            break;
                        }
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }

            disCon();
            Log.e(TAG, "Recieve Completed");

            Log.e(TAG, "Mag");
            dbHelper.showMag();

            Log.e(TAG,"Beacon");
            dbHelper.showBeacon();


            hd.post(new Runnable() {
                @Override
                public void run() {
                    updateActivity.sceepDown();
                }
            });
        }
    }

    private void disCon(){
        try{
            os.close();
            is.close();
            socket.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
