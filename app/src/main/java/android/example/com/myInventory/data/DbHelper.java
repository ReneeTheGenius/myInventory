package android.example.com.myInventory.data;
import android.content.Context;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = DbHelper.class.getSimpleName();

    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "dock.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link DbHelper}.
     *
     * @param context of the app
     */
    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the pets table
        String SQL_CREATE_INVENT_TABLE = "CREATE TABLE " + InventContract.InventEntry.INVENT_TABLE_NAME + " ("
                + InventContract.InventEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventContract.InventEntry.COLUMN_NUMBER + " INTEGER NOT NULL, "
                + InventContract.InventEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + InventContract.InventEntry.COLUMN_DATE + " TEXT NOT NULL, "
                + InventContract.InventEntry.COLUMN_GOODS + " TEXT NOT NULL, "
                + InventContract.InventEntry.COLUMN_TOTAL + " REAL NOT NULL );";
        String SQL_CREATE_BILL_TABLE = "CREATE TABLE " + InventContract.InventEntry.BILLS_TABLE_NAME + " ("
                + InventContract.InventEntry.COLUMN_NAME + " TEXT PRIMARY KEY, "
                + InventContract.InventEntry.COLUMN_BILLS + " REAL NOT NULL );";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_INVENT_TABLE);
        db.execSQL(SQL_CREATE_BILL_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}
