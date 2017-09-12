package com.example.vntraal.byouleave;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.media.Image;
import android.media.Ringtone;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.support.annotation.IdRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

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
    private AlertDialog.Builder builder;
    private AlertDialog dialog;


    private BroadcastReceiver lockScreenReceiver;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //TextView statusBLE = (TextView) findViewById(R.id.BluetoothStatus);


            String actionName = intent.getStringExtra("Status BLE").substring(0,9);
            String actionStatus = intent.getStringExtra("Status BLE").substring(11);

            List<String> calendarResult = new ArrayList<String>(calendarManager.getCalendarRetults());

            switch (actionName) {
                case "DOORSTATU":
                    TextView doorAction = (TextView) findViewById(R.id.doorStatusText);
                    doorAction.setText(actionStatus);

                    break;
                case "STATUSBLE":
                    switch(actionStatus.trim()){
                        case "2":
                            doorAction = (TextView) findViewById(R.id.doorStatusText);
                            doorAction.setText("Bluetooth Connected");
                            Toast.makeText(getContext(),"Bluetooth Connected", Toast.LENGTH_LONG).show();
                            break;
                        default:
                            doorAction = (TextView) findViewById(R.id.doorStatusText);
                            doorAction.setText("Bluetooth Disconnected");
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

                    //192.168.42.1:3000/api/sensor/?mac=5C:CF:7F:8F:6E:83&presence=1&ip=0.0.0.0&roomname="garage"
                    //final ViewGroup viewGroup = (ViewGroup) findViewById(R.id.mainLayout);
                    //viewGroup.removeAllViews();
                    //viewGroup.setPadding(0, 0, 0, 0);
                    Intent changeToDoorLockedView = new Intent(getContext(), LockedScreen.class);
                    Intent changeToDoorOpenedView = new Intent(getContext(), MainActivity.class);

                    switch (actionStatus.trim()){
                        case "Switch CLOSED":
                            startActivityForResult(changeToDoorLockedView, 0);
                            break;
                        case "Switch OPEN":
                            finishActivity(0);

                            doorAction = (TextView) findViewById(R.id.doorStatusText);
                            doorAction.setText(actionStatus);

                            ArrayList<String> lista = new ArrayList<String>(calendarManager.getCalendarRetults());
                            ((EventAdapter) meuAdapter).setmData(lista);
                            for(String event : lista){Log.e("X", event);}
                            Log.e("Go", "Jussssst Go!");

                            break;
                        default:
                            doorAction = (TextView) findViewById(R.id.doorStatusText);
                            doorAction.setText("Problems with the Message"); doorAction.setText("Problems with the Message"); break;
                    }

                    break;
                default:
                    doorAction = (TextView) findViewById(R.id.doorStatusText);
                    doorAction.setText("Problems with the Header"); doorAction.setText("Problems with the Header"); break;

            }
        }
    };

    private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

            LinearLayoutManager layoutManager = ((LinearLayoutManager)recyclerView.getLayoutManager());
            int pos = layoutManager.findLastCompletelyVisibleItemPosition();
            int numItems = recyclerView.getAdapter().getItemCount();
            ImageView arrowDownAnimation = (ImageView) findViewById(R.id.arrowDownAnimation);

            if (pos >= numItems - 1) {
                arrowDownAnimation.setVisibility(View.INVISIBLE);
                arrowDownAnimation.setBackgroundResource(R.drawable.arrow_down_animation);
                AnimationDrawable anim = (AnimationDrawable) arrowDownAnimation.getBackground();
                anim.stop();
            } else {
                arrowDownAnimation.setVisibility(View.VISIBLE);
                arrowDownAnimation.setBackgroundResource(R.drawable.arrow_down_animation);
                AnimationDrawable anim = (AnimationDrawable) arrowDownAnimation.getBackground();
                anim.start();
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

        mRecyclerView.addOnScrollListener(mOnScrollListener);

        mRecyclerView.addOnItemTouchListener(new RecyclerItemListener(getApplicationContext(), mRecyclerView,
                new RecyclerItemListener.RecyclerTouchListener() {
                    public void onClickItem(View v, int position) {

                        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                        alert.setTitle(calendarManager.getCalendarRetults().get(position));
                        alert.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //Your action here
                            }
                        });
                        alert.show();
                    }

                    public void onLongClickItem(View v, int position) {

                    }
        }));

        checkPermitions();

        registerReceiver(broadcastReceiver, new IntentFilter(BluetoothConnection.BROADCAST_ACTION));
    }

    public interface VolleyCallback{
        void onSuccess(String result);
        void onErrorResponse(VolleyError error);
    }

    public void reconectionAsynchronousTask(final VolleyCallback callback) {

        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            //ArrayList<String> lista = new ArrayList<String>(calendarManager.getCalendarRetults());
                            //((EventAdapter) meuAdapter).setmData(lista);
                            //Log.e("Assync", "Assync updated RecyclerView Adapter Data");

                            // Instantiate the RequestQueue.
                            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                            String url ="http://192.168.42.1:3000/garage";

                            // Request a string response from the provided URL.
                            final StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                                    new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            callback.onSuccess(response);
                                        }
                                    }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    callback.onErrorResponse(error);
                                }
                            }) {
                                @Override
                                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                                    int mRequestCode = response.statusCode;
                                    return super.parseNetworkResponse(response);
                                }
                            };

                            queue.add(stringRequest);
                          
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 2000); //execute in every 50000 ms
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
            btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
            calendarManager.startTask();
            startService(new Intent(getBaseContext(), BluetoothConnection.class));

            builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Trying to reconnect...").setTitle("Server not responding");
            dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);

            reconectionAsynchronousTask(new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        if(dialog.isShowing()){
                            dialog.dismiss();
                            startService(new Intent(getBaseContext(), BluetoothConnection.class));
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(!dialog.isShowing()){
                            dialog.show();
                            stopService(new Intent(getBaseContext(), BluetoothConnection.class));
                        }

                    }
            });
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
                    btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
                    calendarManager.startTask();
                    startService(new Intent(getBaseContext(), BluetoothConnection.class));

                    reconectionAsynchronousTask(new VolleyCallback() {
                        @Override
                        public void onSuccess(String result) {
                            dialog.dismiss();
                        }

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if(!dialog.isShowing()){
                                if(!dialog.isShowing()){
                                    dialog.show();
                                }
                            }
                        }
                    });
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
