package ru.amobilestudio.autorazborassistant.asyncs;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.util.JsonReader;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import ru.amobilestudio.autorazborassistant.db.PartsDataDb;
import ru.amobilestudio.autorazborassistant.helpers.ActivityHelper;
import ru.amobilestudio.autorazborassistant.helpers.UserInfoHelper;

/**
 * Created by vetal on 17.07.14.
 */
public class MainAsync extends AsyncTask<Void, Void, Void> {

    private Context _context;
    private PartsDataDb _partsDataDb;
    private HashMap<String, String> _errors;

    private Cursor _cursor;

    public MainAsync(Context context) {
        _context = context;
        _errors = new HashMap<String, String>();
        _partsDataDb = new PartsDataDb(context);
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
                    Log.d(ActivityHelper.TAG, "part name = " + _cursor.getString(_cursor.getColumnIndex(PartsDataDb.COLUMN_PART_NAME)));
                    sendAndSavePart(_cursor);
                    //this.isCancelled();
                } while (_cursor.moveToNext());
            }
            ActivityHelper.debug("end iteration");
            try {
                Thread.sleep(3 * 60 * 1000);
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

//        MainActivity mainActivity = (MainActivity) _context;
//        SyncFragment syncFragment = (SyncFragment) mainActivity.mAppSectionsPagerAdapter.getItem(1);
//        syncFragment.updateList();
    }

    private void sendAndSavePart(Cursor c){

        try{
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

            //init array for sending
            nameValuePairs.add(new BasicNameValuePair("Part[price_sell]", c.getString(c.getColumnIndex(PartsDataDb.COLUMN_PART_PRICE_SELL))));
            nameValuePairs.add(new BasicNameValuePair("Part[price_buy]", c.getString(c.getColumnIndex(PartsDataDb.COLUMN_PART_PRICE_BUY))));
            nameValuePairs.add(new BasicNameValuePair("Part[comment]", c.getString(c.getColumnIndex(PartsDataDb.COLUMN_PART_COMMENT))));

            //selects
            //c.getString(c.getColumnIndex(PartsDataDb.COLUMN_PART_PRICE_SELL))
            //nameValuePairs.add(new BasicNameValuePair("Part[category_id]", (activity._categoryId != 0 ? activity._categoryId : "") + ""));

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

            Log.d(ActivityHelper.TAG, "nameValuePairs = " + nameValuePairs);

            //send POST
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(ActivityHelper.HOST + "api/savePart/id/" + c.getLong(c.getColumnIndex(PartsDataDb.COLUMN_PART_ID)));

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));

            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();

            //set state to part that sync start
            _partsDataDb.setStateToPart(c.getLong(c.getColumnIndex(BaseColumns._ID)), PartsDataDb.STATE_START_SYNC);

            //update list
            publishProgress();

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
}
