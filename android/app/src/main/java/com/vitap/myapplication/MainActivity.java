package com.vitap.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceManager;
import android.service.autofill.TextValueSanitizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class MainActivity extends AppCompatActivity {

    EditText link ;
    TextView status ;
    Button ok;
    RequestQueue queue;
    SharedPreferences sharedPref ;
    SharedPreferences.Editor editor ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref = getSharedPreferences("1",MODE_PRIVATE);

        /*if(!sharedPref.contains("LINK")){
            jump();
            return;
        }*/

        editor = sharedPref.edit();

        queue =  Volley.newRequestQueue(this);
        link=findViewById(R.id.link);
        ok = findViewById(R.id.button);
        status = findViewById(R.id.status);
        status.setText("Enter the link");
    }

    public void jump(){
        Intent jumper = new Intent(this,Login_register.class);
        startActivity(jumper);
    }

    public void onPress(View v){
        String Link = link.getText().toString();
        if (!Link.equals("")){
            StringRequest stringRequest = new StringRequest(Request.Method.GET, Link,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // Display the first 500 characters of the response string.
                            if (response.contains("-1")){
                                editor.putString("LINK",Link);
                                editor.apply();
                                Log.d("ola",sharedPref.getString("LINK","hello"));
                                Log.d("q","ok");
                                status.setText("Link active");
                                jump();
                            }
                            else {
                                status.setText("Link not working");
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            status.setText("That didn't work!");
                        }
                    }
            );

// Add the request to the RequestQueue.
            queue.add(stringRequest);

        } else {
            status.setText("type proper link");
        }

    }

}