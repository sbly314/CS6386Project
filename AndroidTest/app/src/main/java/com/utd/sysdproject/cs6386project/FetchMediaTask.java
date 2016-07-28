package com.utd.sysdproject.cs6386project;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.utd.sysdproject.cs6386project.data.SearchContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Vector;

/**
 * Asynchronously get data from database
 */
public class FetchMediaTask extends AsyncTask<String, Void, Void> {
    private final String LOG_TAG = FetchMediaTask.class.getSimpleName();

    private final Context mContext;

    public FetchMediaTask(Context context) {
        Log.d(LOG_TAG, "FUNCTION: FetchMediaTask");
        mContext = context;
    }

    /**
     * Helper method to handle insertion of new data in the media database
     *
     * @param mediaName The name of the media file.
     * @param category The category of the media file.
     * @param ip The IP address of the media server.
     * @param port The port of the media server.
     * @return the row ID of the added entry
     */
    long addMedia(String mediaName, String category, String ip, String port) {
        long mediaId;

        Log.d(LOG_TAG, "FUNCTION: addMedia");

        // First, check if the media already exists in the db
        Cursor mediaCursor = mContext.getContentResolver().query(
                SearchContract.MediaEntry.CONTENT_URI,
                new String[]{SearchContract.MediaEntry._ID},
                SearchContract.MediaEntry.COLUMN_MEDIANAME + " = ?",
                new String[]{mediaName},
                null);

        if (mediaCursor.moveToFirst()) {
            int mediaIdIndex = mediaCursor.getColumnIndex(SearchContract.MediaEntry._ID);
            mediaId = mediaCursor.getLong(mediaIdIndex);
        } else {
            // Now that the content provider is set up, inserting rows of data is pretty simple.
            // First create a ContentValues object to hold the data you want to insert.
            ContentValues mediaValues = new ContentValues();

            // Then add the data, along with the corresponding name of the data type,
            // so the content provider knows what kind of value is being inserted.
            mediaValues.put(SearchContract.MediaEntry.COLUMN_MEDIANAME, mediaName);
            mediaValues.put(SearchContract.MediaEntry.COLUMN_CATEGORY, category);
            mediaValues.put(SearchContract.MediaEntry.COLUMN_IP, ip);
            mediaValues.put(SearchContract.MediaEntry.COLUMN_PORT, port);

            // Finally, insert location data into the database.
            Uri insertedUri = mContext.getContentResolver().insert(
                    SearchContract.MediaEntry.CONTENT_URI,
                    mediaValues
            );

            // The resulting URI contains the ID for the row.  Extract the mediaId from the Uri.
            mediaId = ContentUris.parseId(insertedUri);
        }

        mediaCursor.close();

        return mediaId;
    }

