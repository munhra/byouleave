package com.example.vntraal.byouleave;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Iterator;
import java.util.List;

import static android.media.RingtoneManager.TYPE_NOTIFICATION;
import static android.media.RingtoneManager.getDefaultUri;
import static android.media.RingtoneManager.getRingtone;

public class MainActivity extends AppCompatActivity {

    private static Context mContext;
    private static Activity mActivity;
    private static SharedPreferences mSettings;
    private static ConnectivityManager mConnMgr;
    private BluetoothManager btManager;
    private final static int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter.LeScanCallback lesScanCallBack;
    private BluetoothAdapter btAdapter;
    private BluetoothGattCallback btleGattCallback;
    private BluetoothGatt bluetoothGatt;
    private RecyclerView mRecyclerView;
    private RecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private final int PERMISSION_ACCESS_COARSE_LOCATION = 0;
    private CalendarManager calendarManager;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TextView statusBLE = (TextView) findViewById(R.id.doorStatusText);
            TextView contentOfCalendar = (TextView) findViewById(R.id.itensForTest);


            String actionName = intent.getStringExtra("Status BLE").substring(0,9);
            String actionStatus = intent.getStringExtra("Status BLE").substring(11);
            Log.e("Action status",actionStatus);
            Log.v("Acion name",actionName);

            List<String> calendarResult = calendarManager.getCalendarRetults();
            calendarManager.startRepeatingTask();

            //remove it when fixed mock data
            if (calendarResult.isEmpty()) {
                calendarResult.add("Levar a vó na musculação");
                calendarResult.add("Comprar gelo");
                calendarResult.add("Pegar o cachorro no pet shop");
                calendarResult.add("Pagar o Celso");
            }

            switch (actionName) {
                case "STATUSBLE":
                    statusBLE.setText("");
                    statusBLE.setText("Bluetooth Connected");

                    playNotificationSound();
                    break;
                case "CLOSENEDO":
                    playNotificationSound();
                    statusBLE.setText("");
                    break;

                case "OPENEDDOO":
                    playNotificationSound();

                    //statusBLE.setText(actionStatus);

                    statusBLE.setText("");

                    for(String item : calendarResult) {
                        statusBLE.append(item);
                        statusBLE.append("\n");
                        Log.v("MainActivity","");
                    }

                    //statusBLE.setText(contentOfCalendar);


                    break;
            }

            //Log.e("Resultado", "" + intent.getStringExtra("Status BLE"));
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        mActivity = this;
        mSettings = getPreferences(Context.MODE_PRIVATE);
        mConnMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        calendarManager = CalendarManager.getInstance();

        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new RecyclerAdapter();

        mRecyclerView.setAdapter(mAdapter);
        checkPermitions();
        startService(new Intent(getBaseContext(), BluetoothConnection.class));

        registerReceiver(broadcastReceiver, new IntentFilter(BluetoothConnection.BROADCAST_ACTION));
    }


    public static Context getContext() {
        return mContext;
    }

    public static  Activity getActivity() {
        return mActivity;
    }

    public static SharedPreferences getSettings() {
        return mSettings;
    }

    public static ConnectivityManager getConnMgr() {
        return mConnMgr;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        calendarManager.onActivityResult(requestCode, resultCode, data);
    }


    private void checkPermitions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_ACCESS_COARSE_LOCATION);
        } else {
            btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
            //checkBLEAvaiability();
            //calendarManager.startRepeatingTask();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
                    //checkBLEAvaiability();
                    //calendarManager.startRepeatingTask();
                }

                break;
        }
    }

    private void defineButtonClick() {
        final Button button = (Button) findViewById(R.id.scan_button);
        button.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                Log.v("BYouLeave","Start Scan...");
                btAdapter.startLeScan(lesScanCallBack);
            }

        });
    }

    public void playNotificationSound() {
        Uri notification = getDefaultUri(TYPE_NOTIFICATION);
        Ringtone r = getRingtone(getApplicationContext(), notification);
        r.play();
    }
}
