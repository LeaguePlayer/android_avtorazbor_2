package ru.amobilestudio.autorazborassistant.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import ru.amobilestudio.autorazborassistant.app.R;
import ru.amobilestudio.autorazborassistant.helpers.ActivityHelper;

/**
 * Created by vetal on 09.06.14.
 */
public class PartsDataDb extends DbSQLiteHelper {

    private Context _context;

    public static final int STATE_NO_SYNC = 1;
    public static final int STATE_START_SYNC = 2;
    public static final int STATE_SUCCESS_SYNC = 3;
    public static final int STATE_ALLOW_SYNC = 4;
    public static final int STATE_ERROR_SYNC = 5;

    //statuses
    public static final int STATUS_PUBLISH = 1;
    public static final int STATUS_ON_DEVICE = 7;
    public static final int STATUS_RESERVE_DEVICE = 8;

    public HashMap<Integer, String> states;

    public PartsDataDb(Context context) {
        super(context);

        _context = context;

        states = new HashMap<Integer, String>();

        states.put(STATE_NO_SYNC, context.getString(R.string.state_no_sync));
        states.put(STATE_START_SYNC, context.getString(R.string.state_start_sync));
        states.put(STATE_SUCCESS_SYNC, context.getString(R.string.state_success_sync));
        states.put(STATE_ALLOW_SYNC, context.getString(R.string.state_allow_sync));
        states.put(STATE_ERROR_SYNC, context.getString(R.string.state_error_sync));
    }

    public Cursor fetchAllReservedParts(int userId){
        SQLiteDatabase db = this.getReadableDatabase();

        //Cursor c = db.query(TABLE_NAME_PARTS, new String[] {BaseColumns._ID, COLUMN_PART_ID, COLUMN_PART_NAME, COLUMN_PART_CREATE_DATE},
        //        null, null, null, null, null);
        String[] select = new String[] { BaseColumns._ID, COLUMN_PART_ID, COLUMN_PART_NAME, COLUMN_PART_CREATE_DATE };
        Cursor c = db.query(TABLE_NAME_PARTS, select, COLUMN_PART_USER_ID + "=? AND " +
                COLUMN_PART_STATUS + "=" + STATUS_RESERVE_DEVICE, new String[] { userId + "" }, null, null, null, null);

        if(c != null)
            c.moveToFirst();

        return c;
    }

    //for main AsyncTask
    public Cursor fetchAllNoReserved(int userId){
        SQLiteDatabase db = this.getReadableDatabase();

        String whereClause = COLUMN_PART_USER_ID + "=? AND " + COLUMN_PART_STATE + " in (?, ?)";
        String[] params = new String[] {userId + "", STATE_ALLOW_SYNC + "", STATE_ERROR_SYNC + ""};

        /*String whereClause = COLUMN_PART_USER_ID + "=? AND " + COLUMN_PART_STATUS + "!=?";
        String[] params = new String[] {userId + "", STATUS_RESERVE_DEVICE + ""};*/

        Cursor c = db.query(TABLE_NAME_PARTS, getAllColumns(), whereClause, params, null, null, null, null);

        if(c != null)
            c.moveToFirst();

        return c;
    }

    public Cursor fetchForSyncParts(int userId){
        SQLiteDatabase db = this.getReadableDatabase();

        String[] params = new String[] { userId + "", STATE_ALLOW_SYNC + "",
                STATE_SUCCESS_SYNC + "", STATE_START_SYNC + "", STATE_ERROR_SYNC + ""};

        String[] select = new String[] { BaseColumns._ID, COLUMN_PART_ID, COLUMN_PART_NAME, COLUMN_PART_UPDATE_DATE, COLUMN_PART_STATE };

        String whereClause = COLUMN_PART_USER_ID + "=? AND " + COLUMN_PART_STATE + " in (?, ?, ?, ?)";
        Cursor c = db.query(TABLE_NAME_PARTS, select, whereClause, params, null, null, null, null);

        if(c != null)
            c.moveToFirst();

        return c;
    }

    public Cursor fetchPart(long id){
        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery("select * from " + TABLE_NAME_PARTS +" where " + BaseColumns._ID + "=?", new String[] { id + "" });
    }

    public void saveToSyncPart(long id, ContentValues cv){
        SQLiteDatabase db = this.getWritableDatabase();

        if(id > 0)
            db.update(TABLE_NAME_PARTS, cv, BaseColumns._ID + "=?", new String[] { id + "" });
    }