    /**
     * Take the String representing the complete media list in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private void getMediaDataFromJson(String mediaJsonStr) throws JSONException {
        // Now we have a String representing the complete media list in JSON Format.
        // Fortunately parsing is easy:  constructor takes the JSON string and converts it
        // into an Object hierarchy for us.

        Log.d(LOG_TAG, "FUNCTION: getMediaDataFromJson");

        // Delete data from databse if there is anything currently in there
        mContext.getContentResolver().delete(SearchContract.MediaEntry.CONTENT_URI, null, null);

        // These are the names of the JSON objects that need to be extracted.
        final String CONTROLLER_LIST = "medialist";

        final String CONTROLLER_MEDIANAME = "name";
        final String CONTROLLER_CATEGORY = "category";
        final String CONTROLLER_IP = "ip";
        final String CONTROLLER_PORT = "port";

        try {
            JSONObject mediaJson = new JSONObject(mediaJsonStr);

            String ip = mediaJson.getString(CONTROLLER_IP);
            String port = mediaJson.getString(CONTROLLER_PORT);

            JSONArray mediaArray = mediaJson.getJSONArray(CONTROLLER_LIST);

            // Insert the new weather information into the database
            Vector<ContentValues> cVVector = new Vector<ContentValues>(mediaArray.length());

            for(int i = 0; i < mediaArray.length(); i++) {
                // These are the values that will be collected
                String mediaName;
                String category;


                JSONObject mediaObject = mediaArray.getJSONObject(i);

                mediaName = mediaObject.getString(CONTROLLER_MEDIANAME);
                category = mediaObject.getString(CONTROLLER_CATEGORY);

                ContentValues mediaValues = new ContentValues();

                mediaValues.put(SearchContract.MediaEntry.COLUMN_MEDIANAME, mediaName);
                mediaValues.put(SearchContract.MediaEntry.COLUMN_CATEGORY, category);
                mediaValues.put(SearchContract.MediaEntry.COLUMN_IP, ip);
                mediaValues.put(SearchContract.MediaEntry.COLUMN_PORT, port);

                cVVector.add(mediaValues);
            }

            int inserted = 0;
            // add to database
            if ( cVVector.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                inserted = mContext.getContentResolver().bulkInsert(SearchContract.MediaEntry.CONTENT_URI, cvArray);
            }

            Log.d(LOG_TAG, "FetchMediaTask Complete. " + inserted + " Inserted");

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    // This is the task that queries the Controller
    @Override
    protected Void doInBackground(String... params) {
        Log.d(LOG_TAG, "FUNCTION: doInBackground");

        int DEBUG = 0;

        if (DEBUG == 0) {
            // Client Socket
            Socket clientSocket = null;

            DataInputStream is = null;
            PrintStream os = null;

            boolean closed = false;

            int portNumber = 2222;
            String host = "129.110.92.16"; // Controller (cs2.utdallas.edu)

            // If there's no mediaName and Category, there's nothing to look up.  Verify size of params.
            if (params.length != 2) {
                return null;
            }

            String mediaQuery = params[0];
            String categoryQuery = params[1];

            try {
                clientSocket = new Socket(host, portNumber);
                is = new DataInputStream(clientSocket.getInputStream());
                os = new PrintStream(clientSocket.getOutputStream());

                // Send Sync message
                // os.println("search");
                String search = "search";
                String media = mediaQuery + "/" + categoryQuery;
                search = search.concat(",");
                search = search.concat(media);
                os.println(search);

                Vector<String> resultsVector = new Vector<String>();

                String line = "";
                line = is.readLine();

                String temp[] = line.split(",");

                if (temp[0].equals("FIN")) {
                    Log.d(LOG_TAG, "DEBUG: FIN received");
                    String receivedLine = temp[1];
                    if (receivedLine.equals("ERR")) {
                        Log.d(LOG_TAG, "DEBUG: FIN,ERR received");

                        // display No Results message in database
                        String mediaJson = "{\"medialist\":[{\"name\":\"No Data Found\",\"category\":\"\",\"ip\":\"null\",\"port\":\"null\"}]}";
                        getMediaDataFromJson(mediaJson);
                    } else {
                        Log.d(LOG_TAG, "DEBUG: FIN, " + receivedLine + " received");
                        // parse and store results
                        int numberResults = Integer.parseInt(receivedLine);


                        // Gather results
                        for (int i=1; i<= numberResults; i++) {
                            line = is.readLine();
                            resultsVector.add(line);
                        }

                        // Compile JSON from Vector
                        String compileJSON = "";
                        Iterator iter = resultsVector.iterator();
                        while (iter.hasNext()) {
                            compileJSON = compileJSON + iter.next();
                            if(iter.hasNext()) {
                                compileJSON = compileJSON + ",";
                            }
                        }

                        Log.d(LOG_TAG, "DEBUG: compileJSON is " + compileJSON);

                        getMediaDataFromJson(compileJSON);
                    }
                }
            } catch (UnknownHostException e) {
                Log.e(LOG_TAG, "Unknown host " + host);
            } catch (IOException e) {
                Log.e(LOG_TAG, "IOEXCEPTION: Couldn't get I/O for connection to host " + host);
                Log.e(LOG_TAG, "IOEXCEPTION: " + e.getMessage());
            } catch (NullPointerException e) {
                Log.e(LOG_TAG, "NullPointerException from host " + host);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }




//            // These two need to be declared outside the try/catch
//            // so that they can be closed in the finally block.
//            HttpURLConnection urlConnection = null;
//            BufferedReader reader = null;
//
//            // Will contain the raw JSON response as a string.
//            String mediaJsonStr = null;
//
//            try {
//                // Construct the URL
//                final String BASE_URL = "http://10.28.34.150:5984/jsontest/mediadb/";
//                //            final String QUERY_PARAM = "q";
//                //            final String MEDIA_PARAM = "medianame";
//                //            final String CATEGORY_PARAM = "cat";
//                //
//                //            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
//                //                    .appendQueryParameter(MEDIA_PARAM, mediaQuery)
//                //                    .appendQueryParameter(CATEGORY_PARAM, categoryQuery)
//                //                    .build();
//                Uri builtUri = Uri.parse(BASE_URL).buildUpon().build();
//
//                URL url = new URL(builtUri.toString());
//
//                // Create the request to the Controller, and open the connection
//                urlConnection = (HttpURLConnection) url.openConnection();
//                urlConnection.setRequestMethod("GET");
//                urlConnection.connect();
//
//                // Read the input stream into a String
//                InputStream inputStream = urlConnection.getInputStream();
//                StringBuffer buffer = new StringBuffer();
//                if (inputStream == null) {
//                    // Nothing to do.
//                    return null;
//                }
//                reader = new BufferedReader(new InputStreamReader(inputStream));
//
//                String line;
//
//                while ((line = reader.readLine()) != null) {
//                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
//                    // But it does make debugging a *lot* easier if you print out the completed
//                    // buffer for debugging.
//                    buffer.append(line + "\n");
//                }
//
//                if (buffer.length() == 0) {
//                    // Stream was empty.  No point in parsing.
//                    return null;
//                }
//
//                Log.d(LOG_TAG, "DEBUG: JSON Buffer is: " + buffer.toString());
//
//                mediaJsonStr = buffer.toString();
//                getMediaDataFromJson(mediaJsonStr);
//            } catch (IOException e) {
//                Log.e(LOG_TAG, "Error ", e);
//                // If the code didn't successfully get the media data, there's no point in attempting
//                // to parse it.
//            } catch (JSONException e) {
//                Log.e(LOG_TAG, e.getMessage(), e);
//                e.printStackTrace();
//            } finally {
//                if (urlConnection != null) {
//                    urlConnection.disconnect();
//                }
//                if (reader != null) {
//                    try {
//                        reader.close();
//                    } catch (final IOException e) {
//                        Log.e(LOG_TAG, "Error closing stream", e);
//                    }
//                }
//            }
        } else {
            try {
                String mediaJsonStr = "{\"medialist\":[{\"name\":\"TestName\",\"category\":\"TestCategory\",\"ip\":\"10.1.2.1\",\"port\":\"3456\"}]}\n";
                getMediaDataFromJson(mediaJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "test error");
            }
        }
        return null;
    }
}
