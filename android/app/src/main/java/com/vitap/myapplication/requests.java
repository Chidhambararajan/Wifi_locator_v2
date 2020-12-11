package com.vitap.myapplication;

import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class requests {


    public static JSONObject request(String url, JSONObject json){

        try {
            URL Url = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) Url.openConnection();
            connection.setRequestMethod("POST");
            OutputStream os = connection.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os,"UTF-8");
            osw.write(json.toString());
            osw.flush();
            osw.close();
            os.close();
            connection.connect();
            String result;
            BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            int result2 = bis.read();
            while(result2 != -1) {
                buf.write((byte) result2);
                result2 = bis.read();
            }
            result = buf.toString();
            return new JSONObject(result);
        } catch (MalformedURLException e) {
            Log.d("error","malformedUrl");
            return null;
        } catch (IOException e) {
            Log.d("error","IOException");
            e.printStackTrace();
            return null;
        } catch (JSONException e) {
            Log.d("error","JsonException");
            return null;
        }
    }
}
