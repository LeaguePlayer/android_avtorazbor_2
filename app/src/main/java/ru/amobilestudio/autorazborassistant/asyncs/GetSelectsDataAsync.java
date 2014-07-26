package ru.amobilestudio.autorazborassistant.asyncs;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import ru.amobilestudio.autorazborassistant.app.R;
import ru.amobilestudio.autorazborassistant.db.SelectsDataDb;
import ru.amobilestudio.autorazborassistant.helpers.ActivityHelper;

/**
 * Created by vetal on 10.06.14.
 */
public class GetSelectsDataAsync extends AsyncTask<Void, Integer, Void> {

    private Context _context;
    private ProgressDialog _progress;

    private boolean _throwEx = false;

    public GetSelectsDataAsync(Context context) {

        _context = context;
        _progress = new ProgressDialog(context);
        _progress.setTitle(context.getString(R.string.wait_title));
        _progress.setMessage(context.getString(R.string.load_message));
        _progress.setCancelable(false);
        _progress.setProgress(0);
        _progress.setMax(100);
        _progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        _progress.show();
    }

    @Override
    protected Void doInBackground(Void... voids) {

        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(ActivityHelper.HOST + "api/fieldsData");
        SelectsDataDb dictionarySql = new SelectsDataDb(_context);

        try {
            HttpResponse response = httpclient.execute(httppost);
            InputStream inputStream = response.getEntity().getContent();

            JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));

            reader.beginObject();

            publishProgress(0);

            while (reader.hasNext()){
                String name = reader.nextName();
                if (name.equals("data")) {
                    reader.beginObject();
                    while(reader.hasNext()){
                        String type = reader.nextName();

                        if(type.equals("categories")) {
                            saveDataField(reader, dictionarySql, SelectsDataDb.TABLE_NAME_CATEGORIES);
                            publishProgress((int) ((1 / (float) 5) * 100));
                        }else if(type.equals("car_models")) {
                            saveDataField(reader, dictionarySql, SelectsDataDb.TABLE_NAME_CAR_MODELS);
                            publishProgress((int) ((2 / (float) 5) * 100));
                        }else if(type.equals("locations")){
                            saveDataField(reader, dictionarySql, SelectsDataDb.TABLE_NAME_LOCATIONS);
                            publishProgress((int) ((3 / (float) 5) * 100));
                        }else if(type.equals("suppliers")) {
                            saveDataField(reader, dictionarySql, SelectsDataDb.TABLE_NAME_SUPPLIERS);
                            publishProgress((int) ((4 / (float) 5) * 100));
                        }else if(type.equals("bu_cars")) {
                            saveDataField(reader, dictionarySql, SelectsDataDb.TABLE_NAME_BU_CARS);
                            publishProgress((int) ((5 / (float) 5) * 100));
                        }else reader.skipValue();
                    }
                    reader.endObject();
                }else
                    reader.skipValue();
            }

            reader.endObject();

        } catch (IOException e) {
            e.printStackTrace();
            _throwEx = true;
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        _progress.setProgress(values[0]);
    }

    public void saveDataField(JsonReader reader, SelectsDataDb db, String tableName) throws IOException {
        reader.beginArray();

        while (reader.hasNext()){
            reader.beginObject();
            ContentValues values = new ContentValues();

            while (reader.hasNext()){
                String fieldName = reader.nextName();

                if(fieldName.equals("id")){
                    values.put(SelectsDataDb.COLUMN_ID_VALUE, reader.nextString());
                }
                else if(fieldName.equals("name")){
                    values.put(SelectsDataDb.COLUMN_NAME_VALUE, reader.nextString());
                }else
                    reader.skipValue();
            }
            db.addItem(tableName, values);
            reader.endObject();
        }

        reader.endArray();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        _progress.dismiss();

        if(_throwEx)
            Toast.makeText(_context,
                    _context.getString(R.string.connection_error_title), Toast.LENGTH_LONG).show();
    }
}