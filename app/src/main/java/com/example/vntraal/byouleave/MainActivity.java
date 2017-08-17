package com.example.vntraal.byouleave;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import static android.media.RingtoneManager.*;

public class MainActivity extends AppCompatActivity {

    private static final String BLE_SERVICE = "0000ffe0-0000-1000-8000-00805f9b34fb";
    private static final String BLE_CHARACTERISTIC = "0000ffe1-0000-1000-8000-00805f9b34fb";
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

    private final int PERMISSION_ACCESS_COARSE_LOCATION = 0;
    private CalendarManager calendarManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        mActivity = this;
        mSettings = getPreferences(Context.MODE_PRIVATE);
        mConnMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        calendarManager = CalendarManager.getInstance();
        setContentView(R.layout.activity_main);
        checkPermitions();
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
            checkBLEAvaiability();
            calendarManager.startRepeatingTask();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
                    checkBLEAvaiability();
                    calendarManager.startRepeatingTask();
                }

                break;
        }
    }

    private void checkBLEAvaiability() {
        btAdapter = btManager.getAdapter();
        if (btAdapter != null && !btAdapter.isEnabled()) {
            Log.v("BYouLeave","Adapter not ready");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,REQUEST_ENABLE_BT);
        }else{
            Log.v("BYouLeave","Adapter ready enable button");
            createScanCallBack();
            btAdapter.startLeScan(lesScanCallBack);
            defineButtonClick();
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

    private void createScanCallBack() {
        final Context context = this;
        lesScanCallBack = new BluetoothAdapter.LeScanCallback() {

            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                Log.v("BYouLeave","Scan BLE callback ! "+device.getName());
                if ("TESTNAME".equals(device.getName())) {
                    Log.v("BYouLeave","FOUND HMSOFT ! "+device.getName());
                    btAdapter.stopLeScan(lesScanCallBack);
                    createConnectionCallBack();
                    bluetoothGatt = device.connectGatt(context,false,btleGattCallback);
                }
            }
        };
    }

    private void createConnectionCallBack() {
        btleGattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                //super.onConnectionStateChange(gatt, status, newState);
                Log.v("BYouLeave","onConnectionStateChange");
                bluetoothGatt.discoverServices();
                final TextView statusText = (TextView) findViewById(R.id.doorStatusText);
                statusText.post(new Runnable() {
                    @Override
                    public void run() {
                        statusText.setText("BLE Connected");
                    }
                });

            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                Log.v("BYouLeave","onServicesDiscovered");
                List<BluetoothGattService> services = gatt.getServices();

                for (BluetoothGattService service:services) {
                    Log.v("BYouLeave", "Service " + service.getUuid());
                    if (BLE_SERVICE.equals(service.getUuid().toString())) {
                        List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                        for (BluetoothGattCharacteristic characteristic : characteristics) {
                            if (BLE_CHARACTERISTIC.equals(characteristic.getUuid().toString())) {
                                Log.v("BYouLeave", "Found the Characteristic" + characteristic.getUuid());
                                for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                                    Log.v("BYouLeave", "Descriptors " + descriptor.getUuid());
                                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                                    bluetoothGatt.writeDescriptor(descriptor);

                                }

                                bluetoothGatt.setCharacteristicNotification(characteristic,true);

                                break;
                            }
                        }
                        break;
                    }
                }
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                Log.v("BYouLeave","onCharacteristicRead");
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                Log.v("BYouLeave","onCharacteristicWrite");
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                Log.v("BYouLeave","onCharacteristicChanged "+bytesToString2(characteristic.getValue()));
                final String bleText = bytesToString2(characteristic.getValue());
                final TextView statusText = (TextView) findViewById(R.id.doorStatusText);
                statusText.post(new Runnable() {
                    @Override
                    public void run() {
                        statusText.setText(bleText);

                        if (bleText.equals("Switch OPEN")) {
                            Log.v("asdasdasdasd", "asdasdadsasd");
                        }

                        List<String> calendarResult = calendarManager.getCalendarRetults();
                        statusText.append("\n");
                        for(String item : calendarResult) {
                            statusText.append(item);
                            statusText.append("\n");
                        }
                        playNotificationSound();
                    }
                });


            }

            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorRead(gatt, descriptor, status);
                Log.v("BYouLeave","onDescriptorRead");
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorWrite(gatt, descriptor, status);
                Log.v("BYouLeave","onDescriptorWrite");
            }

            @Override
            public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
                super.onReliableWriteCompleted(gatt, status);
                Log.v("BYouLeave","onReliableWriteCompleted");
            }

            @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                super.onReadRemoteRssi(gatt, rssi, status);
                Log.v("BYouLeave","onReadRemoteRssi");
            }

            @Override
            public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                super.onMtuChanged(gatt, mtu, status);
                Log.v("BYouLeave","onMtuChanged");
            }
        };
    }

    public void playNotificationSound() {
        Uri notification = getDefaultUri(TYPE_NOTIFICATION);
        Ringtone r = getRingtone(getApplicationContext(), notification);
        r.play();
    }

    public static String bytesToString2(byte[] bytes) {
        String text = "";
        try {
            text =  new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return text;
    }

    public static String bytesToString(byte[] bytes){
        StringBuilder stringBuilder = new StringBuilder(
                bytes.length);
        for (byte byteChar : bytes)
            stringBuilder.append(String.format("%02X ", byteChar));
        return stringBuilder.toString();
    }
}
