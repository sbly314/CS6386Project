package com.utd.sysdproject.cs6386project;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

public class QueryResults extends AppCompatActivity {
    private final String LOG_TAG = QueryResults.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "FUNCTION: onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_results);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
