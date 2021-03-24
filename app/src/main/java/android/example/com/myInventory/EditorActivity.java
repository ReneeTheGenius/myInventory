package android.example.com.myInventory;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.example.com.myInventory.data.InventContract;
import android.example.com.myInventory.data.myContentUris;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks {

    private static final int INVENT_LOADER = 0;
    private static final int BILL_LOADER = 1;
    private LinearLayout cards;
    private Calendar mCalendar;
    private Uri mCurrentUri;
    private int mCards;
    private boolean mHasChanged;
    private EditText mNameEditText;
    private TextView mDateText;
    private TextView mBillText;
    private float mInitialedTotal;
    private int baseId;
    private TextView mTotalText;
    private EditText mTypeEditText;
    private EditText mAmountEditText;
    private EditText mMoneyUnitEditText;
    private View card;
    private Float mTotal;
    private Float mBill;
    private EditText mDanweiEditText;
    private View.OnTouchListener mTouchListener;
    private String mName;

    private View.OnFocusChangeListener mFocusChangedListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            mHasChanged = true;
            if (!hasFocus) {
                calculateMoney(v);
                calculateTotal(mTotalText);
            }
        }
    };

    private void addCard(goods newGoods, int i) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, (int) getResources().getDimension(R.dimen.activity_margin_bottom));

        card = LayoutInflater.from(getBaseContext()).inflate(R.layout.goods_card, null, false);
        card.setLayoutParams(lp);
        mTypeEditText = card.findViewById(R.id.goods);
        mAmountEditText = card.findViewById(R.id.amount);
        mMoneyUnitEditText = card.findViewById(R.id.unit_price);
        mDanweiEditText = card.findViewById(R.id.danwei);
        mAmountEditText.setOnFocusChangeListener(mFocusChangedListener);
        mMoneyUnitEditText.setOnFocusChangeListener(mFocusChangedListener);
        TextView mMoneyText = card.findViewById(R.id.money);
        mTypeEditText.setOnTouchListener(mTouchListener);
        mDanweiEditText.setOnTouchListener(mTouchListener);
        if (newGoods == null) {
            card.setId(baseId + mCards);
        } else {
            card.setId(baseId + i);
            mTypeEditText.setText(newGoods.getType());
            mDanweiEditText.setText(newGoods.getDanwei());
            mAmountEditText.setText(String.valueOf(newGoods.getAmount()));
            mMoneyUnitEditText.setText(String.valueOf(newGoods.getMoney_unit()));
            mMoneyText.setText(String.valueOf(newGoods.getMoney()));
        }
        cards.addView(card);
        mCards += 1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        mCurrentUri = getIntent().getData();
        baseId = 10000;
        mTotal = 0f;
        mCards = 0;
        mBill = 0f;
        mInitialedTotal = 0f;
        mHasChanged = false;

        mTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mHasChanged = true;
                return false;
            }
        };

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addCard(null, 0);
            }
        });
        mNameEditText = findViewById(R.id.name);
        cards = findViewById(R.id.cards);
        mTotalText = findViewById(R.id.total);
        mDateText = findViewById(R.id.date);
        mNameEditText.setOnTouchListener(mTouchListener);
        mBillText = findViewById(R.id.bill);
        if (mCurrentUri == null) {
            setTitle(getString(R.string.newInvent));
            invalidateOptionsMenu();
            addCard(null, 0);
        } else {
            mNameEditText.setFocusable(false);
            mNameEditText.setFocusableInTouchMode(false);
            setTitle(getString(R.string.editInvent));
            LoaderManager.getInstance(this).initLoader(INVENT_LOADER, null, this);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        return true;
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        switch (id) {
            case INVENT_LOADER: {
                String[] projection = {
                        InventContract.InventEntry._ID,
                        InventContract.InventEntry.COLUMN_NAME,
                        InventContract.InventEntry.COLUMN_DATE,
                        InventContract.InventEntry.COLUMN_TOTAL,
                        InventContract.InventEntry.COLUMN_GOODS,
                        InventContract.InventEntry.COLUMN_NUMBER};
                return new CursorLoader(this,   // Parent activity context
                        mCurrentUri,         // Query the content URI for the current pet
                        projection,             // Columns to include in the resulting Cursor
                        null,                   // No selection clause
                        null,                   // No selection arguments
                        null);
            }
            case BILL_LOADER: {
                Log.i("OncreateLoader", "create BILL LOADER");
                String[] projection = {
                        InventContract.InventEntry.COLUMN_NAME,
                        InventContract.InventEntry.COLUMN_BILLS};
                Uri billUri = myContentUris.withAppendedName(InventContract.InventEntry.BILL_URI, args.getString("_name"));
                return new CursorLoader(this, billUri, projection, null, null, null);
            }
            default:
                return null;
        }

    }

    @Override
    public void onLoadFinished(@NonNull Loader loader, Object data) {
        Cursor cursor = (Cursor) data;
        switch (loader.getId()) {
            case INVENT_LOADER:
                if (cursor == null || cursor.getCount() < 1) {
                    return;
                }
                if (mCards > 0) return;
                if (cursor.moveToFirst()) {
                    // Find the columns of pet attributes that we're interested in
                    int nameColumnIndex = cursor.getColumnIndex(InventContract.InventEntry.COLUMN_NAME);
                    int numberColumnIndex = cursor.getColumnIndex(InventContract.InventEntry.COLUMN_NUMBER);
                    int totalColumnIndex = cursor.getColumnIndex(InventContract.InventEntry.COLUMN_TOTAL);
                    int goodsColumnIndex = cursor.getColumnIndex(InventContract.InventEntry.COLUMN_GOODS);
                    int dateColumnIndex = cursor.getColumnIndex(InventContract.InventEntry.COLUMN_DATE);

                    // Extract out the value from the Cursor for the given column index
                    String name = cursor.getString(nameColumnIndex);
                    mName = name;
                    Bundle bundle = new Bundle();
                    bundle.putString("_name", name);
                    Log.i("onLoadFinished","start calling bill_loader");
                    LoaderManager.getInstance(this).initLoader(BILL_LOADER, bundle, this);
                    String mGoods = cursor.getString(goodsColumnIndex);
                    String date = cursor.getString(dateColumnIndex);
                    float total = cursor.getFloat(totalColumnIndex);
                    mInitialedTotal = total;
                    int number = cursor.getInt(numberColumnIndex);


                    // Update the views on the screen with the values from the database
                    mNameEditText.setText(name);
                    mDateText.setText(date);
                    mTotalText.setText(String.valueOf(total));
                    ArrayList<goods> Goods = goods.toGoods(mGoods);
                    for (int i = 0; i < number; i++) {
                        goods newGoods = Goods.get(i);
                        addCard(newGoods, i);
                    }
                }
                break;
            case BILL_LOADER:
                Log.i("OnLoadFinished","Bill load finished");
                if (cursor == null || cursor.getCount() < 1) {
                    mBillText.setText(String.valueOf(mBill));
                    Log.i("onLoadFinished","fail to find bill");
                    return;
                } else if (cursor.moveToFirst()) {
                    Log.i("onLoadFinished","found Bill");
                    int BillColumnIndex = cursor.getColumnIndex(InventContract.InventEntry.COLUMN_BILLS);
                    float bill = cursor.getFloat(BillColumnIndex);
                    mBill = bill;
                    Log.i("onLoadFinished","mBill="+mBill);
                    mBillText.setText(new BigDecimal(mBill).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                }
                break;
        }
        cursor.close();
        LoaderManager.getInstance(this).destroyLoader(loader.getId());
    }

    private void removeAllCards() {
        if (mCards < 1) return;
        for (int i = 0; i < mCards; i++) {
            card = findViewById(baseId + i);
            cards.removeView(card);
        }
        mCards = 0;
    }

    public void removeCard(View view) {
        if (mCards <= 1) {
            Toast.makeText(this, R.string.remove_fail, Toast.LENGTH_SHORT).show();
            return;
        }
        mHasChanged = true;
        view = (View) ((view.getParent().getParent()));
        int id = view.getId();
        id++;
        cards.removeView(view);
        for (; id < baseId + mCards; id++) {
            card = findViewById(id);
            if (card == null) {
                return;
            }
            card.setId(id - 1);
        }
        mCards -= 1;
        calculateTotal(mTotalText);
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {
        mNameEditText.setText("");
        mDateText.setText("");
        mTotalText.setText("");
        mBillText.setText("");
        removeAllCards();
        addCard(null, 0);
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

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mCurrentUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    private ArrayList<goods> getGoods() {
        ArrayList<goods> mGoods = new ArrayList<>();
        for (int i = 0; i < mCards; i++) {
            card = findViewById(baseId + i);
            mTypeEditText = card.findViewById(R.id.goods);
            mAmountEditText = card.findViewById(R.id.amount);
            mMoneyUnitEditText = card.findViewById(R.id.unit_price);
            mDanweiEditText = card.findViewById(R.id.danwei);

            String type = mTypeEditText.getText().toString().trim();
            String amount = mAmountEditText.getText().toString().trim();
            String money_unit = mMoneyUnitEditText.getText().toString().trim();
            String danwei = mDanweiEditText.getText().toString().trim();
            if (TextUtils.isEmpty(type)) {
                Toast.makeText(this, R.string.checkType, Toast.LENGTH_SHORT).show();
                return null;
            }
            if (TextUtils.isEmpty(amount)) {
                Toast.makeText(this, R.string.checkAmount, Toast.LENGTH_SHORT).show();
                return null;
            }
            if (TextUtils.isEmpty(money_unit)) {
                Toast.makeText(this, R.string.checkUnitPrice, Toast.LENGTH_SHORT).show();
                return null;
            }
            if (TextUtils.isEmpty(danwei)) {
                danwei = "元";
            }

            float moneyUnit = Float.parseFloat(money_unit);
            float Amount = Float.parseFloat(amount);
            if (Math.abs(moneyUnit) < 0.00001f || Math.abs(Amount) < 0.00001f) {
                Toast.makeText(this, R.string.zeroMoney, Toast.LENGTH_SHORT).show();
                return null;
            }
            mGoods.add(new goods(type, moneyUnit, Amount, danwei));
        }
        return mGoods;
    }

    private void saveInvent() {
        if (!mHasChanged) {
            finish();
            return;
        }
        String name = mNameEditText.getText().toString().trim();
        String date = mDateText.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, R.string.wrongBuyerInfoDialogMsg, Toast.LENGTH_SHORT).show();
            return;
        }

        if (mCards < 1) {
            Toast.makeText(this, R.string.noInventDialogMsg, Toast.LENGTH_SHORT).show();
            return;
        }
        calculateTotal(mTotalText);
        getBill(name);

        ContentValues values = new ContentValues();
        String mGoods = goods.toJson(getGoods());
        mTotal = new BigDecimal(mTotal).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
        if (mGoods == null) {
            return;
        }
        Log.v("SaveInvent", "开始保存");
        values.put(InventContract.InventEntry.COLUMN_NAME, name);
        values.put(InventContract.InventEntry.COLUMN_DATE, date);
        values.put(InventContract.InventEntry.COLUMN_NUMBER, mCards);
        values.put(InventContract.InventEntry.COLUMN_TOTAL, mTotal);
        values.put(InventContract.InventEntry.COLUMN_GOODS, mGoods);

        if (mCurrentUri == null) {
            Uri newUri = getContentResolver().insert(InventContract.InventEntry.CONTENT_URI, values);
            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_successful),
                        Toast.LENGTH_SHORT).show();
                values.clear();
                values.put(InventContract.InventEntry.COLUMN_NAME, name);
                try {
                    values.put(InventContract.InventEntry.COLUMN_BILLS, mTotal);
                    newUri = getContentResolver().insert(InventContract.InventEntry.BILL_URI, values);
                    if (newUri == null) Log.e("SaveInvent", "插入欠款失败");
                    else Toast.makeText(this, "插入欠款成功", Toast.LENGTH_SHORT).show();
                } catch (SQLiteConstraintException e) {
                    updateBill(name, calculateBill(null));
                }


            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                rowsAffected = getContentResolver().update(mCurrentUri, values, null, null);
                if (rowsAffected > 0)
                    Toast.makeText(this, getString(R.string.editor_update_successful),
                            Toast.LENGTH_SHORT).show();
                updateBill(name, calculateBill(null));
            }
        }
        finish();
        return;
    }

    private void getBill(String name) {
        Cursor cursor = getContentResolver().query(getBillUri(name),null,null,null);
        if(cursor==null||cursor.getCount()<1)return;
        if (cursor.moveToFirst()){
            int billIndex=cursor.getColumnIndex(InventContract.InventEntry.COLUMN_BILLS);
            mBill=cursor.getFloat(billIndex);
        }
        cursor.close();
    }

    private void updateBill(String name, Float bill) {
        Uri billUri = myContentUris.withAppendedName(InventContract.InventEntry.BILL_URI, name);
        ContentValues values = new ContentValues();
        values.put(InventContract.InventEntry.COLUMN_BILLS, bill);
        int rowsAffected = getContentResolver().update(billUri, values, null, null);
        if (rowsAffected > 0) Toast.makeText(this, "更改欠款成功", Toast.LENGTH_SHORT).show();
        else Toast.makeText(this, "更改欠款失败", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                saveInvent();
                return true;

            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;

            case R.id.action_share:
                share();
                return true;

            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, navigate to parent activity.
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void share() {
        try {
            Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
            m.invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int REQUEST_CODE_CONTACT = 101;
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        //验证是否许可权限
        for (String str : permissions) {
            if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                return;
            }
        }

        View dView = getWindow().getDecorView();
        dView.destroyDrawingCache();
        dView.setDrawingCacheEnabled(true);
        dView.buildDrawingCache();
        Bitmap bmp = dView.getDrawingCache();

        File picFile = ShareToolUtil.saveSharePic(this, bmp);

        Intent intent = new Intent();
        ComponentName cop = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI");
        intent.setComponent(cop);
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");
        Uri uri = Uri.fromFile(picFile);
        intent.putExtra(Intent.EXTRA_STREAM, uri);// 分享的内容
        intent.setType("image/*");// 分享发送的数据类型
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(intent, "Share"));
    }


    /**
     * Prompt the user to confirm that they want to delete this pet.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteInvent();
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
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void PickDate(final View v) {
        mCalendar = Calendar.getInstance();
        new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        mCalendar.set(Calendar.YEAR, year);
                        mCalendar.set(Calendar.MONTH, month);
                        mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateDate(v);
                    }
                },
                mCalendar.get(Calendar.YEAR),
                mCalendar.get(Calendar.MONTH),
                mCalendar.get(Calendar.DAY_OF_MONTH)

        ).show();
    }

    public void calculateMoney(View view) {
        view = (View) view.getParent().getParent().getParent();
        EditText amount = view.findViewById(R.id.amount);
        EditText unit_price = view.findViewById(R.id.unit_price);
        String amountString = amount.getText().toString().trim();
        String unitString = unit_price.getText().toString().trim();
        if (TextUtils.isEmpty(amountString) || TextUtils.isEmpty(unitString)) {
            //Toast.makeText(this, R.string.checkCard, Toast.LENGTH_SHORT).show();
            return;
        }
        Float Amount = Float.parseFloat(amountString);
        Float UnitPrice = Float.parseFloat(unitString);
        Float money = Amount * UnitPrice;
        TextView moneyText = view.findViewById(R.id.money);
        BigDecimal bd = new BigDecimal(money);
        moneyText.setText(String.valueOf(bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue()));
        calculateTotal(mTotalText);
    }

    private float getMoney(View view) {
        if (view == null) {
            Log.e("getMoney", "cannot get Money on a null object");
            return 0;
        }
        EditText amount = view.findViewById(R.id.amount);
        EditText unit_price = view.findViewById(R.id.unit_price);
        String amountString = amount.getText().toString().trim();
        String unitString = unit_price.getText().toString().trim();
        if (TextUtils.isEmpty(amountString) || TextUtils.isEmpty(unitString)) {
            //Toast.makeText(this, R.string.checkCard, Toast.LENGTH_SHORT).show();
            return 0;
        }
        Float Amount = Float.parseFloat(amountString);
        Float UnitPrice = Float.parseFloat(unitString);
        return Amount * UnitPrice;
    }

    public void calculateTotal(View view) {
        float total = 0;
        for (int i = 0; i < mCards; i++) {
            card = findViewById(baseId + i);
            if (card == null) return;
            float money = getMoney(card);
            if (Math.abs(money) < 0.00001f) {
                //Toast.makeText(this, R.string.checkTotal, Toast.LENGTH_SHORT).show();
                return;
            }
            total += money;
        }
        BigDecimal bd = new BigDecimal(total);
        total = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
        ((TextView) view).setText(String.valueOf(total));
        mTotal = total;
        calculateBill(mBillText);
    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private void deleteInvent() {
        // Only perform the delete if this is an existing pet.
        if (mCurrentUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_failed), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_successful), Toast.LENGTH_SHORT).show();

                deleteBill(mName);
            }
        }

        // Close the activity
        finish();
    }

    private void deleteBill(String name) {
        ContentValues values = new ContentValues();
        float currentBill = mBill - mInitialedTotal;
        if (Math.abs(currentBill) < 0.000001f) {
            int rowsAffectd = getContentResolver().delete(getBillUri(name), null, null);
            if (rowsAffectd > 0) Toast.makeText(this, "欠款删除成功", Toast.LENGTH_SHORT).show();
            else Toast.makeText(this, "欠款删除失败", Toast.LENGTH_SHORT).show();
            return;
        }
        values.put(InventContract.InventEntry.COLUMN_BILLS, currentBill);
        int rowsAffectd = getContentResolver().update(getBillUri(name), values, null, null);
        if (rowsAffectd > 0) Toast.makeText(this, "欠款减少成功", Toast.LENGTH_SHORT).show();
        else Toast.makeText(this, "欠款减少失败", Toast.LENGTH_SHORT).show();
    }

    private Uri getBillUri(String name) {
        return myContentUris.withAppendedName(InventContract.InventEntry.BILL_URI, name);
    }

    /**
     * This method is called when the back button is pressed.
     */
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

    public void getDate(View view) {
        mHasChanged = true;
        PickDate(view);
    }

    private void updateDate(View v) {
        TextView mTimeText = (TextView) v;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        mTimeText.setText(sdf.format(mCalendar.getTime()));
    }

    private float calculateBill(View view) {
        Log.i("calculateBill","mTotal:"+mTotal);
        Log.i("calculateBill","mBill:"+mBill);
        Log.i("calculateBill","mInitialedTotal:"+mInitialedTotal);
        return new BigDecimal(mTotal + mBill - mInitialedTotal).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    public void displayBill(View view) {
        Log.i("display Bill", "cal display BILL");
        mName = mNameEditText.getText().toString().trim();
        if (TextUtils.isEmpty(mName)) {
            Toast.makeText(this, "请输入用户名", Toast.LENGTH_SHORT);
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putString("_name", mName);
        LoaderManager.getInstance(this).initLoader(BILL_LOADER, bundle, this);
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