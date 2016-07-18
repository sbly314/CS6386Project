package com.utd.sysdproject.cs6386project;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class QueryResultsFragment extends Fragment {

    private final String LOG_TAG = QueryResultsFragment.class.getSimpleName();

//    private QueryAdapter mQueryAdapter;
    private ArrayAdapter<String> mQueryAdapter;

    private ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;

    private static final String SELECTED_KEY = "selected_position";

    private static final int QUERY_LOADER = 0;

//    private static final String[] MEDIA_COLUMNS = {
//            MediaContract.MediaEntry.COLUMN_MEDIA_NAME,
//            MediaContract.MediaEntry.COLUMN_IP,
//            MediaContract.MediaEntry.COLUMN_PORT
//    };

    // These indices are tied to MEDIA_COLUMNS.  If MEDIA_COLUMNS changes, these
    // must change.
    static final int COL_MEDIA_NAME = 0;
    static final int COL_IP = 1;
    static final int COL_PORT = 2;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }


    public QueryResultsFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_query_results, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();
        if (id == R.id.action_search) {
            // Create explicit intent to switch back to Main screen
            Intent intent = new Intent(this.getActivity(), MainActivity.class);
            this.startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle extras = getActivity().getIntent().getExtras();
        String mediaName = extras.getString("mediaName_string");
        String categoryName = extras.getString("category_string");

        Log.d(LOG_TAG, "Received from MainActivity... Media Name: " + mediaName + " and Category: " + categoryName);


        // Sample Data
        String[] data = {
                "Friends - Drama",
                "How I Met Your Mother - Comedy",
                "Scream - Horror",
                "Star Wars - Science Fiction",
                "The Notebook - Romance",
                "Antman - Science Fiction",
                "Avengers - Science Fiction",
                "Avengers: Age of Ultron - Science Fiction",
                "Captain America - Science Fiction",
                "The Empire Strikes Back - Science Fiction",
                "Return of the Jedi - Science Fiction",
                "The Phantom Menace - Science Fiction",
                "Attack of the Clones - Science Fiction",
                "Revenge of the Sith - Science Fiction",
                "Star Trek - Science Fiction",
                "James Bond - Drama",
                "The Hulk - Science Fiction",
                "Thor - Science Fiction",
                "Finding Nemo - Comedy",
                "Finding Dory - Comedy",
                "Toy Story - Comedy",
                "Toy Story 2 - Comedy",
                "Toy Story 3 - Comedy",
                "Cars - Comedy",
                "Cars 2 - Comedy"
        };
        List<String> searchResults = new ArrayList<String>(Arrays.asList(data));



//        // The QueryAdapter will take data from a source and
//        // use it to populate the ListView it's attached to.
//        mQueryAdapter = new QueryAdapter(getActivity(), null, 0);

        // populate fake data
        mQueryAdapter = new ArrayAdapter<String>(
                getActivity(), // The current context (this activity)
                R.layout.list_item_searchresults, // The name of the layout ID
                R.id.list_item_medianame_textview, // The ID of the textview to populate
                searchResults // fake data
        );

        View rootView = inflater.inflate(R.layout.fragment_query_results, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        mListView = (ListView) rootView.findViewById(R.id.listview_media);
        mListView.setAdapter(mQueryAdapter);

//        // Set onItemClickListener (when user wants to play)
//        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
//                // CursorAdapter returns a cursor at the correct position for getItem(), or null
//                // if it cannot seek to that position.
//                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
//                if (cursor != null) {
//                    // load video
//
//                }
//                mPosition = position;
//            }
//        });


        return rootView;
    }
}