    public void setStateToPart(long id, int state){
        SQLiteDatabase db = this.getWritableDatabase();

        if(id > 0 && states.containsKey(state)){
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_PART_STATE, state);

            db.update(TABLE_NAME_PARTS, cv, BaseColumns._ID + "=?", new String[] { id + "" });
        }
    }

    public int getStatePart(long id){
        SQLiteDatabase db = this.getReadableDatabase();

        String[] params = new String[] { id + ""};
        String[] select = new String[] { BaseColumns._ID, COLUMN_PART_STATE };

        Cursor c = db.query(TABLE_NAME_PARTS, select, BaseColumns._ID + "=?", params, null, null, null, null);

        if(c != null)
            c.moveToFirst();
        else
            return 0;

        return c.getInt(c.getColumnIndex(COLUMN_PART_STATE));
    }

    //insert or update part
    public void addPart(ContentValues cv){
        SQLiteDatabase db = this.getWritableDatabase();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // stop HERE

        //get Id
        String partId = cv.getAsString(COLUMN_PART_ID);

        //search Part in DB's device
        String[] params = new String[] { partId + ""};
        String[] select = new String[] { BaseColumns._ID, COLUMN_PART_UPDATE_DATE };

        Cursor cursor = db.query(TABLE_NAME_PARTS, select, COLUMN_PART_ID + "=?",
                params, null, null, null, null);
        //Cursor cursor = db.rawQuery("select 1 from " + TABLE_NAME_PARTS +" where " + COLUMN_PART_ID + "=?", new String[] { ID });
        boolean exists = (cursor.getCount() > 0);

        String newUpdateDate = cv.getAsString(COLUMN_PART_UPDATE_DATE);
        long newUpdateTime = 0;
        if(newUpdateDate != null && !newUpdateDate.equals("") && !newUpdateDate.equals("null")) {
            try {
                Date d = dateFormat.parse(newUpdateDate);
                newUpdateTime = d.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        //if exist update else add to Db
        if(exists && cursor.moveToFirst()){
            long oldUpdatetime = cursor.getLong(cursor.getColumnIndex(COLUMN_PART_UPDATE_DATE));
            ActivityHelper.debug("old - " + oldUpdatetime + " new - " + newUpdateTime);
            if (newUpdateTime > 0 && newUpdateTime > oldUpdatetime){
                cv.put(COLUMN_PART_UPDATE_DATE, newUpdateTime);
            }else
                cv.put(COLUMN_PART_UPDATE_DATE, oldUpdatetime);
            db.update(TABLE_NAME_PARTS, cv, COLUMN_PART_ID + "=?", new String[]{ partId });
        }else{
            try {
                Date d = dateFormat.parse(cv.getAsString(COLUMN_PART_CREATE_DATE));
                cv.put(COLUMN_PART_STATE, STATE_SUCCESS_SYNC);
                cv.put(COLUMN_PART_CREATE_DATE, d.getTime());
                db.insert(TABLE_NAME_PARTS, null, cv);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        cursor.close();
    }

    //add Reserve part
    public void addReservePart(int id, int userId){

        if(id > 0 && userId > 0){
            ContentValues part = new ContentValues();

            part.put(COLUMN_PART_ID, id);
            part.put(COLUMN_PART_NAME, _context.getString(R.string.part_name_default));
            part.put(COLUMN_PART_PRICE_SELL, 0);
            part.put(COLUMN_PART_PRICE_BUY, 0);
            part.put(COLUMN_PART_STATE, STATE_NO_SYNC);
            part.put(COLUMN_PART_STATUS, STATUS_RESERVE_DEVICE);
            part.put(COLUMN_PART_USER_ID, userId);
            part.put(COLUMN_PART_CREATE_DATE, System.currentTimeMillis());

            SQLiteDatabase db = this.getWritableDatabase();

            db.insert(TABLE_NAME_PARTS, null, part);
        }
    }

    public void deletePart(long id){
        SQLiteDatabase db = this.getWritableDatabase();

        //delete part images
        ImagesDataDb imagesDataDb = new ImagesDataDb(_context);
        Cursor c = imagesDataDb.fetchAllImages(id);

        if(c.getCount() > 0 && c != null){
            do{
                imagesDataDb.delete(c.getLong(c.getColumnIndex(BaseColumns._ID)));
            }while(c.moveToNext());
        }

        //delete part
        db.delete(TABLE_NAME_PARTS, BaseColumns._ID + "=?", new String[]{ id + "" });
        c.close();
    }

    private static String[] getAllColumns(){

        return new String[]{
            BaseColumns._ID,
            COLUMN_PART_ID,
            COLUMN_PART_NAME,
            COLUMN_PART_PRICE_SELL,
            COLUMN_PART_PRICE_BUY,
            COLUMN_PART_COMMENT,
            COLUMN_PART_CATEGORY_ID,
            COLUMN_PART_CAR_MODEL_ID,
            COLUMN_PART_LOCATION_ID,
            COLUMN_PART_SUPPLIER_ID,
            COLUMN_PART_BU_ID,
            COLUMN_PART_CREATE_DATE,
            COLUMN_PART_UPDATE_DATE,
            COLUMN_PART_USER_ID,
            COLUMN_PART_STATE,
            COLUMN_PART_STATUS
        };
    }
}
