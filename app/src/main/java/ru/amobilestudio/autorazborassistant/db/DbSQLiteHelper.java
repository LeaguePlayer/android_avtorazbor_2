package ru.amobilestudio.autorazborassistant.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by vetal on 09.06.14.
 */
public class DbSQLiteHelper extends SQLiteOpenHelper {
    private Context _context;

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "AutoAssistant.db";

    public static final String DB_PREFS = "DbPrefsFile";

    public static final String COLUMN_ID_VALUE = "id";
    public static final String COLUMN_NAME_VALUE = "name";

    private static final String INT_TYPE = " INTEGER";
    private static final String TEXT_TYPE = " TEXT";
    private static final String REAL_TYPE = " REAL";

    //parts table
    public static final String TABLE_NAME_PARTS= "parts";

    public static final String COLUMN_PART_ID = "id";
    public static final String COLUMN_PART_NAME = "name";
    public static final String COLUMN_PART_PRICE_SELL = "price_sell";
    public static final String COLUMN_PART_PRICE_BUY = "price_buy";
    public static final String COLUMN_PART_COMMENT = "comment";
    public static final String COLUMN_PART_CATEGORY_ID = "category_id";
    public static final String COLUMN_PART_CAR_MODEL_ID = "car_model_id";
    public static final String COLUMN_PART_LOCATION_ID = "location_id";
    public static final String COLUMN_PART_SUPPLIER_ID = "supplier_id";
    public static final String COLUMN_PART_BU_ID = "bu_id";
    public static final String COLUMN_PART_CREATE_DATE = "create_time";
    public static final String COLUMN_PART_UPDATE_DATE = "update_time";
    public static final String COLUMN_PART_USER_ID = "user_id";
    public static final String COLUMN_PART_STATE = "state";
    public static final String COLUMN_PART_STATUS = "status";

    //images table
    public static final String TABLE_NAME_IMAGES= "images";

    public static final String COLUMN_IMAGES_PART_ID= "part_id";
    public static final String COLUMN_IMAGES_PATH= "path";
    public static final String COLUMN_IMAGES_STATE= "state";

    public static final String TABLE_NAME_CATEGORIES = "categories";
    public static final String TABLE_NAME_CAR_MODELS = "car_models";
    public static final String TABLE_NAME_LOCATIONS = "locations";
    public static final String TABLE_NAME_SUPPLIERS = "suppliers";
    public static final String TABLE_NAME_BU_CARS = "bu_cars";

    public static final String[] TABLE_NAMES = new String[]{
            TABLE_NAME_CATEGORIES,
            TABLE_NAME_CAR_MODELS,
            TABLE_NAME_LOCATIONS,
            TABLE_NAME_SUPPLIERS,
            TABLE_NAME_BU_CARS
    };

    public DbSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        _context = context;
    }

    public Context getContext(){
        return _context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //create parts table
        String partCreateSQL = "CREATE TABLE " + TABLE_NAME_PARTS + " (" +
                BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_PART_ID + INT_TYPE + ", " +
                COLUMN_PART_NAME + TEXT_TYPE + ", " +
                COLUMN_PART_PRICE_SELL + REAL_TYPE + ", " +
                COLUMN_PART_PRICE_BUY + REAL_TYPE + ", " +
                COLUMN_PART_COMMENT + TEXT_TYPE + ", " +
                COLUMN_PART_CATEGORY_ID + INT_TYPE + ", " +
                COLUMN_PART_CAR_MODEL_ID + INT_TYPE + ", " +
                COLUMN_PART_LOCATION_ID + INT_TYPE + ", " +
                COLUMN_PART_SUPPLIER_ID + INT_TYPE + ", " +
                COLUMN_PART_BU_ID + INT_TYPE + ", " +
                COLUMN_PART_CREATE_DATE + INT_TYPE + ", " +
                COLUMN_PART_UPDATE_DATE + INT_TYPE + ", " +
                COLUMN_PART_USER_ID + INT_TYPE + ", " +
                COLUMN_PART_STATUS + INT_TYPE + ", " +
                COLUMN_PART_STATE + INT_TYPE +
                " ); ";
        db.execSQL(partCreateSQL);

        //create images table
        String imagesCreateSQL = "CREATE TABLE " + TABLE_NAME_IMAGES + " (" +
                BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_IMAGES_PART_ID + INT_TYPE + ", " +
                COLUMN_IMAGES_STATE + INT_TYPE + ", " +
                COLUMN_IMAGES_PATH + TEXT_TYPE +
                " ); ";
        db.execSQL(imagesCreateSQL);

        //create dictonaries
        for (String s : TABLE_NAMES){
            db.execSQL("CREATE TABLE " + s + " (" +
                    BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_ID_VALUE + INT_TYPE + ", " +
                    COLUMN_NAME_VALUE + TEXT_TYPE +
                    " ); ");
        }

        setTrueDb(_context);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL(" DROP TABLE IF EXISTS " + TABLE_NAME_PARTS + ";");
        db.execSQL(" DROP TABLE IF EXISTS " + TABLE_NAME_IMAGES + ";");

        for (String s : TABLE_NAMES){
            db.execSQL(" DROP TABLE IF EXISTS " + s + ";");
        }

        setFalseDb(_context);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public String getValueById(int id, String tableName){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.query(tableName, new String[]{COLUMN_NAME_VALUE}, COLUMN_ID_VALUE + "=?",
                new String[]{id + ""}, null, null, null, null);

        String result = "";

        if(id == 0) return result;

        if(c.moveToFirst())
            result = c.getString(c.getColumnIndex(COLUMN_NAME_VALUE));

        return result;
    }

    public int getIdByValue(String name, String tableName){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.query(tableName, new String[] {COLUMN_ID_VALUE}, COLUMN_NAME_VALUE + "=?",
                new String[] { name }, null, null, null, null);

        int result = 0;
        if(c.moveToFirst())
            result = c.getInt(c.getColumnIndex(COLUMN_ID_VALUE));

        return result;
    }

    public static void setTrueDb(Context context){

        SharedPreferences dbInfo = context.getSharedPreferences(DB_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = dbInfo.edit();

        editor.putBoolean("isDb", true);
        editor.commit();
    }

    public static void setFalseDb(Context context){

        SharedPreferences dbInfo = context.getSharedPreferences(DB_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = dbInfo.edit();

        editor.putBoolean("isDb", false);
        editor.commit();
    }

    public static boolean isCreateDb(Context context){
        SharedPreferences dbInfo = context.getSharedPreferences(DB_PREFS, Context.MODE_PRIVATE);

        return dbInfo.getBoolean("isDb", false);
    }
}
