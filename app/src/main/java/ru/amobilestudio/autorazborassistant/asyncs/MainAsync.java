package ru.amobilestudio.autorazborassistant.asyncs;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.util.JsonReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import ru.amobilestudio.autorazborassistant.app.MainActivity;
import ru.amobilestudio.autorazborassistant.app.R;
import ru.amobilestudio.autorazborassistant.db.ImagesDataDb;
import ru.amobilestudio.autorazborassistant.db.PartsDataDb;
import ru.amobilestudio.autorazborassistant.fragments.ImageFragment;
import ru.amobilestudio.autorazborassistant.fragments.SyncFragment;
import ru.amobilestudio.autorazborassistant.helpers.ActivityHelper;
import ru.amobilestudio.autorazborassistant.helpers.UserInfoHelper;

/**
 * Created by vetal on 17.07.14.
 */
public class MainAsync extends AsyncTask<Void, Void, Void> {

    private Context _context;
    private PartsDataDb _partsDataDb;
    private ImagesDataDb _imagesDataDb;
    private HashMap<String, String> _errors;

    private Cursor _cursor;
    private Cursor _imagesCursor;

    public MainAsync(Context context) {
        _context = context;
        _errors = new HashMap<String, String>();
        _partsDataDb = new PartsDataDb(context);
        _imagesDataDb = new ImagesDataDb(context);
    }

