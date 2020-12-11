package com.vitap.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class get_friend_location extends AppCompatActivity {
    TextView status;
    EditText friendIdView;
    SharedPreferences sharedPref ;
    String name,password,url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_friend_location);
        friendIdView = findViewById(R.id.friendID);
        status = findViewById(R.id.textView4);
        sharedPref = getSharedPreferences("1",MODE_PRIVATE);
        name = sharedPref.getString("studentID","l");
        password = sharedPref.getString("password","l");
        url = sharedPref.getString("LINK","l");
    }

    public void onPress(View v) throws JSONException {
        String friendID = friendIdView.getText().toString().trim();
        if(friendID.equals("")||friendID.contains(" ")){
            status.setText("Enter a valid id");
            return;
        }
        JSONObject subBody = new JSONObject();
        subBody.put("studentID",name);
        subBody.put("password",password);
        subBody.put("otherStudentID",friendID);
        JSONObject body = new JSONObject();
        body.put("mode","GET_FRIEND_LOCATION");
        body.put("info",subBody);

        //JsonObjectRequest request = new JsonObjectRequest(Method.GET,)
        Log.d("ola1",url);

        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject result = requests.request(url,body);
                if(result!=null){
                    try {
                        if(((String)result.get("msg")).equals("SUCCESS")){
                            Log.d("ola1","");
                            writeToStatus("Your friend is present at "+result.get("data"));
                        }
                        else{
                            writeToStatus("Access Denied , you are not present in user's whitelist");
                            Log.d("ola1","Oopsie");
                        }
                    } catch (JSONException e) {
                        writeToStatus("Unknown Error");
                    }
                } else {
                    writeToStatus("Not able to reach to server");
                }
            }
        }).start();

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