package com.vitap.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class options extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
    }

    public void jumpToUpdateWhitelist(View v){
        startActivity(new Intent(this,update_whitelist.class));
    }

    public void jumpToUpdateLocation(View v){
        startActivity(new Intent(this,update_location.class));
    }

    public void jumpToFriendLocation(View v){
        startActivity(new Intent(this,get_friend_location.class));
    }
}