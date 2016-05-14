package com.example.davidcocom.noestructuradosapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.rey.material.widget.ProgressView;
import com.rey.material.widget.Spinner;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity {

    private Spinner spinner;
    private TextView gamerTag;
    private ProgressView progressView;
    static String API_KEY = "b008336d-6fe1-4b33-b2ec-6f4cb98844b1";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupSpinner();
        progressView = (ProgressView) findViewById(R.id.progressView);
        gamerTag = (TextView) findViewById(R.id.gamerTag);
    }

    public void openNewActivity(View view) {
        if (isStatusValid(view)) {
            Intent championList = new Intent(this, UserInfo.class);
            championList.putExtra("id",getIdFromJSON(getJSON(getCompleteUrl())));
            championList.putExtra("region", spinner.getSelectedItem().toString());
            startActivity(championList);
        }
    }

    private boolean isStatusValid(View view) {
        if (gamerTag.getText().toString().isEmpty()) {
            Snackbar.make(view, "Llena el campo de texto con tu nombre de usuario.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        if (!isNetworkAvailable(getApplicationContext())) {
            Snackbar.make(view, "Asegurate de tener una conexión a internet.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        return !gamerTag.getText().toString().isEmpty() && isNetworkAvailable(getApplicationContext());
    }

    private String getCompleteUrl() {
        String region = spinner.getSelectedItem().toString();
        String gamerTag = this.gamerTag.getText().toString();
        String url = "https://" +
                region
                + ".api.pvp.net/api/lol/" +
                region
                + "/v1.4/summoner/by-name/" +
                gamerTag
                + "?api_key=" +
                API_KEY;
        return url;
    }

    private void setupSpinner() {
        ArrayList<String> regions = new ArrayList<>();
        regions.add("LAN");
        regions.add("BR");
        regions.add("EUNE");
        regions.add("EUW");
        regions.add("JP");
        regions.add("KR");
        regions.add("NA");
        regions.add("OCE");
        regions.add("TR");
        regions.add("RU");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, regions);
        spinner = (Spinner) findViewById(R.id.spinner_regions);
        spinner.setAdapter(dataAdapter);
    }

    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public String getJSON(String url) {
        progressView.start();
        String response = null;
        try {
            JSONTask task = new JSONTask();
            response = task.execute(url).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        progressView.stop();
        return response;
    }

    private String getIdFromJSON(String data){
        JSONObject json = null;
        String id = "";
        String gamerTag = this.gamerTag.getText().toString().toLowerCase();
        try {
            json = new JSONObject(data);
            id = json.getJSONObject(gamerTag).get("id").toString();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return id;
    }

    private void showDialog(String data) {
        android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(this);
        alert.setTitle("Contenido:");
        alert.setMessage(data);

        alert.setPositiveButton("Copiar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        alert.setNegativeButton("Salir",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });

        alert.show();
    }



}
