package android.example.com.myInventory;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.example.com.myInventory.data.InventContract;
import android.example.com.myInventory.data.myContentUris;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import java.math.BigDecimal;

public class RawDataActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    private Spinner mSpinner;
    private float initialBill;
    private int mMethod;
    private Boolean mHasChanged;
    private EditText mNameEdit;
    private EditText mBillEdit;
    private TextView mBillText;
    private final static int OVERRIDE=0;
    private final static int INCREASE = 1;
    private final static int DECREASE =2;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raw_data);
        setTitle("历史欠款");

        initialBill=0f;
        mHasChanged=false;
        mSpinner = findViewById(R.id.spinner);
        mBillEdit=findViewById(R.id.edit_bill);
        mBillText=findViewById(R.id.bill);
        mNameEdit=findViewById(R.id.edit_name);
        setupSpinner();
        View.OnTouchListener mTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mHasChanged = true;
                return false;
            }
        };
        mNameEdit.setOnTouchListener(mTouchListener);
        mBillEdit.setOnTouchListener(mTouchListener);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.raw_data_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            case R.id.action_save:
                saveBill();
                return true;
            case android.R.id.home:
                if (!mHasChanged) {
                    NavUtils.navigateUpFromSameTask(RawDataActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, navigate to parent activity.
                        NavUtils.navigateUpFromSameTask(RawDataActivity.this);
                    }
                };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private Uri getUri(String name){
        return myContentUris.withAppendedName(InventContract.InventEntry.BILL_URI,name);
    }
    private void queryBill(){
        String name = mNameEdit.getText().toString().trim();
        if(TextUtils.isEmpty(name)){
            Toast.makeText(this,R.string.checkName,Toast.LENGTH_SHORT).show();
            return;
        }
        Cursor cursor = getContentResolver().query(getUri(name),null,null,null);
        if(cursor==null||cursor.getCount()<1 )return;
        if(cursor.moveToFirst()){
            int billIndex = cursor.getColumnIndex(InventContract.InventEntry.COLUMN_BILLS);
            initialBill = cursor.getFloat(billIndex);
        }
    }
    private void saveBill() {
        queryBill();
        String name = mNameEdit.getText().toString().trim();
        if(TextUtils.isEmpty(name)){
            Toast.makeText(this,R.string.checkName,Toast.LENGTH_SHORT).show();
            return;
        }
        String newBillString = mBillEdit.getText().toString().trim();
        if(TextUtils.isEmpty(newBillString)){
            Toast.makeText(this,R.string.checkBill,Toast.LENGTH_SHORT).show();
            return;
        }
        Float currentBill = Float.parseFloat(newBillString);
        if (Math.abs(currentBill)<0.00001f){
            Toast.makeText(this,R.string.zeroBill,Toast.LENGTH_SHORT).show();
            return;
        }
        Float newBill;
        ContentValues values = new ContentValues();
        int rowsAffected=-1;
        switch (mMethod){
            case INCREASE:
                newBill = initialBill+currentBill;

                values.put(InventContract.InventEntry.COLUMN_BILLS,newBill);
                rowsAffected = insertOrUpdate(name,values);
                break;
            case DECREASE:
                newBill = initialBill-currentBill;
                if (newBill<0){
                    Toast.makeText(this,R.string.negativeBill,Toast.LENGTH_SHORT).show();
                    return;
                }
                values.put(InventContract.InventEntry.COLUMN_BILLS,newBill);
                try{
                    rowsAffected = getContentResolver().update(getUri(name),values,null,null);
                }catch (IllegalArgumentException e){
                    rowsAffected = getContentResolver().delete(getUri(name),null,null);
                }

                break;
            case OVERRIDE:
                newBill=currentBill;
                values.put(InventContract.InventEntry.COLUMN_BILLS,newBill);
                rowsAffected=insertOrUpdate(name,values);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + mMethod);
        }
        Log.i("SaveBill","newBill ="+newBill);
        Log.i("SaveBill","rowsAffected="+rowsAffected);
        if(rowsAffected>0)Toast.makeText(this,R.string.changeBillSucceed,Toast.LENGTH_SHORT).show();
        finish();
    }
private int insertOrUpdate(String name,ContentValues values){
    if(Math.abs(initialBill)<0.000001f){
        values.put(InventContract.InventEntry.COLUMN_NAME,name);
        Uri uri = getContentResolver().insert(InventContract.InventEntry.BILL_URI,values);
        if(uri==null){
            Toast.makeText(this,R.string.editor_insert_failed,Toast.LENGTH_SHORT).show();
            return -1;
        }
        return 1;
    }else{
        return getContentResolver().update(getUri(name),values,null,null);
    }
}

    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter SpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        SpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mSpinner.setAdapter(SpinnerAdapter);

        // Set the integer mSelected to the constant values
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.increase))) {
                        mMethod= INCREASE;
                    } else if (selection.equals(getString(R.string.decrease))) {
                        mMethod=DECREASE;
                    } else {
                        mMethod=OVERRIDE;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
               mMethod= INCREASE;
            }
        });
    }

    public void queryBill(View view) {
        String name = mNameEdit.getText().toString().trim();
        if(TextUtils.isEmpty(name)){
            Toast.makeText(this,R.string.checkName,Toast.LENGTH_SHORT).show();
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putString("_name",name);
        int BILL_LOADER = 0;
        LoaderManager.getInstance(this).initLoader(BILL_LOADER, bundle, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = {
                InventContract.InventEntry.COLUMN_NAME,
                InventContract.InventEntry.COLUMN_BILLS};
        Uri billUri = myContentUris.withAppendedName(InventContract.InventEntry.BILL_URI, args.getString("_name"));
        return new CursorLoader(this, billUri, projection, null, null, null);
    }
    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        Log.i("OnLoadFinished","Bill load finished");
        if (cursor == null || cursor.getCount() < 1) {
            mBillText.setText(String.valueOf(initialBill));
            Log.i("onLoadFinished","fail to find bill");
            return;
        } else if (cursor.moveToFirst()) {
            Log.i("onLoadFinished","found Bill");
            int BillColumnIndex = cursor.getColumnIndex(InventContract.InventEntry.COLUMN_BILLS);
            float bill = cursor.getFloat(BillColumnIndex);
            initialBill = bill;
            mBillText.setText(new BigDecimal(initialBill).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        }
        cursor.close();
        LoaderManager.getInstance(this).destroyLoader(loader.getId());
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mBillText.setText("");
    }
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener BackdiscardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };
        showUnsavedChangesDialog(BackdiscardButtonClickListener);
    }

    public void rootViewListener(View v) {
        switch (v.getId()) {
            case R.id.rootView:
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                v.setFocusable(true);
                v.setFocusableInTouchMode(true);
                v.requestFocus();
                break;
        }
    }
}
