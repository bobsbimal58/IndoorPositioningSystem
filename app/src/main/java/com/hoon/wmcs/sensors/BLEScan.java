package com.hoon.wmcs.sensors;

import android.bluetooth.BluetoothAdapter;
import android.database.Cursor;
import android.graphics.PointF;
import android.util.Log;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;
import com.hoon.wmcs.basenavigation.MainActivity;
import com.hoon.wmcs.external.DBHelper;
import com.hoon.wmcs.positioning.Mode1;
import com.hoon.wmcs.positioning.Mode2;
import com.hoon.wmcs.positioning.Mode3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.hoon.wmcs.external.Constants.MODE1;
import static com.hoon.wmcs.external.Constants.MODE2;
import static com.hoon.wmcs.external.Constants.MODE3;

/**
 * Created by Rohan on 6/29/2017.
 */

public class BLEScan{
    private static final String TAG = BLEScan.class.getSimpleName();

    private MainActivity mainActivity;

    private DBHelper dbHelper;
    private BeaconManager beaconManager;
    private BluetoothAdapter bluetoothAdapter;
    private Region region;

    private int mode;
    private Mode1 mode1;
    private Mode2 mode2;
    private Mode3 mode3;

    private HashMap<PointF, Double> finalData;
    HashMap<Integer, PointF> floorLocation;

    private float beaconLocationX = 0.0f;
    private float beaconLocationY = 0.0f;
    private float imuLocationX = 0.0f;
    private float imuLocationY = 0.0f;
    private float finalLocationX = 0.0f;
    private float finalLocationY = 0.0f;

    private boolean isInitial = true;


    private static HashMap<String, List<Beacon>> BEACON_LIST = new HashMap<>();

    public BLEScan(){

    }

    public BLEScan(MainActivity mainActivity, DBHelper dbHelper, Mode1 mode1){
        this.mainActivity = mainActivity;
        this.dbHelper = dbHelper;
        this.mode1 = mode1;

        beaconManager = new BeaconManager(this.mainActivity);

        if(!SystemRequirementsChecker.checkWithDefaultDialogs(this.mainActivity))
            return;
        beaconManager.connect(new BeaconManager.ServiceReadyCallback(){
            @Override
            public void onServiceReady(){
                beaconManager.startRanging(region);
                displayBeacon();
            }
        });

        startScan();

        mode = MODE1;
    }

    public BLEScan(MainActivity mainActivity, DBHelper dbHelper, Mode2 mode2){
        this.mainActivity = mainActivity;
        this.dbHelper = dbHelper;
        this.mode2 = mode2;

        beaconManager = new BeaconManager(this.mainActivity);

        if(!SystemRequirementsChecker.checkWithDefaultDialogs(this.mainActivity))
            return;
        beaconManager.connect(new BeaconManager.ServiceReadyCallback(){
            @Override
            public void onServiceReady(){
                beaconManager.startRanging(region);
                displayBeacon();
            }
        });

        startScan();


        mode = MODE2;
    }

    public BLEScan(MainActivity mainActivity, DBHelper dbHelper, Mode3 mode3){
        this.mainActivity = mainActivity;
        this.dbHelper = dbHelper;
        this.mode3 = mode3;

        beaconManager = new BeaconManager(this.mainActivity);

        if(!SystemRequirementsChecker.checkWithDefaultDialogs(this.mainActivity))
            return;
        beaconManager.connect(new BeaconManager.ServiceReadyCallback(){
            @Override
            public void onServiceReady(){
                beaconManager.startRanging(region);
                displayBeacon();
            }
        });

        startScan();

        mode = MODE3;
    }

