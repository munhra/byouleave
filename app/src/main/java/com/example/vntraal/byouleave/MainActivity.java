package com.example.vntraal.byouleave;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.media.RingtoneManager.TYPE_NOTIFICATION;
import static android.media.RingtoneManager.getDefaultUri;
import static android.media.RingtoneManager.getRingtone;
import static com.google.api.client.http.HttpMethods.HEAD;

public class MainActivity extends AppCompatActivity {

    private static Context mContext;
    private static Activity mActivity;
    private static SharedPreferences mSettings;
    private static ConnectivityManager mConnMgr;
  //  private BluetoothManager btManager;
 //   private final static int REQUEST_ENABLE_BT = 1;
 //   private BluetoothAdapter.LeScanCallback lesScanCallBack;
 //   private BluetoothAdapter btAdapter;
//    private BluetoothGattCallback btleGattCallback;
//    private BluetoothGatt bluetoothGatt;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter meuAdapter = new EventAdapter();
    private RecyclerView.LayoutManager mLayoutManager;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName componentName = null;

    private final int PERMISSION_ACCESS_COARSE_LOCATION = 0;
    private CalendarManager calendarManager;
    private AlertDialog.Builder builder;
    private AlertDialog dialog;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //TextView statusBLE = (TextView) findViewById(R.id.BluetoothStatus);
            TextView doorAction = (TextView) findViewById(R.id.doorStatusText);


           /* String actionName = intent.getStringExtra("Status BLE").substring(0,9);
            String actionStatus = intent.getStringExtra("Status BLE").substring(11);
            Log.e("BR",actionStatus);*/

           String status = intent.getStringExtra("Status DOOR").substring(0,12);

        //    playNotificationSound();

            List<String> calendarResult = new ArrayList<String>(calendarManager.getCalendarRetults());

            Log.e("status",status);

            switch (status){
                case "Conectado000":
                    doorAction.setText("Wifi Connected");
                    playNotificationSound();
                    break;

                case "Porta Aberta":
                    ArrayList<String> lista = new ArrayList<String>(calendarManager.getCalendarRetults());
                    ((EventAdapter) meuAdapter).setmData(lista);
                    playNotificationSound();
                    Log.e("Action", "Door has been opened");
                    doorAction.setText("Porta Aberta");
                    Unlock();
                    break;

                case "Porta Fechad":
                    ((EventAdapter) meuAdapter).resetData();
                    playNotificationSound();
                    Log.e("Action", "Door has been closed");
                    doorAction.setText("Porta Fechada");
                    fadeOut();
                    break;

                default: doorAction.setText("Problems with the Header"); doorAction.setText("Problems with the Header"); break;
            }

     /*       switch (status) {
                case "DOORSTATU":
                    doorAction.setText(status);

                    break;
                case "STATUSBLE":
                    switch(actionStatus.trim()){
                        case "2":
                            doorAction.setText("Bluetooth Connected");
                            Toast.makeText(getContext(),"Bluetooth Connected", Toast.LENGTH_LONG).show();
                            break;
                        default: doorAction.setText("Bluetooth Disconnected");
                            Toast.makeText(getContext(),"Trying to Reconnect", Toast.LENGTH_LONG).show();
                            stopService(new Intent(getBaseContext(), BluetoothConnection.class));

                            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                            if (!mBluetoothAdapter.isEnabled()) {
                                mBluetoothAdapter.enable();
                            }

                            startService(new Intent(getBaseContext(), BluetoothConnection.class));
                            break;
                    }

                    playNotificationSound();
                    break;
                case "OPENEDDOO":
                    playNotificationSound();

<<<<<<< HEAD
                    doorAction.setText(actionStatus);

                    //192.168.42.1:3000/api/sensor/?mac=5C:CF:7F:8F:6E:83&presence=1&ip=0.0.0.0&roomname="garage"

                    switch (actionStatus.trim()){
=======

                    doorAction.setText(status);
                    switch (status.trim()){
>>>>>>> TASK9-ReplaceBLEbyWIFI
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
                        default: doorAction.setText("Problems with the Message"); doorAction.setText("Problems with the Message"); break;
                    }

                    break;
                default: doorAction.setText("Problems with the Header"); doorAction.setText("Problems with the Header"); break;

            }*/
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
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

        registerReceiver(broadcastReceiver, new IntentFilter(WifiConnection.BROADCAST_ACTION));
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
        super.onActivityResult(requestCode, resultCode, data);
        calendarManager.onActivityResult(requestCode, resultCode, data);
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.example.vntraal.byouleave.BluetoothConnection".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    private void checkPermitions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_ACCESS_COARSE_LOCATION);
        } else {
            calendarManager = CalendarManager.getInstance();
            calendarManager.startTask();
            startService(new Intent(getBaseContext(), WifiConnection.class));

            builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Trying to reconnect...").setTitle("Server not responding");
            dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    calendarManager.startTask();
                    startService(new Intent(getBaseContext(), WifiConnection.class));
                }

                break;
        }
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
