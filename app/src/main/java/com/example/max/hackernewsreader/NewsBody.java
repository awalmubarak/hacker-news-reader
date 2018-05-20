package com.example.max.hackernewsreader;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class NewsBody extends AppCompatActivity {
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_body);

        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        Intent intent = getIntent();

        String webContent = "<html><body><h1>No Content Available</h1><p>Sorry but your code messed up...<strong>BIG TIME</strong></p></body></html>";

        if (intent.getStringExtra("content") != null){

            webContent = intent.getStringExtra("content");

        }

        webView.loadData(webContent,"text/html", "UTF-8");


    }
}
