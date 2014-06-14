package ru.amobilestudio.autorazborassistant.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by vetal on 14.06.14.
 */
public class ImagesDataDb extends DbSQLiteHelper {

    public static final int STATE_NO_SYNC = 1;
    public static final int STATE_START_SYNC = 2;
    public static final int STATE_SUCCESS_SYNC = 3;
    public static final int STATE_ALLOW_SYNC = 4;

    public ImagesDataDb(Context context) {
        super(context);
    }

    public void add(long part_id, String path){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();

        cv.put(COLUMN_IMAGES_PART_ID, part_id);
        cv.put(COLUMN_IMAGES_PATH, path);
        cv.put(COLUMN_IMAGES_STATE, STATE_ALLOW_SYNC);

        db.insert(TABLE_NAME_IMAGES, null, cv);
    }

    public void delete(long id){
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_NAME_IMAGES, BaseColumns._ID + "=?", new String[] { id+"" });
    }

    public Cursor fetchAllImages(long part_id){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.query(TABLE_NAME_IMAGES, new String[] { BaseColumns._ID, COLUMN_IMAGES_PART_ID, COLUMN_IMAGES_PATH, COLUMN_IMAGES_STATE },
                COLUMN_IMAGES_PART_ID + "=?", new String[] { part_id + "" }, null, null, null, null);

        if (c != null)
            c.moveToFirst();

        return c;
    }
}
