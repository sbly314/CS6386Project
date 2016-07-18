package com.utd.sysdproject.cs6386project.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines contract for searching based on mediaName and category
 */
public class SearchContract {
    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.utd.sysdproject.cs6386project";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH = "search";

    public static final class MediaEntry implements BaseColumns {
        // Build path
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH;

        public static final String TABLE_NAME = "media";

        // No need to implement unique key for each row; BaseColumns takes care of this

        // Media Name
        public static final String COLUMN_MEDIANAME = "medianame";

        // Category
        public static final String COLUMN_CATEGORY = "category";

        // IP
        public static final String COLUMN_IP = "ip";

        // Port
        public static final String COLUMN_PORT = "port";

        public static Uri buildSearchUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildSearchUriWithParameters(
                String mediaName, String category, String IP, String port) {
            return CONTENT_URI.buildUpon().appendQueryParameter(COLUMN_MEDIANAME, mediaName)
                    .appendQueryParameter(COLUMN_CATEGORY, category)
                    .appendQueryParameter(COLUMN_IP, IP)
                    .appendQueryParameter(COLUMN_PORT, port).build();
        }

        public static String getMediaNameFromUri(Uri uri) {
            String mediaName = uri.getQueryParameter(COLUMN_MEDIANAME);
            if (mediaName != null && mediaName.length() > 0) {
                return mediaName;
            } else {
                return null;
            }
        }

        public static String getCategoryFromUri(Uri uri) {
            String category = uri.getQueryParameter(COLUMN_CATEGORY);
            if (category != null && category.length() > 0) {
                return category;
            } else {
                return null;
            }
        }

        public static String getIPFromUri(Uri uri) {
            String ip = uri.getQueryParameter(COLUMN_IP);
            if (ip != null && ip.length() > 0) {
                return ip;
            } else {
                return null;
            }
        }

        public static String getPortFromUri(Uri uri) {
            String port = uri.getQueryParameter(COLUMN_PORT);
            if (port != null && port.length() > 0) {
                return port;
            } else {
                return null;
            }
        }
    }
}
