package com.example.max.hackernewsreader;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    static ArrayList<String> contents;
    ArrayList<String> titles;
    SQLiteDatabase newsDB;
    ArrayAdapter<String> adapter;
    TextView textView;
    ProgressBar progressBar;
    private Request.Priority priority = Request.Priority.HIGH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.listview);
        textView = (TextView) findViewById(R.id.noDataText);
        progressBar = (ProgressBar) findViewById(R.id.Progress);
        titles = new ArrayList<>();
        contents = new ArrayList<>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, titles);
        listView.setAdapter(adapter);

         newsDB = this.openOrCreateDatabase("NewsApp", MODE_PRIVATE, null);
        newsDB.execSQL("CREATE TABLE IF NOT EXISTS articles (id INTEGER PRIMARY KEY , title VARCHAR, content VARCHAR)");

        //newsDB.close();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(getApplicationContext(), NewsBody.class);
                intent.putExtra("content", contents.get(position));
                startActivity(intent);

            }
        });

        updateList();

    }




    public void getData(View view){

        textView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);

        String url= "https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                int endOfNews = 20;

                if (response.length()<20){
                    endOfNews = response.length();
                }

                    for (int i =0; i<endOfNews; i++){


                        try {

                            Log.i("Data", response.getString(i));

                            DownloadNews(response.getString(i));

                        } catch (JSONException e) {

                            e.printStackTrace();
                        }

                    }
                    adapter.notifyDataSetChanged();
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.i("Main Error", error.toString());

            }
        });

        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonArrayRequest);

        updateList();

    }


    public void DownloadNews( String id){
        String ItemUrl = "https://hacker-news.firebaseio.com/v0/item/" + id + ".json?print=pretty";

        JsonObjectRequest news = new JsonObjectRequest(ItemUrl,null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                    try {

                        if (!response.isNull("title") && !response.isNull("url")){

                        String title = response.getString("title");


                        String url = response.getString("url");

                            getContent(url, title);

                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }



            }


        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.i("News Error", error.toString());

            }
        });

        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(news);

    }


    public void getContent(String url, final String title){

        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                //Log.i("Content", response);

                contents.add(response);
                titles.add(title);

                newsDB.execSQL("DELETE FROM articles");

                String sql = "INSERT INTO articles (title, content) VALUES (? , ?)";

                SQLiteStatement sqLiteStatement = newsDB.compileStatement(sql);
                sqLiteStatement.bindString(1, title);
                sqLiteStatement.bindString(2, response);
                sqLiteStatement.execute();

                adapter.notifyDataSetChanged();



            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.i("Content Error", error.toString());

            }
        }){
            @Override
            public Priority getPriority() {

                return priority;
            }
        };


        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);


    }


    public void updateList(){
        adapter.notifyDataSetChanged();

        progressBar.setVisibility(View.GONE);
        textView.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);


        Cursor c = newsDB.rawQuery("SELECT * FROM articles", null);

        int titleIndex = c.getColumnIndex("title");
        int contentIndex = c.getColumnIndex("content");

        if (c.moveToFirst()) {

            titles.clear();
            contents.clear();

            do {
                String newTitle = c.getString(titleIndex);
                String newContent = c.getString(contentIndex);

                titles.add(newTitle);
                contents.add(newContent);

            } while (c.moveToNext());

        }

        c.close();

        adapter.notifyDataSetChanged();


    }


}
