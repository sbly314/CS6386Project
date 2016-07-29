package com.utd.sysdproject.cs6386project;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.utd.sysdproject.cs6386project.data.SearchContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class QueryResultsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = QueryResultsFragment.class.getSimpleName();

    private QueryAdapter mQueryAdapter;
//    private ArrayAdapter<String> mQueryAdapter;

    private ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;
    private String mMediaName = null;
    private String mCategory = null;

    private static final String SELECTED_KEY = "selected_position";
    private static final String SEARCH_MEDIANAME = "search_medianame";
    private static final String SEARCH_CATEGORY = "search_category";

    private static final int QUERY_LOADER = 0;

    private static final String[] MEDIA_COLUMNS = {
            SearchContract.MediaEntry._ID,
            SearchContract.MediaEntry.COLUMN_MEDIANAME,
            SearchContract.MediaEntry.COLUMN_CATEGORY,
            SearchContract.MediaEntry.COLUMN_IP,
            SearchContract.MediaEntry.COLUMN_PORT
    };

    // These indices are tied to MEDIA_COLUMNS.  If MEDIA_COLUMNS changes, these
    // must change.
    static final int COL_ID = 0;
    static final int COL_MEDIANAME = 1;
    static final int COL_CATEGORY = 2;
    static final int COL_IP = 3;
    static final int COL_PORT = 4;

    private TextView mMediaNameView;
    private TextView mCategoryView;

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
        Log.d(LOG_TAG, "FUNCTION: QueryResultsFragment");
//        setHasOptionsMenu(true);
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "FUNCTION: onCreate");
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(LOG_TAG, "FUNCTION: onCreateOptionsMenu");
        inflater.inflate(R.menu.menu_query_results, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        Log.d(LOG_TAG, "FUNCTION: onOptionsItemSelected");

        int id = item.getItemId();
        if (id == R.id.action_search) {
            // Set mMediaName and mCategory to null; shouldn't need to do this, but just in case
            mMediaName = null;
            mCategory = null;

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

        Log.d(LOG_TAG, "FUNCTION: onCreateView");

        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null) {
            Log.d(LOG_TAG, "DEBUG: savedInstanceState != null");
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
            mMediaName = savedInstanceState.getString(SEARCH_MEDIANAME);
            mCategory = savedInstanceState.getString(SEARCH_CATEGORY);
        } else {

            Log.d(LOG_TAG, "DEBUG: savedInstanceState == null and now trying to read from Intent");

            // check if new search (as opposed to coming back from Video playing)
            if ( (mMediaName == null) || (mCategory == null) ) {
                Bundle extras = getActivity().getIntent().getExtras();

                try {
                    mMediaName = extras.getString("mediaName_string");
                    mCategory = extras.getString("category_string");

                    // Only need to query the database if this is the first time searching with this criteria
                    updateMedia(mMediaName, mCategory);
                } catch (NullPointerException e ) {
                    Log.d(LOG_TAG, "DEBUG: NullPointerException hit");
                }
            }

            Log.d(LOG_TAG, "Received from MainActivity... Media Name: " + mMediaName + " and Category: " + mCategory);
        }

        // The QueryAdapter will take data from a source and
        // use it to populate the ListView it's attached to.
        mQueryAdapter = new QueryAdapter(getActivity(), null, 0);


        final View rootView = inflater.inflate(R.layout.fragment_query_results, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        mListView = (ListView) rootView.findViewById(R.id.listview_media);
        mListView.setAdapter(mQueryAdapter);

        // Set onItemClickListener (when user wants to play)
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    // load video
                    // add to backStack when switching to fragment

                    if(cursor.getString(COL_IP).equals("null")) {
                        Toast.makeText(getContext(),"No Results Found - Search Again",Toast.LENGTH_LONG).show();
                    } else {
                        String text = cursor.getString(COL_IP) + ":" + cursor.getString(COL_PORT) + "/" + cursor.getString(COL_MEDIANAME);
                        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();

                        // store values
                        Bundle outState = new Bundle();

                        onSaveInstanceState(outState);


                        // Pass arguments to fragment
                        Bundle bundle = new Bundle();
                        bundle.putString("ip_string", cursor.getString(COL_IP));
                        bundle.putString("port_string", cursor.getString(COL_PORT));
                        bundle.putString("medianame_string", cursor.getString(COL_MEDIANAME));

                        // Intent(FirstScreen.this, SecondScreen.class)
                        Intent intent = new Intent(getActivity(), MediaPlayerActivity.class);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                }
                mPosition = position;
            }
        });

        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "FUNCTION: onActivityCreated");
        getLoaderManager().initLoader(QUERY_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }


    private void updateMedia(String mediaName, String category) {
        Log.d(LOG_TAG, "FUNCTION: updateMedia");
        FetchMediaTask mediaTask = new FetchMediaTask(getActivity());
        mediaTask.execute(mediaName, category);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(LOG_TAG, "FUNCTION: onSaveInstanceState");
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        if (mMediaName != null) {
            outState.putString(SEARCH_MEDIANAME, mMediaName);
        }
        if (mCategory != null) {
            outState.putString(SEARCH_CATEGORY, mCategory);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        Log.d(LOG_TAG, "FUNCTION: onCreateLoader");

        // Sort order:  Ascending, by name.
        String sortOrder = SearchContract.MediaEntry.COLUMN_MEDIANAME + " ASC";

        Uri mediaSearchUri = SearchContract.MediaEntry.buildSearchNoParams();

        return new CursorLoader(getActivity(),
                mediaSearchUri,
                MEDIA_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(LOG_TAG, "FUNCTION: onLoadFinished");
        mQueryAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mListView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(LOG_TAG, "FUNCTION: onLoaderReset");
        mQueryAdapter.swapCursor(null);
    }

}
