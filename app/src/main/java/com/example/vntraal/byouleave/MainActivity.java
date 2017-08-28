package com.example.vntraal.byouleave;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
    private RecyclerView.Adapter meuAdapter = new EventAdapter();
    private RecyclerView.LayoutManager mLayoutManager;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName componentName = null;

    private final int PERMISSION_ACCESS_COARSE_LOCATION = 0;
    private CalendarManager calendarManager;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TextView statusBLE = (TextView) findViewById(R.id.BluetoothStatus);
            TextView doorAction = (TextView) findViewById(R.id.DoorStatus);


            String actionName = intent.getStringExtra("Status BLE").substring(0,9);
            String actionStatus = intent.getStringExtra("Status BLE").substring(11);
            Log.e("BR",actionStatus);

            List<String> calendarResult = new ArrayList<String>(calendarManager.getCalendarRetults());

            switch (actionName) {
                case "DOORSTATU":
                    doorAction.setText(actionStatus);

                    break;
                case "STATUSBLE":
                    statusBLE.setText("Bluetooth Connected");

                    playNotificationSound();
                    break;
                case "OPENEDDOO":
                    playNotificationSound();


                    doorAction.setText(actionStatus);
                    switch (actionStatus.trim()){
                        case "Switch CLOSED":
                            ((EventAdapter) meuAdapter).resetData();
                            Log.e("Action", "Door has been closed");
                            fadeOut();
                            break;
                        case "Switch OPEN":
                            ArrayList<String> lista = new ArrayList<String>(calendarManager.getCalendarRetults());
                            ((EventAdapter) meuAdapter).setmData(lista);
                            Log.e("Action", "Door has been opened");
                            Unlock();
                            break;
                        default: statusBLE.setText("Problems with the Message"); doorAction.setText("Problems with the Message"); break;
                    }

                    break;
                default: statusBLE.setText("Problems with the Header"); doorAction.setText("Problems with the Header"); break;

            }
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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);


        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.setAdapter(meuAdapter);

        checkPermitions();

        registerReceiver(broadcastReceiver, new IntentFilter(BluetoothConnection.BROADCAST_ACTION));


    }


    public void callAsynchronousTask() {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {

            @Override
            public void run() {

                handler.post(new Runnable() {
                    public void run() {
                        try {
                            ArrayList<String> lista = new ArrayList<String>(calendarManager.getCalendarRetults());
                            ((EventAdapter) meuAdapter).setmData(lista);
                            Log.e("Assync", "Assync updated RecyclerView Adapter Data");
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 5000); //execute in every 50000 ms
    };


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
        super.onActivityResult(requestCode, resultCode, data);
        calendarManager.onActivityResult(requestCode, resultCode, data);
    }


    private void checkPermitions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_ACCESS_COARSE_LOCATION);
        } else {
            btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
            calendarManager.startTask();
            //callAsynchronousTask();
            startService(new Intent(getBaseContext(), BluetoothConnection.class));
            //checkBLEAvaiability();
            //calendarManager.startRepeatingTask();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
                    calendarManager.startTask();
                    //callAsynchronousTask();
                    startService(new Intent(getBaseContext(), BluetoothConnection.class));
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
                Log.e("BT","Button was Clicked!");
            }

        });
    }

    public void playNotificationSound() {
        Uri notification = getDefaultUri(TYPE_NOTIFICATION);
        Ringtone r = getRingtone(getApplicationContext(), notification);
        r.play();
    }

    public void fadeOut(){
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.screenBrightness = 0;
        getWindow().setAttributes(params);
    }

    public void Unlock(){
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.screenBrightness = 1.0f;
        getWindow().setAttributes(params);

        KeyguardManager.KeyguardLock lock = ((KeyguardManager) getSystemService(Activity.KEYGUARD_SERVICE)).newKeyguardLock(KEYGUARD_SERVICE);
        PowerManager powerManager = ((PowerManager) getSystemService(Context.POWER_SERVICE));
        PowerManager.WakeLock wake = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");

        lock.disableKeyguard();
        wake.acquire();
    }
}
