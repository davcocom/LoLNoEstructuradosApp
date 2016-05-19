package com.example.davidcocom.noestructuradosapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dexafree.materialList.card.Card;
import com.dexafree.materialList.card.CardProvider;
import com.dexafree.materialList.card.OnActionClickListener;
import com.dexafree.materialList.card.action.TextViewAction;
import com.dexafree.materialList.card.action.WelcomeButtonAction;
import com.dexafree.materialList.view.MaterialListView;
import com.rey.material.widget.ProgressView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class ChampionsList extends AppCompatActivity {

    private ProgressView loadingCards;
    private String userId;
    private String region;
    private MaterialListView mListView;
    private static final String API_KEY = "b008336d-6fe1-4b33-b2ec-6f4cb98844b1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_champions_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Intent intent = getIntent();
        userId = intent.getStringExtra("id");
        region = intent.getStringExtra("region");

        loadingCards = (ProgressView) findViewById(R.id.progress_view_cards);

        setupCards();
        analyzeJSON(getJSONContent(getUrlRequest()));
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
                    .setDrawable(new ChampionImageTask().execute(url).get())
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
            Toast.makeText(this, "No existe información para ese usuario", Toast.LENGTH_SHORT).show();
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

    private void analyzeJSON(String content) {
        try {
            JSONObject jsonObject = new JSONObject(content);
            JSONArray champions = jsonObject.getJSONArray("champions");

            // obtener tamaño y en base a eso un for para obtener los más jugados
            int numeroCampeonesJugados = champions.length();
            int[] numerosPartidasTotales = new int[numeroCampeonesJugados];
            int campeonesVerdaderosPorMostrar = 0;
            int campeonesPorOrdenar = 0;
            int[] topChampionIds;

            // si tiene al menos 5 aparte del champion_id=0
            if(numeroCampeonesJugados > 5){
                campeonesVerdaderosPorMostrar = 5;
                campeonesPorOrdenar = 6;
                topChampionIds = new int[campeonesVerdaderosPorMostrar + 1];
            } else {
                campeonesVerdaderosPorMostrar = numeroCampeonesJugados - 1;
                campeonesPorOrdenar = numeroCampeonesJugados;
                topChampionIds = new int[numeroCampeonesJugados];
            }

            for (int i = 0; i < numeroCampeonesJugados; i++) {
                JSONObject stats = champions.getJSONObject(i).getJSONObject("stats");
                String championId = champions.getJSONObject(i).getString("id");
                numerosPartidasTotales[i] = (int) stats.get("totalSessionsPlayed");
            }
            // hay que ordenar numeroPartidasTotales[]
            int max = 0, index;

            for (int j = 0; j < campeonesPorOrdenar; j++){
                max = numerosPartidasTotales[0];
                index = 0;
                for (int i = 1; i < numerosPartidasTotales.length; i++) {
                    if (max < numerosPartidasTotales[i]) {
                        max = numerosPartidasTotales[i];
                        index = i;
                    }
                }
                topChampionIds[j] = index; // <----
                Log.d("idcampeones", String.valueOf(index));
                numerosPartidasTotales[index] = Integer.MIN_VALUE;
            }

            for (int i = 0; i < campeonesVerdaderosPorMostrar; i++) {
                String cardContent = "";
                JSONObject stats = champions.getJSONObject(topChampionIds[i+1]).getJSONObject("stats");
                String championId = champions.getJSONObject(topChampionIds[i+1]).getString("id");
                String[] championData = getChampionData(championId);
                cardContent = "Juegos realizados: " + stats.get("totalSessionsPlayed").toString() +
                        "\n" + "Ganadas: " + stats.get("totalSessionsWon").toString() +
                        "\n" + "Perdidas: " + stats.get("totalSessionsLost").toString();
                addNewCard(championData[0], championData[1].toUpperCase(), cardContent);
            }
            mListView.smoothScrollToPosition(0);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            this.finish();
            e.printStackTrace();
            Toast.makeText(this,
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


}
