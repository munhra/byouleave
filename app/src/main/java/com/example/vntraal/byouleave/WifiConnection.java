package com.example.vntraal.byouleave;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by Jonaphael on 8/22/2017.
 */

public class WifiConnection extends AppCompatActivity {

    Boolean l1, turn;
    static WifiManager myWifiManager;
    Context myContext;
    WifiConfiguration myWifiConfiguration;
    public static String netWorkSSID = "SSID";
    public static  String  netWorkPass="PASSWORD";
    byte[] buf = new byte[50]; // to recieve Informations

    Socket socket;
    String ip = "192.168.137.56";
    int port = 12345;
    BufferedReader bufferedReader;

    //Components Layout
    Button myTurnButton;
    TextView textESP;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_layout);

        l1 = turn = true;
        myContext = this;

        myWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Policy to transfer data  ui=sing wifi - and permissions
        StrictMode.ThreadPolicy myThreadPolicy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(myThreadPolicy);

        //Textfrom esp
        textESP = (TextView)findViewById(R.id.txtESP);

        // TURN BUTTON
        myTurnButton =  (Button) findViewById(R.id.turn);
        myTurnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    readMessages();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // wifiConnect();
            }
        });
    }

    public void readMessages() throws Exception {
        try {

            socket = new Socket(ip,port);
            Log.e("e","Conectado");

            if(socket.isConnected())
                Log.e("h","Socket Conectado");

           // bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            DataInputStream dataInputStream;

            //este meetodo fica em loop eternamente - tranformar esta l[ogica em um serviço
            while(true) {
                dataInputStream = new DataInputStream(socket.getInputStream());
                int result = dataInputStream.read(buf);
                String ret2 = new String(buf);
                Log.e("e","out1S "+ret2);
                Log.e("e","outINT "+result);
                textESP = (TextView)findViewById(R.id.txtESP);
                textESP.setText(ret2);
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            Toast.makeText(getApplicationContext(),"Ending Connection", Toast.LENGTH_SHORT).show();
            socket.close();

        }
    }


    // TO TURN THE WIFI ON OR OFF
    public static void turnONOFFWifi(Context context, boolean value){
        myWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        myWifiManager.setWifiEnabled(value);
    }

    public void settingPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(getApplicationContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 200);
            }
        }
    }

    // method to connect wifi
    public void wifiConnect(){
        myWifiManager = (WifiManager) myContext.getSystemService(Context.WIFI_SERVICE);

        if (turn){
            turnONOFFWifi(myContext, turn);
            turn = false;
            Toast.makeText(myContext,"Turning on wifi ...", Toast.LENGTH_SHORT).show();

            // wifi configuration
            myWifiConfiguration = new WifiConfiguration();
            myWifiConfiguration.SSID = "\"" + netWorkSSID + "\"";
            myWifiConfiguration.preSharedKey = "\"" + netWorkPass + "\"";
            myWifiConfiguration.status = WifiConfiguration.Status.ENABLED;
            myWifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            myWifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            myWifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            myWifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            myWifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            myWifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);

            int netWorkId = myWifiManager.addNetwork(myWifiConfiguration);
            myWifiManager.disconnect();
            myWifiManager.enableNetwork(netWorkId,true);
            myWifiManager.reconnect();
        }
        else{
            turnONOFFWifi(myContext,turn);
            turn = true;
            Toast.makeText(myContext,"Turning off wifi ...", Toast.LENGTH_SHORT).show();
        }


        try {
            Thread.sleep(20000);
            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (mWifi.isConnected()) {
                readMessages();
            }
            else
            {
               Log.e("erro", "Nao se Conectou na rede ainda");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
