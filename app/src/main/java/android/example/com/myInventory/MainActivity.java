package android.example.com.myInventory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.example.com.myInventory.data.InventContract;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    InventCursorAdapter mCursorAdapter;
    private static final int mLOADER = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
        ListView petListView = findViewById(R.id.list);
        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        petListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of pet data in the Cursor.
        // There is no pet data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new InventCursorAdapter(this, null);
        petListView.setAdapter(mCursorAdapter);
        // Setup the item click listener
        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);

                Uri currentUri = ContentUris.withAppendedId(InventContract.InventEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentUri);

                // Launch the {@link EditorActivity} to display the data for the current pet.
                startActivity(intent);
            }
        });

        // Kick off the loader
        LoaderManager.getInstance(this).initLoader(mLOADER, null, this);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all:
                showDeleteConfirmationDialog();
                return true;
            case R.id.action_add_raw_data:
                Intent intent = new Intent(MainActivity.this, RawDataActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_about:
                startActivity( new Intent(MainActivity.this, AboutActivity2.class));
        }
        return super.onOptionsItemSelected(item);
    }
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteAllInvents();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = {
                InventContract.InventEntry._ID,
                InventContract.InventEntry.COLUMN_NAME,
                InventContract.InventEntry.COLUMN_DATE,
                InventContract.InventEntry.COLUMN_TOTAL};
        return new androidx.loader.content.CursorLoader(this,InventContract.InventEntry.CONTENT_URI,projection,null,null,null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
    /**
     * Helper method to insert hardcoded pet data into the database. For debugging purposes only.
     */
    /*
    private void insertInvent() {
        // Create a ContentValues object where column names are the keys,
        // and Toto's pet attributes are the values.
        ContentValues values = new ContentValues();
        ArrayList<goods> mGoods = new ArrayList<goods>();
        mGoods.add(new goods("水泥",1.5f,1.5f));
        mGoods.add(new goods("砖",2f,1.0f));
        String Goods = goods.toJson(mGoods);
        values.put(InventContract.InventEntry.COLUMN_NAME, "张三");
        values.put(InventContract.InventEntry.COLUMN_DATE, "2020-08-08");
        values.put(InventContract.InventEntry.COLUMN_NUMBER, 2);
        values.put(InventContract.InventEntry.COLUMN_TOTAL, 4.25);
        values.put(InventContract.InventEntry.COLUMN_GOODS,Goods);
        Log.e("InsertDunmmyData",Goods);
        Log.e("InsertDunmmyData",goods.toGoods(Goods).toString());

        // Insert a new row for Toto into the provider using the ContentResolver.
        // Use the {@link PetEntry#CONTENT_URI} to indicate that we want to insert
        // into the pets database table.
        // Receive the new content URI that will allow us to access Toto's data in the future.
        Uri newUri = getContentResolver().insert(InventContract.InventEntry.CONTENT_URI, values);
    }
    */
    /**
     * Helper method to delete all pets in the database.
     */
    private void deleteAllInvents() {
        getContentResolver().delete(InventContract.InventEntry.CONTENT_URI, null, null);
        getContentResolver().delete(InventContract.InventEntry.BILL_URI,null,null);
    }
}
