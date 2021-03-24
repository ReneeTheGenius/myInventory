package android.example.com.myInventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class InventContract {
    private InventContract() {}

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.myInventory";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.pets/pets/ is a valid path for
     * looking at pet data. content://com.example.android.pets/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String INVENTS = "invents";
    public static final String BILLS="bills";

    /**
     * Inner class that defines constant values for the pets database table.
     * Each entry in the table represents a single pet.
     */
    public static final class InventEntry implements BaseColumns {

        /** The content URI to access the pet data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, INVENTS);
        public static final Uri BILL_URI = Uri.withAppendedPath(BASE_CONTENT_URI,BILLS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of pets.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + INVENTS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single pet.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + INVENTS;

        public static final String BILL_ITEM_TYPE=ContentResolver.CURSOR_ITEM_BASE_TYPE+"/"+CONTENT_AUTHORITY+"/"+BILLS;
        public static final String BILL_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE+"/"+CONTENT_AUTHORITY+"/"+BILLS;
        public final static String INVENT_TABLE_NAME = "invents";
        public final static String BILLS_TABLE_NAME ="bills";
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_BILLS = "bills";
        public final static String COLUMN_NUMBER ="number";

        public final static String COLUMN_NAME = "name";

        public final static String COLUMN_TOTAL = "total";

        public final static String COLUMN_DATE = "date";
        public final static String COLUMN_GOODS = "goods";


    }
}
