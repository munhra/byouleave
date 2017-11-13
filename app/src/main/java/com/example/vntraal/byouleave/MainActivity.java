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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.Image;
import android.media.Ringtone;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.api.client.util.ArrayMap;


import org.w3c.dom.Text;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
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
    TextView placeholderMonday;
    TextView placeholderTuesday;
    TextView placeholderWednesday;
    TextView placeholderThursday;
    TextView placeholderFriday;
    TextView placeholderSaturday;
    TextView placeholderSunday;
    TextView nextWeekButton;
    TextView previousWeekButton;
    RelativeLayout sunday;
    RelativeLayout monday;
    RelativeLayout tuesday;
    RelativeLayout wednesday;
    RelativeLayout thursday;
    RelativeLayout friday;
    RelativeLayout saturday;


    private final int PERMISSION_ACCESS_COARSE_LOCATION = 0;
    private CalendarManager calendarManager;

    AlertDialog.Builder builder;
    AlertDialog dialog;

    private static boolean isScreenLocked = false;

    final Handler handler = new Handler();

    private BroadcastReceiver lockScreenReceiver;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String status = "";

            try{
                status = intent.getStringExtra("Status DOOR").substring(0,12);
            } catch (NullPointerException npe){
                Log.e("Err",npe.getMessage());
                status = "";
            }
            TextView doorAction = (TextView) findViewById(R.id.doorStatusText);

            List<String> calendarResult = new ArrayList<String>(calendarManager.getCalendarRetults());
            Intent changeToDoorLockedView = new Intent(getContext(), LockedScreen.class);

            //Log.e("status",status);

            switch (status){
                case "CONNECTED000":
                    playNotificationSound();

                    //connectionWatcher();

                    Log.e("BroadcastAction","Received Connected to Wifi");
                    //doorAction.setText("Connection Stabilished");

                    if(dialog.isShowing()){
                        dialog.dismiss();
                    }

                    break;

                case "OPEN00000000":
                    playNotificationSound();
                    if(isScreenLocked = true){
                        finishActivity(0); //End Locked Screen Activity
                        ArrayList<String> lista = new ArrayList<String>(calendarManager.getCalendarRetults());
                        ArrayMap<String, String> week = new ArrayMap<String, String>();
                        week = calendarManager.getWeekDays();
                        Log.e("FINAL COUNTDOWN", week.get("today"));
                        doorAction.setText("Today, " + week.get("month") + " " + week.get("today"));

                        setupWeek(week.get("today"),week);

                        ((EventAdapter) meuAdapter).setmData(lista);
                        playNotificationSound();
                        Log.e("Action", "Door has been opened");
                        //doorAction.setText("Opened Door");
                        Unlock();
                    }
                    isScreenLocked = false;
                    Log.e("BroadcastAction","Received Opened Door");
                    break;

                case "CLOSE0000000":
                    playNotificationSound();
                    Log.e("BroadcastAction","Received Closed Door");
                    if(isScreenLocked == false){
                        startActivityForResult(changeToDoorLockedView, 0);
                        //((EventAdapter) meuAdapter).resetData();
                        playNotificationSound();
                        Log.e("Action", "Door has been closed");
                        //doorAction.setText("Closed Door");
                    }
                    isScreenLocked = true;


                    break;

                case "RESTART00000":
                    Log.e("BroadcastAction","Received Restart Wifi Service");
                    doorAction.setText("Connection being restarted");
                    if(!dialog.isShowing()) {
                        dialog.show();
                    }

                    WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                    if(!wifi.isWifiEnabled()){
                        wifi.setWifiEnabled(true);
                    }

                    stopService(new Intent(getBaseContext(), WifiConnection.class));
                    restartService();


                    break;

                case "SERVERERROR0":
                    playNotificationSound();
                    doorAction.setText("Huzza Feather wrong IP");
                    Log.e("BroadcastAction","Received Server IP Error");
                    dialog.setTitle("Problems on the IP Request");
                    builder.create();
                    if(!dialog.isShowing()){
                        dialog.show();
                    }
                    stopService(new Intent(getBaseContext(), WifiConnection.class));
                    restartService();

                    break;

                case "HUZZAH ON000":
                    Log.e("Sts","Huzza is alive");
                    break;
                case "Wifi Nao Con":
                    stopService(new Intent(getBaseContext(), WifiConnection.class));
                    doorAction.setText("Disconnected");
                    Log.e("BroadcastAction","Disconnected from Raspberry");
                    dialog.setTitle("Disconnected from Raspberry");
                    builder.create();
                    if(!dialog.isShowing()){
                        dialog.show();
                    }

                    restartService();

                    break;
                default:
                    Log.e("BroadcastAction","Received Undefined Broadcast:" + status);
                    doorAction.setText("???????" + status);
                    //stopService(new Intent(getBaseContext(), WifiConnection.class));
                    //restartService();
                    break;
            }
        }
    };

    public void setupWeek(String today, ArrayMap<String,String> x){
        Log.e("ANOTHER TEST",x.get("Mon") + "");
        placeholderMonday.setText(x.get("Mon"));
        placeholderTuesday.setText(x.get("Tue"));
        placeholderWednesday.setText(x.get("Wed"));
        placeholderThursday.setText(x.get("Thu"));
        placeholderFriday.setText(x.get("Fri"));
        placeholderSaturday.setText(x.get("Sat"));
        placeholderSunday.setText(x.get("Sun"));

        switch (x.get("weekday")){
            case "Mon":
                monday.setBackgroundColor(Color.parseColor("#5FDDFF")); break;
            case "Tue":
                tuesday.setBackgroundColor(Color.parseColor("#5FDDFF")); break;
            case "Wed":
                wednesday.setBackgroundColor(Color.parseColor("#5FDDFF")); break;
            case "Thu":
                thursday.setBackgroundColor(Color.parseColor("#5FDDFF")); break;
            case "Fri":
                friday.setBackgroundColor(Color.parseColor("#5FDDFF")); break;
            case "Sat":
                saturday.setBackgroundColor(Color.parseColor("#5FDDFF")); break;
            case "Sun":
                sunday.setBackgroundColor(Color.parseColor("#5FDDFF")); break;

        }
    }

    public void restartService(){
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
                if(isScreenLocked == true){
                    finishActivity(0);
                    isScreenLocked = false;
                }
                Log.e("Restart","Service restarted");
                startService(new Intent(getBaseContext(), WifiConnection.class));
            }
        }, 5000);
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void onDestroy(){
        Log.e("OnDestroy","Broadcast Receiver closed");
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();

    }

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        mActivity = this;
        mSettings = getPreferences(Context.MODE_PRIVATE);
        mConnMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        calendarManager = CalendarManager.getInstance();
        View decorView = getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        int ui_Options = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(ui_Options);
        setContentView(R.layout.updated_activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.setAdapter(meuAdapter);

        checkPermitions();

        registerReceiver(broadcastReceiver, new IntentFilter(WifiConnection.BROADCAST_ACTION));

        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Server not responding");
        builder.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                restartService();
            }
        });
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        placeholderMonday = (TextView)findViewById(R.id.monday_day);
        placeholderTuesday = (TextView)findViewById(R.id.tuesday_day);
        placeholderWednesday = (TextView)findViewById(R.id.wednesday_day);
        placeholderThursday = (TextView)findViewById(R.id.thursday_day);
        placeholderFriday = (TextView)findViewById(R.id.friday_day);
        placeholderSaturday = (TextView)findViewById(R.id.saturday_day);
        placeholderSunday = (TextView)findViewById(R.id.sunday_day);

        nextWeekButton = (TextView) findViewById(R.id.nextWeek);
        previousWeekButton = (TextView) findViewById(R.id.pastWeek);

        sunday = (RelativeLayout) findViewById(R.id.sunday);
        monday = (RelativeLayout) findViewById(R.id.monday);
        tuesday = (RelativeLayout) findViewById(R.id.tuesday);
        wednesday = (RelativeLayout) findViewById(R.id.wednesday);
        thursday = (RelativeLayout) findViewById(R.id.thursday);
        friday = (RelativeLayout) findViewById(R.id.friday);
        saturday = (RelativeLayout) findViewById(R.id.saturday);

        mRecyclerView.addOnItemTouchListener(new RecyclerItemListener(getApplicationContext(), mRecyclerView,
                new RecyclerItemListener.RecyclerTouchListener() {
                    public void onClickItem(View v, int position) {

                        CheckBox mCheckBox;
                        mCheckBox = (CheckBox)  v.findViewById(R.id.eventCheckbox);

                        mCheckBox.setChecked(!mCheckBox.isChecked());
                    }

                    public void onLongClickItem(View v, int position) {

                    }}));

        nextWeekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.e("RESULT",calendarManager.getNextWeekEvents());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        previousWeekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.e("RESULT",calendarManager.getPastWeekEvents());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

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

    private void checkPermitions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_ACCESS_COARSE_LOCATION);
        } else {
            calendarManager = CalendarManager.getInstance();
            calendarManager.startTask();
            startService(new Intent(getBaseContext(), WifiConnection.class));
        }
    }



    public void makeFullScreen() {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if(Build.VERSION.SDK_INT < 19) { //View.SYSTEM_UI_FLAG_IMMERSIVE is only on API 19+
            this.getWindow().getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        } else {
            this.getWindow().getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE);
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
