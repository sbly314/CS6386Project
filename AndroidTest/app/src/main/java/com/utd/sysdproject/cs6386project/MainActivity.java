package com.utd.sysdproject.cs6386project;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Populate spinner with list of media categories from media_categories.xml */
        Spinner spinner = (Spinner) findViewById(R.id.selected_media_category_id);
        // Create an ArrayAdapter using media_categories.xml and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.categories_array,android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        //MenuItemCompat.setActionProvider(menu.findItem(R.id.action_search), searchPage());
/*
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
*/

        return true;
    }


    public void searchSubmitted(View view) {
        // Get Media Name entered
        EditText mediaNameText = (EditText)findViewById(R.id.search_media_title_id);
        String mediaName = mediaNameText.getText().toString();

        // Get Selected Spinner Item
        Spinner spinnerValue = (Spinner)findViewById(R.id.selected_media_category_id);
        String categoryName = spinnerValue.getSelectedItem().toString();

        Log.d(LOG_TAG, "Searching on Media Name: " + mediaName + " and Category: " + categoryName);




        // Pass arguments to fragment
        Bundle bundle = new Bundle();
        bundle.putString("mediaName_string", mediaName);
        bundle.putString("category_string", categoryName);

        // Intent(FirstScreen.this, SecondScreen.class)
        Intent intent = new Intent(this, QueryResults.class);
        intent.putExtras(bundle);
        startActivity(intent);


//
//        QueryResultsFragment fragobj = new QueryResultsFragment();
//        fragobj.setArguments(bundle);
//
//        getSupportFragmentManager().beginTransaction().replace(R.id.query_fragment, fragobj).commit();

    }
}
