package ru.amobilestudio.autorazborassistant.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by vetal on 09.06.14.
 */
public class SelectsDataDb extends DbSQLiteHelper{

    private Context _context;

    public SelectsDataDb(Context context) {
        super(context);

        _context = context;
    }

    public Cursor fetchAll(String tableName){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM " + tableName, null);

        if(c != null)
            c.moveToFirst();

        return c;
    }

    public Cursor findStr(String tableName, String query){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.query(tableName, new String[] {BaseColumns._ID, COLUMN_ID_VALUE, COLUMN_NAME_VALUE}, COLUMN_NAME_VALUE + " LIKE ?",
                new String[] { "%" + query + "%" }, null, null, null, null);

        return c;
    }

    public void addItem(String tableName, ContentValues cv){
        SQLiteDatabase db = this.getWritableDatabase();

        String id = cv.getAsString(COLUMN_ID_VALUE);

        Cursor c = db.query(tableName, new String[] {COLUMN_NAME_VALUE}, COLUMN_ID_VALUE + "=?",
                new String[] { id }, null, null, null, null);

        if(c.moveToFirst() && c.getCount() > 0) //exists
            db.update(tableName, cv, COLUMN_ID_VALUE + "=?", new String[] { id });
        else
            db.insert(tableName, null, cv);
    }

    public static final class Item{
        private int _id;
        private String _value;

        public Item(int id, String value){
            _id = id;
            _value = value;
        }

        public int getId() {
            return _id;
        }

        public String getValue() {
            return _value;
        }

        @Override
        public String toString() {
            return _value;
        }
    }
}
