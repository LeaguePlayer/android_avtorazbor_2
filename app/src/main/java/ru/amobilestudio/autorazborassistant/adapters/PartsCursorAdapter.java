package ru.amobilestudio.autorazborassistant.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import ru.amobilestudio.autorazborassistant.app.R;
import ru.amobilestudio.autorazborassistant.db.PartsDataDb;

/**
 * Created by vetal on 10.06.14.
 */
public class PartsCursorAdapter extends SimpleCursorAdapter{

    private Context _context;
    private LayoutInflater _layoutInflater;

    public PartsCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

    /*public PartsCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);

        _context = context;
        _layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }*/

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return _layoutInflater.inflate(R.layout.part_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tv = (TextView) view.findViewById(R.id.part_name);
        tv.setText(cursor.getString(cursor.getColumnIndex(PartsDataDb.COLUMN_PART_NAME)));

        tv = (TextView) view.findViewById(R.id.part_date);
        tv.setText(cursor.getString(cursor.getColumnIndex(PartsDataDb.COLUMN_PART_CREATE_DATE)));
    }

}