    @Override
    protected Void doInBackground(Void... voids) {

        //infinity loop
        while(!this.isCancelled()){
            ActivityHelper.debug("start iteration");
            PartsDataDb partsDataDb = new PartsDataDb(_context);
            _cursor = partsDataDb.fetchAllNoReserved(UserInfoHelper.getUserId(_context));

            if(_cursor != null && _cursor.moveToFirst()){
                do {
                    sendAndSavePart(_cursor);
                } while (_cursor.moveToNext());
            }
            ActivityHelper.debug("end iteration");
            try {
                Thread.sleep(1 * 60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();

        if(_cursor != null && _cursor.getCount() > 0)
            saveState(_cursor);
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);

        MainActivity mainActivity = (MainActivity) _context;
        Fragment page = mainActivity.getSupportFragmentManager().findFragmentByTag(
                "android:switcher:" + R.id.pager + ":" + mainActivity.getViewPager().getCurrentItem());

        if(page != null){
            if(mainActivity.getViewPager().getCurrentItem() == 1) ((SyncFragment) page).updateList();
        }
    }

    private void sendAndSavePart(Cursor c){

        //set state to part that sync start
        _partsDataDb.setStateToPart(c.getLong(c.getColumnIndex(BaseColumns._ID)), PartsDataDb.STATE_START_SYNC);

        //update list
        publishProgress();

        //first images
        if(!sendPartImages(c.getLong(c.getColumnIndex(BaseColumns._ID)), c.getLong(c.getColumnIndex(PartsDataDb.COLUMN_PART_ID))))
            return;

        _errors.clear();

        try{
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

            //init array for sending
            nameValuePairs.add(new BasicNameValuePair("Part[price_sell]", c.getString(c.getColumnIndex(PartsDataDb.COLUMN_PART_PRICE_SELL))));
            nameValuePairs.add(new BasicNameValuePair("Part[price_buy]", c.getString(c.getColumnIndex(PartsDataDb.COLUMN_PART_PRICE_BUY))));
            nameValuePairs.add(new BasicNameValuePair("Part[comment]", c.getString(c.getColumnIndex(PartsDataDb.COLUMN_PART_COMMENT))));

            long val = c.getLong(c.getColumnIndex(PartsDataDb.COLUMN_PART_CATEGORY_ID));
            String strVal = val != 0 ? val + "" : "";
            nameValuePairs.add(new BasicNameValuePair("Part[category_id]", strVal));

            val = c.getLong(c.getColumnIndex(PartsDataDb.COLUMN_PART_CAR_MODEL_ID));
            strVal = val != 0 ? val + "" : "";
            nameValuePairs.add(new BasicNameValuePair("Part[car_model_id]", strVal));

            val = c.getLong(c.getColumnIndex(PartsDataDb.COLUMN_PART_LOCATION_ID));
            strVal = val != 0 ? val + "" : "";
            nameValuePairs.add(new BasicNameValuePair("Part[location_id]", strVal));

            val = c.getLong(c.getColumnIndex(PartsDataDb.COLUMN_PART_SUPPLIER_ID));
            strVal = val != 0 ? val + "" : "";
            nameValuePairs.add(new BasicNameValuePair("Part[supplier_id]", strVal));

            val = c.getLong(c.getColumnIndex(PartsDataDb.COLUMN_PART_STATUS));
            strVal = val != 0 ? val + "" : "";
            nameValuePairs.add(new BasicNameValuePair("Part[status]", strVal));

            val = c.getLong(c.getColumnIndex(PartsDataDb.COLUMN_PART_BU_ID));
            strVal = val != 0 ? val + "" : "";
            nameValuePairs.add(new BasicNameValuePair("UsedCar", strVal));

            //dates (format: yyyy-MM-dd HH:mm:ss)
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            //create_time
            val = c.getLong(c.getColumnIndex(PartsDataDb.COLUMN_PART_CREATE_DATE));
            if(val != 0){
                Date date = new Date(val);
                nameValuePairs.add(new BasicNameValuePair("Part[create_time]", dateFormat.format(date)));
            }

            //update_time
            val = c.getLong(c.getColumnIndex(PartsDataDb.COLUMN_PART_UPDATE_DATE));
            if(val != 0){
                Date date = new Date(val);
                nameValuePairs.add(new BasicNameValuePair("Part[update_time]", dateFormat.format(date)));
            }

            //send POST
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(ActivityHelper.HOST + "api/savePart/id/" + c.getLong(c.getColumnIndex(PartsDataDb.COLUMN_PART_ID)));

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));

            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();

            //parse JSON
            InputStream inputStream = entity.getContent();

            JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
            reader.beginObject();

            while (reader.hasNext()){
                String name = reader.nextName();
                if (name.equals("errors")) {
                    reader.beginArray();
                    while (reader.hasNext()){
                        reader.beginObject();
                        while(reader.hasNext()){
                            String field_name = reader.nextName();
                            if(field_name.equals("name")){
                                reader.beginArray();
                                while (reader.hasNext()){
                                    _errors.put(field_name, reader.nextString());
                                }
                                reader.endArray();
                            }else if(field_name.equals("price_sell")){
                                reader.beginArray();
                                while (reader.hasNext()){
                                    _errors.put(field_name, reader.nextString());
                                }
                                reader.endArray();
                            }else if(field_name.equals("price_buy")){
                                reader.beginArray();
                                while (reader.hasNext()){
                                    _errors.put(field_name, reader.nextString());
                                }
                                reader.endArray();
                            }else{
                                reader.skipValue();
                            }
                        }
                        reader.endObject();
                    }
                    reader.endArray();
                }else{
                    reader.skipValue();
                }
            }
            reader.endObject();

            if(_errors.isEmpty()){

                //if publish remove part from db device
                if(c.getInt(c.getColumnIndex(PartsDataDb.COLUMN_PART_STATUS)) == PartsDataDb.STATUS_PUBLISH){
                    _partsDataDb.deletePart(c.getLong(c.getColumnIndex(BaseColumns._ID)));

                    //update list
                    publishProgress();
                    return;
                }

                _partsDataDb.setStateToPart(c.getLong(c.getColumnIndex(BaseColumns._ID)), PartsDataDb.STATE_SUCCESS_SYNC);

                //update list
                publishProgress();
            }
        }catch (ClientProtocolException e){
            e.printStackTrace();
            ActivityHelper.debug("ClientProtocolException");
            saveState(c);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            ActivityHelper.debug("UnsupportedEncodingException");
            saveState(c);
        } catch (IOException e) {
            e.printStackTrace();
            ActivityHelper.debug("IOException");
            saveState(c);
        }
    }

