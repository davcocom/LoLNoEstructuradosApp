package com.example.davidcocom.noestructuradosapp;

import android.os.AsyncTask;
import android.view.View;

import com.rey.material.widget.ProgressView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by David Cocom on 11/05/2016.
 */
public class JSONTask extends AsyncTask<String, Integer, String> {

    @Override
    protected String doInBackground(String... params) {
        String json = getJSON(params[0]);
        return json;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }

    public String getJSON(String jsonURL) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(jsonURL);
            connection = (HttpURLConnection) url.openConnection();
            // connection.setRequestMethod("GET");
            connection.connect();

            InputStream stream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stream));
            StringBuffer buffer = new StringBuffer();

            String line = "";
            while ((line = reader.readLine())!= null){
                buffer.append(line);
            }

            return buffer.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null){
                connection.disconnect();
            }
            if (reader!= null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

}
