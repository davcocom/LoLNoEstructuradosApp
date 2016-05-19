package com.example.davidcocom.noestructuradosapp;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by David Cocom on 19/05/2016.
 */
public class ChampionImageTask extends AsyncTask<URL, Integer, Drawable> {


    @Override
    protected Drawable doInBackground(URL... params) {
        return getDrawableFromUrl(params[0]);
    }

    public static Drawable getDrawableFromUrl(URL url) {
        try {
            InputStream is = url.openStream();
            Drawable d = Drawable.createFromStream(is, "src");
            return d;
        } catch (MalformedURLException e) {
            // e.printStackTrace();
        } catch (IOException e) {
            // e.printStackTrace();
        }
        return null;
    }

}