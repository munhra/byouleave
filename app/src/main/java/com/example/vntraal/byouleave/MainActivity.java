package com.example.vntraal.byouleave;

import android.Manifest;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import static android.media.RingtoneManager.*;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION},
                1);



        startService(new Intent(getBaseContext(), BluetoothConnection.class));

        BroadcastReceiver receiveDataFromService = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                Log.e("Status of Broadcast","Receiving Message");
                String action = intent.getAction();
                final TextView statusText = (TextView)findViewById(R.id.doorStatusText);

                switch(action){
                    case "TextView de Status":
                        String data = intent.getStringExtra("TextView de Status");
                        statusText.setText("BLE Connected");
                        break;
                    case "Porta Aberta":
                        statusText.post(new Runnable() {
                            @Override
                            public void run() {
                                statusText.setText(intent.getStringExtra("Porta Aberta"));
                                playNotificationSound();
                            }
                        });
                        break;
                    default:
                        Log.e("Error","Problems occured on the broadcast/receive of messages between MainActivity and BluetoothConnection Service");
                }



            }
        };

    }

    public void playNotificationSound() {
        Uri notification = getDefaultUri(TYPE_NOTIFICATION);
        Ringtone r = getRingtone(getApplicationContext(), notification);
        r.play();
    }




}
