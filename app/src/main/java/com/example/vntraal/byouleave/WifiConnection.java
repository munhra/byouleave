package com.example.vntraal.byouleave;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
    public static String netWorkSSID = "Jonaphael ESP";
    public static  String  netWorkPass="esp82660";
    byte[] buf = new byte[1024]; // to send Informations

    //Components Layout
    Button myTurnButton;


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

        // TURN BUTTON
        myTurnButton =  (Button) findViewById(R.id.turn);
        myTurnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               // if the wifi is on turn it off
                if(myWifiManager.isWifiEnabled()){
                    myWifiManager.setWifiEnabled(false);
                }
                // setting the permissions to WRITE SSETTINGS
                settingPermission();
                setConfigurations();

                try{
                    activate();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    public void setConfigurations(){

        myWifiConfiguration = new WifiConfiguration();

        if(netWorkPass==""){
            myWifiConfiguration.SSID = netWorkSSID;
            myWifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            myWifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            myWifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            myWifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }
        else {

            myWifiConfiguration.SSID = netWorkSSID;
            myWifiConfiguration.preSharedKey = netWorkPass;
            myWifiConfiguration.status = WifiConfiguration.Status.ENABLED;
            myWifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            myWifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            myWifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            myWifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            myWifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            myWifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            myWifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
         //   myWifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
          //  myWifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
           // myWifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            //myWifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            //myWifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
           // myWifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
           // myWifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            //myWifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        }
    }

    public void activate() throws Exception{
        Method setWifiApMethod = myWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
      /*  boolean apstatus=(Boolean) setWifiApMethod.invoke(myWifiManager, myWifiConfiguration,true);

        Method isWifiApEnabledmethod = myWifiManager.getClass().getMethod("isWifiApEnabled");
        while(!(Boolean)isWifiApEnabledmethod.invoke(myWifiManager)){};
        Method getWifiApStateMethod = myWifiManager.getClass().getMethod("getWifiApState");
        int apstate=(Integer)getWifiApStateMethod.invoke(myWifiManager);
        Method getWifiApConfigurationMethod = myWifiManager.getClass().getMethod("getWifiApConfiguration");
        myWifiConfiguration=(WifiConfiguration)getWifiApConfigurationMethod.invoke(myWifiManager);*/
        setWifiApMethod.invoke(myWifiManager, myWifiConfiguration,false);
        myWifiManager.saveConfiguration();
        Toast.makeText(getApplicationContext(),"SSID:"+myWifiConfiguration.SSID+"Password:"+myWifiConfiguration.preSharedKey+"", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getApplicationContext(),"Turning on wifi ...", Toast.LENGTH_SHORT).show();

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
            Toast.makeText(getApplicationContext(),"Turning off wifi ...", Toast.LENGTH_SHORT).show();
        }
    }

    //to Turn the wifi on or Off
  /*  public static void turnONOFFWifi(Context context, boolean value){
        myWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        myWifiManager.setWifiEnabled(value);
    }
    */

    //to send datato esp
    public class Client implements Runnable{
        private final static String SERVER_ADRESS = "";
        private final static int    SERVER_PORT = 8888;

        @Override
        public void run() {
            InetAddress serverAdress;
            DatagramPacket dtPacket;
            DatagramSocket dtSocket = null;

            try{
                serverAdress = InetAddress.getByName(SERVER_ADRESS);
                dtSocket = new DatagramSocket();
                dtPacket = new DatagramPacket(buf, buf.length, serverAdress, SERVER_PORT);
                dtSocket.send(dtPacket);
            }
            catch (UnknownHostException err){
                err.printStackTrace();
            }
            catch (SocketException err){
                err.printStackTrace();
            }
            catch (IOException err){
                err.printStackTrace();
            }
            finally {
                if(!dtSocket.isClosed()){
                    dtSocket.close();
                }
            }
        }
    }

}
