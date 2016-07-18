package com.utd.sysdproject.cs6386project;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by Stephen on 7/17/2016.
 */
public class QueryAdapter extends CursorAdapter {
    private static final int VIEW_TYPE_COUNT = 1;

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public final TextView mediaNameView;

        public ViewHolder(View view) {
            mediaNameView = (TextView) view.findViewById(R.id.list_item_medianame_textview);
        }
    }


    public QueryAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = R.layout.list_item_searchresults;

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String mediaName = cursor.getString(0);
        viewHolder.mediaNameView.setText(mediaName);
    }


    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

}
