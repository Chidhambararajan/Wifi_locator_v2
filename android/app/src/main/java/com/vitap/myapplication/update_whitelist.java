package com.vitap.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class update_whitelist extends AppCompatActivity {
    EditText whitelistView;
    SharedPreferences sharedPref ;
    SharedPreferences.Editor editor ;
    String url,name,password;
    TextView status;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_whitelist);
        whitelistView = findViewById(R.id.whitelist);
        whitelistView.setText("Please Wait , getting your current whitelist");
        sharedPref = getSharedPreferences("1",MODE_PRIVATE);
        //editor = sharedPref.edit();
        url = sharedPref.getString("LINK","l");
        name = sharedPref.getString("studentID","l");
        password = sharedPref.getString("password","l");
        status = findViewById(R.id.textView2);
        refreshWhitelist(new View(this));
    }

    public void writeToeditor(String str){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                whitelistView.setText(str);
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

    public void updateWhitelist(View v){
        JSONObject subJson = new JSONObject();
        JSONObject body = new JSONObject();
        try {
            subJson.put("studentID",name);
            subJson.put("password",password);
            subJson.put("whitelist",whitelistView.getText().toString());
            body.put("mode","UPDATE_WHITELIST");
            body.put("info",subJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }



        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject result = requests.request(url,body);
                try {
                    if(((String)result.get("msg")).equals("SUCCESS")){
                        writeToStatus("Success");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    writeToStatus("Failed");
                }
            }
        }).start();
    }

    public void refreshWhitelist(View v){
        JSONObject subJson = new JSONObject();
        JSONObject body = new JSONObject();
        try {
            subJson.put("studentID",name);
            subJson.put("password",password);
            body.put("mode","GET_WHITELIST");
            body.put("info",subJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }



        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject result = requests.request(url,body);
                try {
                    if(((String)result.get("msg")).equals("SUCCESS")){
                        writeToeditor((String) result.get("data"));
                    } else {
                        writeToStatus("failed");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    writeToStatus("Unable To Communicate to server");
                }
            }
        }).start();
    }
}