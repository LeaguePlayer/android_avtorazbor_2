package ru.amobilestudio.autorazborassistant.adapters;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import ru.amobilestudio.autorazborassistant.app.R;
import ru.amobilestudio.autorazborassistant.db.PartsDataDb;
import ru.amobilestudio.autorazborassistant.helpers.ActivityHelper;

/**
 * Created by vetal on 12.06.14.
 */
public class PartsSyncCursorAdapter extends SimpleCursorAdapter {

    private Context _context;
    private int _layout;
    private Cursor _cursor;
    private PartsDataDb _partsDataDb;

    public PartsSyncCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);

        _context = context;
        _layout = layout;
        _cursor = c;
        _partsDataDb = new PartsDataDb(context);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        //set id
        TextView tv = (TextView) view.findViewById(R.id.part_id);
        tv.setText(cursor.getString(cursor.getColumnIndex(PartsDataDb.COLUMN_PART_ID)));

        //set name
        tv = (TextView) view.findViewById(R.id.part_name);
        tv.setText(cursor.getString(cursor.getColumnIndex(PartsDataDb.COLUMN_PART_NAME)));


        //set update date
        long dateMilli = cursor.getLong(cursor.getColumnIndex(PartsDataDb.COLUMN_PART_UPDATE_DATE));
        Date updateDate = new Date(dateMilli);
        Log.d(ActivityHelper.TAG, "update " + dateMilli);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm");

        tv = (TextView) view.findViewById(R.id.part_update);
        tv.setText(dateFormat.format(updateDate));

        //set state
        tv = (TextView) view.findViewById(R.id.part_state);
        int state = cursor.getInt(cursor.getColumnIndex(PartsDataDb.COLUMN_PART_STATE));
        tv.setText(_partsDataDb.states.get(state));

        //set state icon
        ImageView stateIcon = (ImageView) view.findViewById(R.id.part_state_icon);
        switch (state){
            case PartsDataDb.STATE_ALLOW_SYNC:
                stateIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_action_pause));
                break;
            case PartsDataDb.STATE_NO_SYNC:
                stateIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_action_error));
                break;
            case PartsDataDb.STATE_START_SYNC:
                stateIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_action_upload));
                break;
            case PartsDataDb.STATE_SUCCESS_SYNC:
                stateIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_action_accept));
                break;
        }

    }
}
