package com.utd.sysdproject.cs6386project;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by Stephen on 7/17/2016.
 */
public class QueryAdapter extends CursorAdapter {
    private final String LOG_TAG = QueryAdapter.class.getSimpleName();
    private static final int VIEW_TYPE_COUNT = 1;

    /**
     * Cache of the children views for a media list item.
     */
    public static class ViewHolder {
        public final TextView mediaNameView;
        public final TextView categoryView;

        public ViewHolder(View view) {
            mediaNameView = (TextView) view.findViewById(R.id.list_item_medianame_textview);
            categoryView = (TextView) view.findViewById(R.id.list_item_category_textview);
        }
    }


    public QueryAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        Log.d(LOG_TAG, "FUNCTION: newView");
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = R.layout.list_item_searchresults;

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Log.d(LOG_TAG, "FUNCTION: bindView");
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String mediaName = cursor.getString(QueryResultsFragment.COL_MEDIANAME);
        viewHolder.mediaNameView.setText(mediaName);

        String category = cursor.getString(QueryResultsFragment.COL_CATEGORY);
        viewHolder.categoryView.setText(category);
    }


    @Override
    public int getViewTypeCount() {
        Log.d(LOG_TAG, "FUNCTION: getViewTypeCount");
        return VIEW_TYPE_COUNT;
    }

}
