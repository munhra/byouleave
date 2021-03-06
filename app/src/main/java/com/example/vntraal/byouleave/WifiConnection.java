package com.example.vntraal.byouleave;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.vntraal.byouleave.BluetoothConnection.BROADCAST_ACTION;
import static com.example.vntraal.byouleave.MainActivity.getActivity;

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


    private AlertDialog.Builder builder;
    private AlertDialog dialog;

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
        return START_NOT_STICKY;
    }

    private void checkWIFIAvaiability(){
        Log.e("Service State", "Entering in WifiACTIVITY");
        myWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(myWifiManager != null && !myWifiManager.isWifiEnabled()) {

            Log.e("BYouLeave","Wifi not ready, enabling");
            WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            wifi.setWifiEnabled(true);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent atualizarStatusBLE = new Intent(BROADCAST_ACTION);
                    atualizarStatusBLE.putExtra("Status DOOR", "RESTART00000");
                    sendBroadcast(atualizarStatusBLE);
                }
            }, 5000);
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

                        Intent atualizarStatusBLE = new Intent(BROADCAST_ACTION);
                        atualizarStatusBLE.putExtra("Status DOOR", "CONNECTED000");
                        sendBroadcast(atualizarStatusBLE);

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
                        Intent atualizarStatusBLE = new Intent(BROADCAST_ACTION);
                        atualizarStatusBLE.putExtra("Status DOOR", "SERVERERROR0");
                        sendBroadcast(atualizarStatusBLE);
                        Log.e("Service State", "Erro ao obter Ip do ESP");

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
                atualizarStatusBLE.putExtra("Status DOOR", "RESTART00000");
                sendBroadcast(atualizarStatusBLE);
                Log.e("Service State", "Wifi Não Conectado");
            }

        }

    }

    private void recieveMessages() throws IOException {
        try {
            mySocket = new Socket(ipESP,portEsp);

            Log.e("e","Conectado");

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
                while(mySocket.isConnected()) {
                    mydataInputStream = new DataInputStream(mySocket.getInputStream());
                    mydataInputStream.read(buf);
                    String status = new String(buf);
                    Intent atualizarStatusBLE = new Intent(BROADCAST_ACTION);
                    atualizarStatusBLE.putExtra("Status DOOR", status);
                    sendBroadcast(atualizarStatusBLE);
                    Log.e("pass",status);
                }
                Log.e("Sckt", "Socket has been disconnected");
                mydataInputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
                Intent atualizarStatusBLE = new Intent(BROADCAST_ACTION);
                atualizarStatusBLE.putExtra("Status DOOR", "Wifi Nao Conectado");
                sendBroadcast(atualizarStatusBLE);
                Log.e("Service State", "Wifi Não Conectado");

                try {
                    mydataInputStream.close();
                    mySocket.close();
                    Log.e("ScreenSTS","Socket has been closed, along with the InputStream");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                onDestroy();

            } catch(Exception exc){
                Log.e("General Exception", exc.getMessage());
            }
           /* finally {
                Toast.makeText(getApplicationContext(),"Ending Connection", Toast.LENGTH_SHORT).show();
            }
            */
        }
    }
}