package android.example.com.myInventory;

import android.content.Context;
import android.database.Cursor;
import android.example.com.myInventory.data.InventContract;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class InventCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link InventCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public InventCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView dateTextView = (TextView) view.findViewById(R.id.date);
        TextView totalTextView = (TextView) view.findViewById(R.id.total);

        // Find the columns of pet attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(InventContract.InventEntry.COLUMN_NAME);
        int dateColumnIndex = cursor.getColumnIndex(InventContract.InventEntry.COLUMN_DATE);
        int totalColumnIndex = cursor.getColumnIndex(InventContract.InventEntry.COLUMN_TOTAL);

        // Read the pet attributes from the Cursor for the current pet
        String name = cursor.getString(nameColumnIndex).trim();
        String date = cursor.getString(dateColumnIndex).trim();
        String total = cursor.getString(totalColumnIndex).trim();

        // If the pet breed is empty string or null, then use some default text
        // that says "Unknown breed", so the TextView isn't blank.
        if (TextUtils.isEmpty(date)) {
            date = context.getString(R.string.unknown_date);
        }

        // Update the TextViews with the attributes for the current pet
        nameTextView.setText(name);
        dateTextView.setText(date);
        totalTextView.setText(total);
    }
}
