package com.android.example.oleg;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RequestQueue requestQueue;
    private TextView textOut;
    private Button parseData;
    private Button loadDB;
    private Button prevData;
    private Button nextData;

    private ArrayList<String> data;
    private int iterator;

    public static String urlWA = "https://api.apixu.com/v1/forecast.json?key=7e87b02239054f0ca5862326180812&q=Omsk&days=";
    public static SQLiteDatabase db;
    public static int iteratorD = 1;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent;
        switch(id){
            case R.id.service_settings:
                intent = new Intent(this, DateActivity.class);
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        data = new ArrayList<String>();
        textOut = findViewById(R.id.textView);
        parseData = findViewById(R.id.parse);
        loadDB = findViewById(R.id.load);
        prevData = findViewById(R.id.previous);
        nextData = findViewById(R.id.next);

        requestQueue = Volley.newRequestQueue(this);

        db = getBaseContext().openOrCreateDatabase("weather.db", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS weather (city TEXT, date TEXT, maxtemperature REAL, mintemperature REAL, wind REAL, humidity REAL, condition TEXT)");



        parseData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = urlWA+iteratorD;
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    JSONObject location = response.getJSONObject("location");
                                    String city = location.getString("name");
                                    JSONObject forecast = response.getJSONObject("forecast");
                                    JSONArray forecastday = forecast.getJSONArray("forecastday");

                                    for (int i = 0; i < forecastday.length(); i++) {
                                        JSONObject oneday = forecastday.getJSONObject(i);

                                        String date = oneday.getString("date");
                                        JSONObject day = oneday.getJSONObject("day");
                                        double maxTemp = day.getDouble("maxtemp_c");
                                        double minTemp = day.getDouble("mintemp_c");
                                        double wind = day.getDouble("maxwind_kph");
                                        double humidity = day.getDouble("avghumidity");
                                        JSONObject condition = day.getJSONObject("condition");
                                        String text = condition.getString("text");

                                        db.execSQL("INSERT INTO weather VALUES ('" + city +
                                                                                "','" + date +
                                                                                "'," + maxTemp +
                                                                                "," + minTemp +
                                                                                "," + wind +
                                                                                "," + humidity +
                                                                                ",'" + text + "');");
                                    }
                                    textOut.setText("Data successfully parsed");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                            }
                        });
                requestQueue.add(request);
                IncrIter();
            }
        });

        loadDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor query = db.rawQuery("SELECT * FROM weather;", null);
                if(query.moveToFirst()){
                    do
                    {
                        String note = "";
                        String city = query.getString(0);
                        String date = query.getString(1);
                        double maxTemp = query.getDouble(2);
                        double minTemp = query.getDouble(3);
                        double wind = query.getDouble(4);
                        double humidity = query.getDouble(5);
                        String text = query.getString(6);
                        note += "City: " + city + "\n";
                        note += "Date: " + date + "\n";
                        note += "Max temperature: " + maxTemp + "\n";
                        note += "Min temperature: " + minTemp + "\n";
                        note += "Wind speed: " + wind + "\n";
                        note += "Humidity: " + humidity + "\n";
                        note += "Condition: " + text + "\n";
                        data.add(note);
                    }
                    while(query.moveToNext());
                }
                query.close();
                textOut.setText(data.get(0));
                iterator = 0;
            }
        });

        nextData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(iterator < data.size() - 1)
                {
                    iterator++;
                }
                else
                {
                    iterator = 0;
                }
                textOut.setText(data.get(iterator));
            }
        });

        prevData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(iterator > 0)
                {
                    iterator--;
                }
                else
                {
                    iterator = data.size()-1;
                }
                textOut.setText(data.get(iterator));
            }
        });
    }

    public static void IncrIter()
    {
        if(iteratorD == 7)
        {
            iteratorD = 1;
        }
        else
        {
            iteratorD++;
        }
    }
}
