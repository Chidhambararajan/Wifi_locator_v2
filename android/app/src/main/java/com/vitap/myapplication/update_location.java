package com.vitap.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class update_location extends AppCompatActivity {
    WifiManager wifiManager ;
    TextView status ;
    Button update;
    String bssid,name,password,url;
    SharedPreferences sharedPref;
    EditText locationView;
    boolean jumpMode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_location);
        locationView = findViewById(R.id.location);
        sharedPref = getSharedPreferences("1",MODE_PRIVATE);
        status = findViewById(R.id.textView3);
        update = findViewById(R.id.button8);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
        name = sharedPref.getString("studentID","l");// Turns on Wifi
        password = sharedPref.getString("password","l");
        url = sharedPref.getString("LINK","l");
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.CHANGE_WIFI_STATE,Manifest.permission.ACCESS_WIFI_STATE,Manifest.permission.ACCESS_COARSE_LOCATION},1);
        registerReceiver(new BroadcastReceiver() {
                             @Override
                             public void onReceive(Context context, Intent intent) {
                                 List<ScanResult> results = wifiManager.getScanResults();
                                 Log.d("ola1","Scanning");
                                 if(results.size()!=0){
                                     bssid = results.get(0).BSSID;
                                     status.setText("nearby bssid identified , now you can update");
                                     update.setEnabled(true);
                                 }
                                 else{
                                     status.setText("no nearby wifi network identified , you cant update now");
                                     update.setEnabled(false);
                                 }
                             }
                         },
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        );
        if (!wifiManager.startScan()) {
            switch(wifiManager.getWifiState()){
                case WifiManager.WIFI_STATE_DISABLING:
                    Toast.makeText(this, "-------Disabling", Toast.LENGTH_LONG).show();
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    Toast.makeText(this,"------------Disabled", Toast.LENGTH_LONG).show();
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    Toast.makeText(this, "--------------Enabling", Toast.LENGTH_LONG).show();
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    Toast.makeText(this, "----------------Enabled", Toast.LENGTH_LONG).show();
                    break;
                case WifiManager.WIFI_STATE_UNKNOWN:
                    Toast.makeText(this,"-------------Unknown", Toast.LENGTH_LONG).show();
                    break;

            }
        } else {
            Toast.makeText(this,"-----------------Scanning",Toast.LENGTH_LONG).show();
        }
        if(wifiManager.isScanAlwaysAvailable()){
            System.out.println("--------------------yes");
        }
    }

    public void onPress(View v){
        JSONObject subJson = new JSONObject();
        JSONObject body = new JSONObject();
        if(!jumpMode) {
            try {
                subJson.put("studentID", name);
                subJson.put("password", password);
                subJson.put("bssid", bssid);
                body.put("mode", "UPDATE_LOCATION");
                body.put("info", subJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    JSONObject result = requests.request(url, body);
                    try {
                        if (((String) result.get("msg")).equals("UPDATION_SUCCESS")) {
                            writeToStatus("Success");
                        }
                        else{
                            writeToStatus("Current location name not registered , please enter the location name");
                            jumpMode=true;
                            enableLocationView();
                            //locationView.setVisibility(View.VISIBLE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        writeToStatus("Failed");
                    }
                }
            }).start();
        } else {
            try {
                subJson.put("studentID", name);
                subJson.put("password", password);
                subJson.put("bssid", bssid);
                if(locationView.getText().toString().equals(""))
                    return;
                subJson.put("location",locationView.getText().toString());
                body.put("mode", "ADD_BSSID_LOCATION");
                body.put("info", subJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    JSONObject result = requests.request(url, body);
                    try {
                        if (((String) result.get("msg")).equals("SUCCESS")) {
                            writeToStatus("Success");
                        }
                        else{
                            writeToStatus("Current location already registered");
                            jumpMode=true;
                        }
                        jumpMode = false;
                        onPress(new View(getApplicationContext()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        writeToStatus("Failed");
                    }
                }
            }).start();
        }
    }

    public void enableLocationView(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                locationView.setVisibility(View.VISIBLE);
            }
        });
    }
    public void writeToStatus(String str){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                status.setText(str);
            }
        });
    }
}