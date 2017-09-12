package com.example.vntraal.byouleave;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;

import static com.example.vntraal.byouleave.BluetoothConnection.BROADCAST_ACTION;

/**
 * Created by Jonaphael on 8/28/2017.
 */

public class WifiConnection extends Service {

    static WifiManager myWifiManager;
    Context myContex;
    WifiConfiguration myWifiConfiguration;
    private static String netWorkSSID = "FollowMe-Pi3";
    private static String netWorkPass="FollowMeRadio";

    private Socket mySocket;
    private String ipServer = "192.168.42.1";
    private  int portServer = 3000;
    private String ipESP = "";
    private  int portEsp = 12345;
    DataInputStream mydataInputStream;
    byte [] buf  = new byte[20];

    IBinder myIBinder;
    LayoutInflater inflater;
    View layout;

    public static final String BROADCAST_ACTION = "com.example.tracking.updateprogress";

    @Override
    public IBinder onBind(Intent intent) {
        return myIBinder;
    }

    @Override
    public void onDestroy() {
        try {
            if(mySocket!=null)
                mySocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e("Service Lifestyle", "Ending Service");
        super.onDestroy();   }

    @Override
    public void onCreate() {
        Log.e("Service State", "Service has been created");
        myWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        StrictMode.ThreadPolicy myThreadPolicy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(myThreadPolicy);

        checkWIFIAvaiability();
        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        layout = inflater.inflate(R.layout.activity_main, null);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Searching and Connect to Wifi", Toast.LENGTH_SHORT).show();
        return START_NOT_STICKY;
    }

    private void checkWIFIAvaiability(){
        Log.e("Service State", "Entering in WifiACTIVITY");
        myWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(myWifiManager != null && !myWifiManager.isWifiEnabled()) {
            Log.e("BYouLeave","Wifi not ready");
            Intent enableIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            startActivity(enableIntent);
        }
        else{
            Log.e("BYouLeave","Adapter ready enable button");
            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if(mWifi.isConnected()) {
                try {
                    // pegar o ip no ervidor mediante o get

                    URL url = new URL("http://"+ipServer+":"+portServer+"/ipDoor");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();
                    Log.e("Mesage Request",connection.getResponseCode()+"");

                    if (connection.getResponseCode() == 200 ) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                        }
                        br.close();
                        ipESP = sb.toString();
                        Log.e("ip",ipESP);
                    }
                    else{
                        Toast.makeText(getApplicationContext(),"Erro ao obter Ip do ESP",Toast.LENGTH_SHORT).show();
                    }
                    connection.disconnect();

                    recieveMessages();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else{
                Intent atualizarStatusBLE = new Intent(BROADCAST_ACTION);
                atualizarStatusBLE.putExtra("Status DOOR", "Wifi Nao Conectado");
                sendBroadcast(atualizarStatusBLE);
                Log.e("Service State", "Wifi Não Conectado");
            }

        }

    }

    private void recieveMessages() throws IOException {
        try {
            mySocket = new Socket(ipESP,portEsp);

            Log.e("e","Conectado");

            if(mySocket.isConnected())
                Log.e("h","Socket Conectado");

            new ClientSide().start();

        } catch (IOException e) {
            e.printStackTrace();

        }

    }

    public class ClientSide extends Thread {

        @Override
        public void run() {
            Log.e("pass","pasou");
            try {
                while(true) {
                    mydataInputStream = new DataInputStream(mySocket.getInputStream());
                    mydataInputStream.read(buf);
                    String status = new String(buf);
                    Intent atualizarStatusBLE = new Intent(BROADCAST_ACTION);
                    atualizarStatusBLE.putExtra("Status DOOR", status);
                    sendBroadcast(atualizarStatusBLE);
                    Log.e("pass",status);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Intent atualizarStatusBLE = new Intent(BROADCAST_ACTION);
                atualizarStatusBLE.putExtra("Status DOOR", "Wifi Nao Conectado");
                sendBroadcast(atualizarStatusBLE);
                Log.e("Service State", "Wifi Não Conectado");
                onDestroy();
            }
           /* finally {
                Toast.makeText(getApplicationContext(),"Ending Connection", Toast.LENGTH_SHORT).show();
            }
            */
        }
    }
}