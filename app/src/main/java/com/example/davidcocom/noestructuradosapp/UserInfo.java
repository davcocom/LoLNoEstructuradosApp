package com.example.davidcocom.noestructuradosapp;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.dexafree.materialList.card.Card;
import com.dexafree.materialList.card.CardProvider;
import com.dexafree.materialList.card.OnActionClickListener;
import com.dexafree.materialList.card.action.TextViewAction;
import com.dexafree.materialList.card.action.WelcomeButtonAction;
import com.dexafree.materialList.view.MaterialListView;
import com.ramotion.foldingcell.FoldingCell;
import com.rey.material.widget.ProgressView;
import com.squareup.picasso.RequestCreator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class UserInfo extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ProgressView loadingCards;
    private String userId;
    private String region;
    private MaterialListView mListView;
    private static final String API_KEY = "b008336d-6fe1-4b33-b2ec-6f4cb98844b1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        Intent intent = getIntent();
        userId = intent.getStringExtra("id");
        region = intent.getStringExtra("region");

        loadingCards = (ProgressView) findViewById(R.id.progress_view_cards);

        setupCards();
        analizeJSON(getJSONContent(getUrlRequest()));
    }

    private void setupCards() {
        mListView = (MaterialListView) findViewById(R.id.material_listview1);
        Card card = new Card.Builder(this)
                .withProvider(new CardProvider())
                .setLayout(R.layout.material_welcome_card_layout)
                .setTitle("¡Bienvenido!")
                .setTitleColor(Color.WHITE)
                .setDescription("Revisa sus estadisticas y encuentra más información interesante sobre ellos.")
                .setDescriptionColor(Color.WHITE)
                .setSubtitle("Estos son tus 5 campeones más usados")
                .setSubtitleColor(Color.WHITE)
                .setBackgroundColor(Color.BLUE)
                .addAction(R.id.ok_button, new WelcomeButtonAction(this)
                        .setText("Okay!")
                        .setTextColor(Color.WHITE)
                        .setListener(new OnActionClickListener() {
                            @Override
                            public void onActionClicked(View view, Card card) {
                                card.dismiss();
                            }
                        }))
                .endConfig()
                .build();

        card.setDismissible(true);
        mListView.getAdapter().add(card);
    }

    private void addNewCard(final String championName, String championDesc, String content) {
        URL url = null;
        Card card = null;
        try {
            url = new URL("http://ddragon.leagueoflegends.com/cdn/img/champion/splash/" +
                    championName +
                    "_0.jpg");

            card = new Card.Builder(this)
                    .withProvider(new CardProvider())
                    .setLayout(R.layout.material_image_with_buttons_card)
                    .setTitle(championName)
                    .setTitleColor(Color.WHITE)
                    .setSubtitle(championDesc)
                    .setDescription(content)
                    .setDrawable(new ChampionImage().execute(url).get())
                    .addAction(R.id.left_text_button, new TextViewAction(this)
                            .setText("Ver Información")
                            .setTextResourceColor(R.color.black_button)
                            .setListener(new OnActionClickListener() {
                                @Override
                                public void onActionClicked(View view, Card card) {
                                    Log.d("ADDING", "CARD");

                                }
                            }))

                    .endConfig()
                    .build();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        mListView.getAdapter().add(card);
    }

    private String getJSONContent(String url) {
        String data = "";
        try {
            JSONTask task = new JSONTask();
            data = task.execute(url).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            this.finish();
            Toast.makeText(UserInfo.this, "No existe información para ese usuario", Toast.LENGTH_SHORT).show();
        }
        return data;
    }

    private String getUrlRequest() {
        String url = "https://" +
                region +
                ".api.pvp.net/api/lol/" +
                region +
                "/v1.3/stats/by-summoner/" +
                userId +
                "/ranked?api_key=" +
                API_KEY;

        return url;
    }

    private void analizeJSON(String content) {
        try {
            JSONObject jsonObject = new JSONObject(content);
            JSONArray champions = jsonObject.getJSONArray("champions");

            // obtener tamaño y en base a eso un for para obtener los más jugados
            int numeroCampeonesJugados = champions.length();
            int[] numerosPartidasTotales = new int[numeroCampeonesJugados];
            int[] top5ChampionIds = new int[6];
            for (int i = 0; i < numeroCampeonesJugados; i++) {
                JSONObject stats = champions.getJSONObject(i).getJSONObject("stats");
                String championId = champions.getJSONObject(i).getString("id");
                numerosPartidasTotales[i] = (int) stats.get("totalSessionsPlayed");
            }
            // hay que ordenar numeroPartidasTotales[]
            int max = 0, index;
            for (int j = 0; j < 6; j++){
                max = numerosPartidasTotales[0];
                index = 0;
                for (int i = 1; i < numerosPartidasTotales.length; i++) {
                    if (max < numerosPartidasTotales[i]) {
                        max = numerosPartidasTotales[i];
                        index = i;
                    }
                }
                top5ChampionIds[j] = index; // <----
                // Log.d("idcampeones", String.valueOf(index));
                numerosPartidasTotales[index] = Integer.MIN_VALUE;
            }

            for (int i = 0; i < 5; i++) {
                String cardContent = "";
                JSONObject stats = champions.getJSONObject(top5ChampionIds[i+1]).getJSONObject("stats");
                String championId = champions.getJSONObject(top5ChampionIds[i+1]).getString("id");
                String[] championData = getChampionData(championId);
                cardContent = "Juegos realizados: " + stats.get("totalSessionsPlayed").toString() +
                        "\n" + "Ganadas: " + stats.get("totalSessionsWon").toString() +
                        "\n" + "Perdidas: " + stats.get("totalSessionsLost").toString();
                addNewCard(championData[0], championData[1].toUpperCase(), cardContent);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            this.finish();
            e.printStackTrace();
            Toast.makeText(UserInfo.this,
                    "No se encontraron partidas clasificatorias de este jugador",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private String[] getChampionData(String championId) {
        String[] championData = new String[2];

        String url = "https://global.api.pvp.net/api/lol/static-data/" +
                region.toLowerCase() +
                "/v1.2/champion/" +
                championId +
                "?api_key=b008336d-6fe1-4b33-b2ec-6f4cb98844b1";

        JSONObject championInfo = null;
        try {
            championInfo = new JSONObject(getJSONContent(url));
            championData[0] = championInfo.get("name").toString();
            championData[1] = championInfo.get("title").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return championData;
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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.user_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}


class ChampionImage extends AsyncTask<URL, Integer, Drawable> {


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
