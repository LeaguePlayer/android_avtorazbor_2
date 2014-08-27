package ru.amobilestudio.autorazborassistant.app;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import ru.amobilestudio.autorazborassistant.custom.MyAutoComplete;
import ru.amobilestudio.autorazborassistant.db.DbSQLiteHelper;
import ru.amobilestudio.autorazborassistant.db.ImagesDataDb;
import ru.amobilestudio.autorazborassistant.db.PartsDataDb;
import ru.amobilestudio.autorazborassistant.db.SelectsDataDb;
import ru.amobilestudio.autorazborassistant.helpers.ActivityHelper;
import ru.amobilestudio.autorazborassistant.helpers.AlertDialogHelper;


public class AddPartActivity extends ActionBarActivity implements View.OnClickListener {

    public static EditText _partsPriceSell;
    public static EditText _partsPriceBuy;
    public static EditText _partsComment;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    public static MyAutoComplete _partsCategoryId;
    public static MyAutoComplete _partsCarModelId;
    public static MyAutoComplete _partsLocationId;
    public static MyAutoComplete _partsSupplierId;
    public static MyAutoComplete _partsBuId;

    //for POST send
    public static int _categoryId;
    public static int _carModelId;
    public static int _locationId;
    public static int _supplierId;
    public static int _buId;

    private static Button _sendButton;
    private static Button _publishButton;
    private static Button _takePhoto;
    private static Button _viewPhoto;

    private PartsDataDb _partsDataDb;
    private ImagesDataDb _imagesDataDb;
    private ArrayList<String> _errors;

