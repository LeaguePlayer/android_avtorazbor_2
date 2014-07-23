package ru.amobilestudio.autorazborassistant.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

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

        Cursor c = db.query(TABLE_NAME_IMAGES, new String[] { COLUMN_IMAGES_PATH },
                BaseColumns._ID + "=?", new String[] { id + "" }, null, null, null, null);

        if(c.moveToFirst()){
            String image_uri = c.getString(c.getColumnIndex(COLUMN_IMAGES_PATH));

            try {
                URI uri = new URI(image_uri);
                File file = new File(uri);

                getContext().getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        MediaStore.Images.ImageColumns.DATA + "='" + file.getPath() + "'", null);

                getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                        Uri.parse("file://" + Environment.getExternalStorageDirectory())));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

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

    public int getCount(long part_id){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.query(TABLE_NAME_IMAGES, new String[] { BaseColumns._ID },
                COLUMN_IMAGES_PART_ID + "=?", new String[] { part_id + "" }, null, null, null, null);

        if (c.moveToFirst())
            return c.getCount();

        return 0;
    }

    public ArrayList<Image> fetchListImages(long part_id){
        SQLiteDatabase db = this.getReadableDatabase();

        ArrayList<Image> list = new ArrayList<Image>();

        Cursor c = db.query(TABLE_NAME_IMAGES, new String[] { BaseColumns._ID, COLUMN_IMAGES_PART_ID, COLUMN_IMAGES_PATH, COLUMN_IMAGES_STATE },
                COLUMN_IMAGES_PART_ID + "=?", new String[] { part_id + "" }, null, null, null, null);

        while(c.moveToNext()){
            Image image = new Image(
                    c.getLong(c.getColumnIndex(BaseColumns._ID)),
                    c.getString(c.getColumnIndex(COLUMN_IMAGES_PATH)),
                    c.getInt(c.getColumnIndex(COLUMN_IMAGES_STATE))
            );
            list.add(image);
        }

        return list;
    }

    static public class Image{
        private long _id;
        private String _uri;
        private int _state;


        public Image(long _id, String _uri, int _state) {
            this._id = _id;
            this._uri = _uri;
            this._state = _state;
        }

        public long get_id(){
            return _id;
        }

        public Uri get_uri(){
            return Uri.parse(_uri);
        }

        public int get_state(){
            return _state;
        }
    }
}
