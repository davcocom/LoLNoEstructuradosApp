package com.example.davidcocom.noestructuradosapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.rey.material.widget.EditText;
import com.rey.material.widget.ProgressView;
import com.rey.material.widget.Spinner;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
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
        progressView.setProgress(0);
        gamerTag = (TextView) findViewById(R.id.gamerTag);
    }

    public void openNewActivity(View view) {
        if (isStatusValid(view)) {
            Intent championList = new Intent(this, ChampionsList.class);
            championList.putExtra("id", getIdFromJSON(getJSON(getCompleteUrl())));
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
        String gamerTag = (this.gamerTag.getText().toString());

        String url = "https://" +
                region
                + ".api.pvp.net/api/lol/" +
                region
                + "/v1.4/summoner/by-name/" +
                gamerTag
                + "?api_key=" +
                API_KEY;

        return replaceSpaces(url);
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
        String response = null;
        try {
            response = new GetJson().execute(url).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return response;
    }

    private String getIdFromJSON(String data) {
        JSONObject json = null;
        String id = "";
        String gamerTag = this.gamerTag.getText().toString().toLowerCase().replace(" ","");
        try {
            json = new JSONObject(data);
            id = json.getJSONObject(gamerTag).get("id").toString();
        } catch (JSONException e) {
            Toast.makeText(MainActivity.this, "Usuario no válido", Toast.LENGTH_SHORT).show();
        } catch (NullPointerException e){
            Toast.makeText(MainActivity.this, "No existe tal usuario", Toast.LENGTH_SHORT).show();
        }
        return id;
    }

    public String replaceSpaces(String string){
        string = string.replace(" ", "%20");
        Log.d("tagname",string);
        return string;
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


    class GetJson extends AsyncTask<String, Integer, String> {

        int myProgress;

        @Override
        protected String doInBackground(String... params) {
            String json = getJSON(params[0]);
            return json;
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
                while ((line = reader.readLine()) != null) {
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
                if (connection != null) {
                    connection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            myProgress = 0;
            progressView.start();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {

        }

        @Override
        protected void onPostExecute(String s) {
            progressView.stop();
        }
    }

}