    private long _part_id;
    private int _count_images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_part);
        getOverflowMenu();

        _partsDataDb = new PartsDataDb(this);
        _imagesDataDb = new ImagesDataDb(this);
        _errors = new ArrayList<String>();

        //init
        _partsPriceSell = (EditText) findViewById(R.id.parts_price_sell);
        _partsPriceBuy = (EditText) findViewById(R.id.parts_price_buy);
        _partsComment = (EditText) findViewById(R.id.parts_comment);

        /*_partsComment.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                ActivityHelper.debug("focus textarea");
            }
        });*/

        /*------------Selects INIT--------------*/

        //focus listener
        View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                MyAutoComplete autoComplete = (MyAutoComplete) view;
                if(autoComplete.getText().toString().equals("")){
                    autoComplete.setText(" ");
                    autoComplete.setText("");
                }
            }
        };

        final SelectsDataDb selectsDataDb = new SelectsDataDb(this);

        String[] from = new String[] { SelectsDataDb.COLUMN_NAME_VALUE };
        int[] to = new int[] { android.R.id.text1 };

        //categories
        ArrayList<SelectsDataDb.Item> allItems = selectsDataDb.fetchAllItems(SelectsDataDb.TABLE_NAME_CATEGORIES);
        ArrayAdapter<SelectsDataDb.Item> arrayAdapter = new ArrayAdapter<SelectsDataDb.Item>(this, android.R.layout.simple_dropdown_item_1line, allItems);
        _partsCategoryId = (MyAutoComplete) findViewById(R.id.parts_category_id);
        _partsCategoryId.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {}
            @Override
            public void beforeTextChanged(CharSequence s, int i, int i2, int i3) {}
            @Override
            public void onTextChanged(CharSequence s, int i, int i2, int i3) {
                _categoryId = selectsDataDb.getIdByValue(s.toString(), SelectsDataDb.TABLE_NAME_CATEGORIES);
            }
        });
        _partsCategoryId.setOnFocusChangeListener(focusChangeListener);
        _partsCategoryId.setAdapter(arrayAdapter);

        //car models
        allItems = selectsDataDb.fetchAllItems(SelectsDataDb.TABLE_NAME_CAR_MODELS);
        arrayAdapter = new ArrayAdapter<SelectsDataDb.Item>(this, android.R.layout.simple_dropdown_item_1line, allItems);
        _partsCarModelId = (MyAutoComplete) findViewById(R.id.parts_car_model_id);
        _partsCarModelId.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {}
            @Override
            public void beforeTextChanged(CharSequence s, int i, int i2, int i3) {}
            @Override
            public void onTextChanged(CharSequence s, int i, int i2, int i3) {
                _carModelId = selectsDataDb.getIdByValue(s.toString(), SelectsDataDb.TABLE_NAME_CAR_MODELS);
            }
        });
        _partsCarModelId.setOnFocusChangeListener(focusChangeListener);
        _partsCarModelId.setAdapter(arrayAdapter);

        //locations
        allItems = selectsDataDb.fetchAllItems(SelectsDataDb.TABLE_NAME_LOCATIONS);
        arrayAdapter = new ArrayAdapter<SelectsDataDb.Item>(this, android.R.layout.simple_dropdown_item_1line, allItems);
        _partsLocationId = (MyAutoComplete) findViewById(R.id.parts_location_id);
        _partsLocationId.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {}
            @Override
            public void beforeTextChanged(CharSequence s, int i, int i2, int i3) {}
            @Override
            public void onTextChanged(CharSequence s, int i, int i2, int i3) {
                _locationId = selectsDataDb.getIdByValue(s.toString(), SelectsDataDb.TABLE_NAME_LOCATIONS);
            }
        });
        _partsLocationId.setOnFocusChangeListener(focusChangeListener);
        _partsLocationId.setAdapter(arrayAdapter);

        //suppliers
        allItems = selectsDataDb.fetchAllItems(SelectsDataDb.TABLE_NAME_SUPPLIERS);
        arrayAdapter = new ArrayAdapter<SelectsDataDb.Item>(this, android.R.layout.simple_dropdown_item_1line, allItems);
        _partsSupplierId = (MyAutoComplete) findViewById(R.id.parts_supplier_id);
        _partsSupplierId.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {}
            @Override
            public void beforeTextChanged(CharSequence s, int i, int i2, int i3) {}
            @Override
            public void onTextChanged(CharSequence s, int i, int i2, int i3) {
                _supplierId = selectsDataDb.getIdByValue(s.toString(), SelectsDataDb.TABLE_NAME_SUPPLIERS);
            }
        });
        _partsSupplierId.setOnFocusChangeListener(focusChangeListener);
        _partsSupplierId.setAdapter(arrayAdapter);

        //bu
        allItems = selectsDataDb.fetchAllItems(SelectsDataDb.TABLE_NAME_BU_CARS);
        arrayAdapter = new ArrayAdapter<SelectsDataDb.Item>(this, android.R.layout.simple_dropdown_item_1line, allItems);
        _partsBuId = (MyAutoComplete) findViewById(R.id.parts_bu_id);
        _partsBuId.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {}
            @Override
            public void beforeTextChanged(CharSequence s, int i, int i2, int i3) {}
            @Override
            public void onTextChanged(CharSequence s, int i, int i2, int i3) {
                _buId = selectsDataDb.getIdByValue(s.toString(), SelectsDataDb.TABLE_NAME_BU_CARS);
            }
        });
        _partsBuId.setOnFocusChangeListener(focusChangeListener);
        _partsBuId.setAdapter(arrayAdapter);

        /*------------Selects end--------------*/

        _sendButton = (Button) findViewById(R.id.save_part_button);
        _sendButton.setOnClickListener(this);

        _publishButton = (Button) findViewById(R.id.publish_part_button);
        _publishButton.setOnClickListener(this);

        //when click on item ListView
        Bundle extras = getIntent().getExtras();

        if(extras != null){
            _part_id = extras.getLong("part_id");

            SharedPreferences part_info = getSharedPreferences(DbSQLiteHelper.DB_PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = part_info.edit();

            editor.putLong("part_id", _part_id);
            editor.commit();

            setPartForm(_part_id);
        }
    }

    private void getOverflowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setPartForm(long part_id){

        Cursor c = _partsDataDb.fetchPart(part_id);

        ProgressBar loader = (ProgressBar) findViewById(R.id.loader);
        loader.setVisibility(View.GONE);

        if(c != null){
            c.moveToFirst();
            TextView tv = (TextView) findViewById(R.id.article_part);

            String text = tv.getText().toString();

            tv.setText(text + " " + c.getInt(c.getColumnIndex(PartsDataDb.COLUMN_PART_ID)));

            //format for price
            DecimalFormat df = new DecimalFormat("#");

            long price = c.getLong(c.getColumnIndex(PartsDataDb.COLUMN_PART_PRICE_SELL));
            String value = df.format(price);
            _partsPriceSell.setText(value);

            price = c.getLong(c.getColumnIndex(PartsDataDb.COLUMN_PART_PRICE_BUY));
            value = df.format(price);
            _partsPriceBuy.setText(value);

            _partsComment.setText(c.getString(c.getColumnIndex(PartsDataDb.COLUMN_PART_COMMENT)));

            int dictionary_id = c.getInt(c.getColumnIndex(PartsDataDb.COLUMN_PART_CATEGORY_ID));
            String name = _partsDataDb.getValueById(dictionary_id, PartsDataDb.TABLE_NAME_CATEGORIES);
            _partsCategoryId.setText(name);

            dictionary_id = c.getInt(c.getColumnIndex(PartsDataDb.COLUMN_PART_CAR_MODEL_ID));
            name = _partsDataDb.getValueById(dictionary_id, PartsDataDb.TABLE_NAME_CAR_MODELS);
            _partsCarModelId.setText(name);

            dictionary_id = c.getInt(c.getColumnIndex(PartsDataDb.COLUMN_PART_LOCATION_ID));
            name = _partsDataDb.getValueById(dictionary_id, PartsDataDb.TABLE_NAME_LOCATIONS);
            _partsLocationId.setText(name);

            dictionary_id = c.getInt(c.getColumnIndex(PartsDataDb.COLUMN_PART_SUPPLIER_ID));
            name = _partsDataDb.getValueById(dictionary_id, PartsDataDb.TABLE_NAME_SUPPLIERS);
            _partsSupplierId.setText(name);

            dictionary_id = c.getInt(c.getColumnIndex(PartsDataDb.COLUMN_PART_BU_ID));
            name = _partsDataDb.getValueById(dictionary_id, PartsDataDb.TABLE_NAME_BU_CARS);
            _partsBuId.setText(name);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_part_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case R.id.take_photo:
                takePhoto();
                break;
            case R.id.view_gallery:
                if(_imagesDataDb.getCount(_part_id) > 0){
                    Intent intent = new Intent(this, GalleryActivity.class);
                    intent.putExtra("part_id", _part_id);
                    startActivity(intent);
                }else{
                    AlertDialogHelper.showAlertDialog(this, R.string.alert_title, R.string.empty_images, true);
                }

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.save_part_button:
                savePart(false);
                break;
            case R.id.publish_part_button:
                savePart(true);
                break;
        }

    }

    private void savePart(boolean publish){

        if(validateForm()){
            ContentValues part = new ContentValues();

            part.put(PartsDataDb.COLUMN_PART_PRICE_SELL, _partsPriceSell.getText().toString());
            part.put(PartsDataDb.COLUMN_PART_PRICE_BUY, _partsPriceBuy.getText().toString());
            part.put(PartsDataDb.COLUMN_PART_COMMENT, _partsComment.getText().toString());

            part.put(PartsDataDb.COLUMN_PART_CATEGORY_ID, _categoryId);
            part.put(PartsDataDb.COLUMN_PART_CAR_MODEL_ID, _carModelId);
            part.put(PartsDataDb.COLUMN_PART_LOCATION_ID, _locationId);
            part.put(PartsDataDb.COLUMN_PART_SUPPLIER_ID, _supplierId);
            part.put(PartsDataDb.COLUMN_PART_BU_ID, _buId);

            part.put(PartsDataDb.COLUMN_PART_NAME, _partsCategoryId.getText().toString() + ", " + _partsCarModelId.getText().toString());

            part.put(PartsDataDb.COLUMN_PART_STATE, PartsDataDb.STATE_ALLOW_SYNC);
            part.put(PartsDataDb.COLUMN_PART_UPDATE_DATE, System.currentTimeMillis());

            if(publish)
                part.put(PartsDataDb.COLUMN_PART_STATUS, PartsDataDb.STATUS_PUBLISH);
            else
                part.put(PartsDataDb.COLUMN_PART_STATUS, PartsDataDb.STATUS_ON_DEVICE);

            _partsDataDb.saveToSyncPart(_part_id, part);

            finish();
        }else
            AlertDialogHelper.showAlertDialog(this, getString(R.string.error_title), TextUtils.join("\n", _errors), true);
    }

    private boolean validateForm(){

        boolean valid = true;
        _errors.clear();

        String price = _partsPriceSell.getText().toString();
        if(price.equals("") || !(Integer.parseInt(price) > 0)){
            _errors.add(getString(R.string.error_price_sell));
            valid = false;
        }

        price = _partsPriceBuy.getText().toString();
        if(price.equals("") || !(Integer.parseInt(price) > 0)){
            _errors.add(getString(R.string.error_price_buy));
            valid = false;
        }

        if(!(_categoryId > 0 && _carModelId > 0)){
            _errors.add(getString(R.string.error_cat_model));
            valid = false;
        }

        return valid;
    }

    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.d(ActivityHelper.TAG, "curr = " + mCurrentPhotoPath);
        return image;
    }

    private void takePhoto(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {}
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);

            if(mCurrentPhotoPath == null){
                Toast.makeText(this, R.string.error_photo, Toast.LENGTH_LONG).show();
                return;
            }

            File f = new File(mCurrentPhotoPath);
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            this.sendBroadcast(mediaScanIntent);

            if(_part_id > 0){
                _imagesDataDb.add(_part_id, contentUri.toString());
            }

        }
    }
}
