package com.vitap.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.VoiceInteractor;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import static com.android.volley.Request.*;

public class Login_register extends AppCompatActivity {

    EditText idInp,passwordInp;
    TextView status;
    RequestQueue queue;
    String url;
    SharedPreferences sharedPref ;
    SharedPreferences.Editor editor ;
    String name,password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);

        sharedPref = getSharedPreferences("1",MODE_PRIVATE);
        editor = sharedPref.edit();


        idInp = findViewById(R.id.studentID);
        passwordInp = findViewById(R.id.password);
        queue =  Volley.newRequestQueue(this);
        status = findViewById(R.id.status2);
        url = sharedPref.getString("LINK","lol");
        Log.d("ola",url);
    }

    public void onLogin(View v) throws JSONException {
        name = idInp.getText().toString();
        password = passwordInp.getText().toString();
        JSONObject subBody = new JSONObject();
        subBody.put("studentID",name);
        subBody.put("password",password);
        JSONObject body = new JSONObject();
        body.put("mode","AUTH_USER");
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
                            Log.d("ola1","Succes");
                            onSuccess();
                        }
                        else{
                            writeToStatus("Wrong Credentials");
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

    public void onRegister(View v) throws JSONException {
        name = idInp.getText().toString();
        password = passwordInp.getText().toString();
        JSONObject subBody = new JSONObject();
        subBody.put("studentID",name);
        subBody.put("password",password);
        JSONObject body = new JSONObject();
        body.put("mode","CREATE_USER");
        body.put("info",subBody);

        //JsonObjectRequest request = new JsonObjectRequest(Method.GET,)
        Log.d("ola1",url);

        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject result = requests.request(url,body);
                if(result!=null){
                    try {
                        if(((String)result.get("msg")).equals("ADD_USER_SUCCESS")){
                            Log.d("ola1","");
                            onSuccess();
                        }
                        else{
                            writeToStatus("User Already Exists");
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

    public void onSuccess(){

        Log.d("ola1","success");
        editor.putString("studentID",name);
        editor.putString("password",password);
        editor.apply();
        writeToStatus("Login Success");
        startActivity(new Intent(this,options.class));
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