    private void startScan() {

        region = new Region("ranged region", UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), 7008 , null);

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                boolean IsDisplay = false;
                if (!list.isEmpty()) {
                    for (Beacon b : list){

                        IsDisplay = makeBeaconList(b);
                        Log.d(TAG, b.getMajor() + ", " + b.getMinor() + ", " + b.getRssi());
                    }
                    estimateBeaconPos();
                }
            }
        });
    }

    private boolean makeBeaconList(Beacon beacon) {
        String beaconKey = String.format("%d-%d", beacon.getMajor(), beacon.getMinor());
        List<Beacon> aBeacongroup = new ArrayList<>();
        if (BEACON_LIST.containsKey(beaconKey))
            aBeacongroup = BEACON_LIST.get(beaconKey);
        if (aBeacongroup.size()>(1000-1))
            return false;
        aBeacongroup.add(beacon);

        BEACON_LIST.put(beaconKey, aBeacongroup);

        return true;
    }

    private void displayBeacon() {
        int current = 0;
        int value = 0;

        for (Map.Entry<String, List<Beacon>> entry : BEACON_LIST.entrySet()) {
            int avgRssi = 0;
            current++;
            String beaconKey = entry.getKey();
            List<Beacon> aBeaconGroup = entry.getValue();

            for (Beacon b : aBeaconGroup) {
                value = b.getRssi();
                avgRssi += b.getRssi();
            }

            //Log.d(TAG, beaconKey);
        }
    }

    public void stopScan(){
        beaconManager.disconnect();
        BEACON_LIST.clear();
    }


    public void estimateBeaconPos() {
        String dbBeaconKey = "";
        float dbRssi = 0.0f;
        int mapId = 0;
        int preId = 1;
        int floor=0;
        double difference = 0;
        double sqrtDiff = 0;
        PointF location;


        int stepCnt = 0;

        finalData = new HashMap<>();
        floorLocation = new HashMap<>();

        if(mode == MODE1)
            stepCnt = mode1.getStepCount();
        else if(mode == MODE2)
            stepCnt = mode2.getStepCount();
        else if(mode == MODE3)
            stepCnt = mode3.getStepCount();

        if(stepCnt == 0){
            Cursor cursor = dbHelper.getBeaconData();
            cursor.moveToFirst();
            int readCnt = 0;

            if(cursor.isLast() == false){
                do{
                    mapId = cursor.getInt(1);
                    location = new PointF(cursor.getFloat(2), cursor.getFloat(3));
                    dbBeaconKey = cursor.getString(6);
                    dbRssi = cursor.getFloat(7);
                    floor = cursor.getInt(0);



                    for(Map.Entry<String, List<Beacon>> entry : BEACON_LIST.entrySet()){
                        String beaconKey = entry.getKey();

                        if(mapId != preId){
                            Log.e("Map is changed", "********************** Map is changed **********************" + mapId + "," + beaconKey);
                            difference = 0;
                            preId = mapId;
                        }

                        if(!beaconKey.equals(dbBeaconKey)){
                            continue;
                        }

                        List<Beacon> aBeaconGroup = entry.getValue();

                        int rssi = 0;

                        //Log.e("beaconGroup", "BeaconGroup Size : " + aBeaconGroup.size());
                        for (Beacon b : aBeaconGroup) {
                            rssi = b.getRssi();
                            // Log.e("beaconRssi", dbBeaconKey.toString() + ", " + beaconKey + ", " + rssi + ", " + dbRssi);
                            readCnt++;

                            if (readCnt == aBeaconGroup.size()) {
                                //Log.e("pre diff", "pre diff" + difference);
                                difference += (Math.pow((rssi - dbRssi), 2));
                                sqrtDiff = Math.sqrt(difference);
                                finalData.put(location, sqrtDiff);
                                floorLocation.put(floor, location);
                                //Log.e("put Data", "put Data : " + difference);

                                readCnt = 0;
                                break;
                            }
                        }
                        //Log.e("beaconEnd", "------------------------------------------------------------------------");
                    }
                }while(cursor.moveToNext());

                Map.Entry<PointF, Double> min = Collections.min(finalData.entrySet(), new Comparator<Map.Entry<PointF, Double>>() {
                    @Override
                    public int compare(Map.Entry<PointF, Double> o1, Map.Entry<PointF, Double> o2) {
                        return o1.getValue().compareTo(o2.getValue());
                    }
                });

                PointF locationXY = min.getKey();
                beaconLocationX = locationXY.x;
                beaconLocationY = locationXY.y;

                if(mode == MODE1){
                    imuLocationX = beaconLocationX;
                    imuLocationY = beaconLocationY;

                    Log.d(TAG, "IMU LOC = X : " + imuLocationX + ", Y : " + imuLocationY);
                    isInitial = false;

                    mode1.setInitPDRPos(imuLocationX, imuLocationY);
                    mode1.setFinalLocation(imuLocationX, imuLocationY);
                }else if(mode == MODE2){
                    mode2.setBeaconLocation(beaconLocationX, beaconLocationY);
                    dbHelper.setBeaconXY(beaconLocationX, beaconLocationY);
                    mode2.setIsPos(true);
                }else if(mode == MODE3){
                    imuLocationX = beaconLocationX;
                    imuLocationY = beaconLocationY;

                    Log.d(TAG, "IMU LOC = X : " + imuLocationX + ", Y : " + imuLocationY);
                    isInitial = false;

                    mode3.setInitPDRPos(imuLocationX, imuLocationY);
                    mode3.setFinalLocation(imuLocationX, imuLocationY);
                }
            }

            Log.d(TAG, "BEACON LOC = X : " + beaconLocationX + ", Y : " + beaconLocationY);

            switch(mode){
                case MODE1:
                    mode1.setBeaconLocation(beaconLocationX, beaconLocationY);
                    break;
                case MODE2:
                    mode2.setBeaconLocation(beaconLocationX, beaconLocationY);
                    break;
                case MODE3:
                    mode3.setBeaconLocation(beaconLocationX, beaconLocationY);
                    break;
            }

        }else if(stepCnt >= 1){
            //Log.e(TAG, mode1.getIMULocationX() + "," + mode1.getIMULocationY());
            Cursor cursor = null;

            switch(mode){
                case MODE1:
                    cursor = dbHelper.getSelectedData(mode1.getIMULocationX(), mode1.getIMULocationY());
                    break;
                case MODE2:
                    cursor = dbHelper.getSelectedData(mode2.getIMULocationX(), mode2.getIMULocationY());
                    break;
                case MODE3:
                    cursor = dbHelper.getSelectedData(mode3.getIMULocationX(), mode3.getIMULocationY());
                    break;
            }

            cursor.moveToFirst();
            int readCnt = 0;

            if(cursor.isLast() == false){
                while(cursor.moveToNext()){
                    mapId = cursor.getInt(1);
                    location = new PointF(cursor.getFloat(2), cursor.getFloat(3));
                    dbBeaconKey = cursor.getString(6);
                    dbRssi = cursor.getFloat(7);

                    for(Map.Entry<String, List<Beacon>> entry : BEACON_LIST.entrySet()){
                        String beaconKey = entry.getKey();

                        if(mapId != preId){
                            // Log.e("Map is changed", "********************** Map is changed **********************" + mapId);
                            difference = 0;
                            preId = mapId;
                        }

                        if (!beaconKey.equals(dbBeaconKey)) {
                            continue;
                        }List<Beacon> aBeaconGroup = entry.getValue();
                        int rssi = 0;
                        // Log.e("beaconGroup", "BeaconGroup Size : " + aBeaconGroup.size());
                        for (Beacon b : aBeaconGroup) {
                            rssi = b.getRssi();
                            // Log.e("beaconRssi", dbBeaconKey.toString() + ", " + beaconKey + ", " + rssi + ", " + dbRssi);
                            readCnt++;

                            if (readCnt == aBeaconGroup.size()) {
                                // Log.e("pre diff", "pre diff" + difference);
                                difference += (Math.pow((rssi - dbRssi), 2));
                                sqrtDiff = Math.sqrt(difference);
                                finalData.put(location, sqrtDiff);
                                // Log.e("put Data", "put Data : " + difference);

                                readCnt = 0;
                                break;
                            }
                        }
                        //Log.e("beaconEnd", "------------------------------------------------------------------------");
                    }
                }

                if(finalData.size() != 0) {
                    Map.Entry<PointF, Double> min = Collections.min(finalData.entrySet(), new Comparator<Map.Entry<PointF, Double>>() {
                        @Override
                        public int compare(Map.Entry<PointF, Double> o1, Map.Entry<PointF, Double> o2) {
                            return o1.getValue().compareTo(o2.getValue());
                        }
                    });

                    PointF locationXY = min.getKey();
                    beaconLocationX = locationXY.x;
                    beaconLocationY = locationXY.y;

                    Log.d(TAG, "BEACON LOC = X : " + beaconLocationX + ", Y : " + beaconLocationY);

                    switch(mode){
                        case MODE1:
                            mode1.setBeaconLocation(beaconLocationX, beaconLocationY);
                            if(mode1.getIsFinal()){
                                finalLocationX = (beaconLocationX + mode1.getIMULocationX())/2.0f;
                                finalLocationY = (beaconLocationY + mode1.getIMULocationY())/2.0f;

                                mode1.setFinalLocation(finalLocationX, finalLocationY);
                                Log.d(TAG, "Final LOC = X : " + finalLocationX + ", Y : " + finalLocationY);

                                mode1.setIsFinal(false);
                            }
                            break;
                        case MODE2:
                            mode2.setBeaconLocation(beaconLocationX, beaconLocationY);
                            //mode2.setFinalLocation(finalLocationX, finalLocationY);
                            break;
                        case MODE3:
                            mode3.setBeaconLocation(beaconLocationX, beaconLocationY);
                            if(mode3.getIsFinal()){
                                finalLocationX = (beaconLocationX + mode3.getIMULocationX())/2.0f;
                                finalLocationY = (beaconLocationY + mode3.getIMULocationY())/2.0f;

                                mode3.setFinalLocation(finalLocationX, finalLocationY);
                                Log.d(TAG, "Final LOC = X : " + finalLocationX + ", Y : " + finalLocationY);

                                mode3.setIsFinal(false);
                            }

                            break;
                    }
                }
            }
        }
    }

    public void pauseScan(){
        if(beaconManager == null){
            return;
        }
        beaconManager.stopRanging(region);
    }
}