    //if break connection or some error
    private void saveState(Cursor c){
        long id = c.getLong(c.getColumnIndex(BaseColumns._ID));
        if(_partsDataDb.getStatePart(id) == PartsDataDb.STATE_START_SYNC || _partsDataDb.getStatePart(id) == PartsDataDb.STATE_ALLOW_SYNC){
            _partsDataDb.setStateToPart(id, PartsDataDb.STATE_ERROR_SYNC);
            publishProgress();
        }
    }

    private boolean sendPartImages(long partId, long webId){

        _imagesCursor = _imagesDataDb.fetchReadyToSyncImages(partId);
        if(_imagesCursor.getCount() == 0)
            return true;

        if(_imagesCursor != null && _imagesCursor.moveToFirst()){
            do {
                try{
                    _imagesDataDb.setState(_imagesCursor.getLong(_imagesCursor.getColumnIndex(BaseColumns._ID)), ImagesDataDb.STATE_START_SYNC);

                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(ActivityHelper.HOST + "api/addImage/id/" + webId);

                    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

                    URI uri = new URI(Uri.parse(_imagesCursor.getString(_imagesCursor.getColumnIndex(ImagesDataDb.COLUMN_IMAGES_PATH))).toString());
                    File image = new File(uri);

                    image = rotateAndResizeImage(image);

                    FileBody fb = new FileBody(image);
                    builder.addPart("Image", fb);

                    final HttpEntity entity = builder.build();
                    httppost.setEntity(entity);

                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity_res = response.getEntity();

                    //parse JSON
                    InputStream inputStream = entity_res.getContent();

                    JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
                    reader.beginObject();

                    while (reader.hasNext()){
                        String name = reader.nextName();
                        if (name.equals("errors")) {
                            reader.beginArray();
                            while (reader.hasNext()){
                                _errors.put("error", reader.nextString());
                            }
                            reader.endArray();
                        }else{
                            reader.skipValue();
                        }
                    }
                    reader.endObject();

                    if(_errors.isEmpty()){
                        _imagesDataDb.setState(_imagesCursor.getLong(_imagesCursor.getColumnIndex(BaseColumns._ID)),
                                ImagesDataDb.STATE_SUCCESS_SYNC);
                        return true;
                    }else{
                        _imagesDataDb.setState(_imagesCursor.getLong(_imagesCursor.getColumnIndex(BaseColumns._ID)),
                                ImagesDataDb.STATE_ERROR_SYNC);
                    }

                }catch (URISyntaxException e) {
                    e.printStackTrace();
                    saveImageState(_imagesCursor);
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                    saveImageState(_imagesCursor);
                } catch (IOException e) {
                    e.printStackTrace();
                    saveImageState(_imagesCursor);
                }
            }while (_imagesCursor.moveToNext());
        }

        return false;
    }

    private File rotateAndResizeImage(File image){
        File newFile = new File(image.getPath());

        Bitmap bitmap = ImageFragment.decodeSampledBitmapFromResource(image.getPath(), 1200, 800);

        //rotate image
        try {
            ExifInterface exif = new ExifInterface(image.getPath());
            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            int rotationInDegrees = ImageFragment.exifToDegrees(rotation);
            Matrix matrix = new Matrix();
            if (rotation != 0f) {matrix.preRotate(rotationInDegrees);}

            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            FileOutputStream fOut = new FileOutputStream(newFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fOut);

            fOut.flush();
            fOut.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        if(newFile != null)
            return newFile;

        return image;
    }

    private void saveImageState(Cursor c){
        _imagesDataDb.setState(c.getLong(c.getColumnIndex(BaseColumns._ID)), ImagesDataDb.STATE_ERROR_SYNC);
    }
}
