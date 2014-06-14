package ru.amobilestudio.autorazborassistant.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.widget.SimpleCursorAdapter;

import ru.amobilestudio.autorazborassistant.db.SelectsDataDb;

/**
 * Created by vetal on 10.06.14.
 */
public class SelectsCursorAdapter extends SimpleCursorAdapter{

    private Context _context;
    private LayoutInflater _layoutInflater;

    public SelectsCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

    @Override
    public CharSequence convertToString(Cursor cursor) {
        String name = cursor.getString(cursor.getColumnIndex(SelectsDataDb.COLUMN_NAME_VALUE));
        return name;
    }
}
