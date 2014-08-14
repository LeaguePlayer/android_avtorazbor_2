package ru.amobilestudio.autorazborassistant.adapters;

import android.content.Context;
import android.database.Cursor;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;

import ru.amobilestudio.autorazborassistant.db.SelectsDataDb;

/**
 * Created by vetal on 10.06.14.
 */
public class SelectsCursorAdapter extends SimpleCursorAdapter{

    private SelectsDataDb _selectsDataDb;
    private String _tableName;

    public SelectsCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags, String tableName) {
        super(context, layout, c, from, to, flags);

        _selectsDataDb = new SelectsDataDb(context);
        _tableName = tableName;

        this.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence charSequence) {
                return _selectsDataDb.findStr(_tableName, charSequence.toString());
            }
        });
    }

    @Override
    public CharSequence convertToString(Cursor cursor) {
        String name = cursor.getString(cursor.getColumnIndex(SelectsDataDb.COLUMN_NAME_VALUE));
        return name;
    }
}
