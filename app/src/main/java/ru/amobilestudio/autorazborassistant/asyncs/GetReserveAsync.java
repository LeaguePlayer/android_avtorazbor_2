package ru.amobilestudio.autorazborassistant.asyncs;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.JsonReader;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import ru.amobilestudio.autorazborassistant.app.R;
import ru.amobilestudio.autorazborassistant.db.PartsDataDb;
import ru.amobilestudio.autorazborassistant.helpers.ActivityHelper;
import ru.amobilestudio.autorazborassistant.helpers.AlertDialogHelper;
import ru.amobilestudio.autorazborassistant.helpers.UserInfoHelper;

/**
 * Created by vetal on 09.06.14.
 */
public class GetReserveAsync extends AsyncTask<Integer, Void, Void> {

    private Context _context;
    private ProgressDialog _progress;

    private ArrayList<String> _errors;
    private boolean _throwEx = false;

    private int _userId;

    //listener
    private OnTaskCompleted _listener;

    public GetReserveAsync(Context context) {

        _context = context;
        _errors = new ArrayList<String>();

        _progress = new ProgressDialog(context);
        _progress.setTitle(context.getString(R.string.wait_title));
        _progress.setMessage(context.getString(R.string.reserve_message));
        _progress.setCancelable(true);
        _progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        _userId = UserInfoHelper.getUserId(context);

        _listener = null;
    }

    public GetReserveAsync(Context context, OnTaskCompleted onTaskCompleted) {

        _context = context;
        _errors = new ArrayList<String>();

        _progress = new ProgressDialog(context);
        _progress.setTitle(context.getString(R.string.wait_title));
        _progress.setMessage(context.getString(R.string.reserve_message));
        _progress.setCancelable(true);
        _progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        _userId = UserInfoHelper.getUserId(context);

        ActivityHelper.debug(_userId + " --- ");

        _listener = onTaskCompleted;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        _progress.show();
    }

    @Override
    protected Void doInBackground(Integer... counts) {

        ActivityHelper.debug(_userId + " --- " + counts[0]);

        if(_userId > 0 && counts[0] > 0){
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(ActivityHelper.HOST + "api/reserve?count=" + counts[0] + "&" + "user_id=" + _userId + "&" + "secretKey=RazborApp");

            ActivityHelper.debug(_userId + " --- " + counts[0]);

            try {
                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httpGet);
                InputStream inputStream = response.getEntity().getContent();

                JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
                reader.beginObject();

                ActivityHelper.debug(inputStream.toString());

                PartsDataDb partsDataDb = new PartsDataDb(_context);

                while (reader.hasNext()){
                    String name = reader.nextName();
                    if (name.equals("errors")) {
                        reader.beginArray();
                        while(reader.hasNext()){
                            _errors.add(reader.nextString());
                        }
                        reader.endArray();
                    }else if (name.equals("data")) {
                        reader.beginArray();
                        while(reader.hasNext()){
                            reader.beginObject();
                            while (reader.hasNext()){
                                String dataName = reader.nextName();
                                //add reserve part
                                if(dataName.equals("id")){
                                    partsDataDb.addReservePart(reader.nextInt(), _userId);
                                }else
                                    reader.skipValue();
                            }
                            reader.endObject();
                        }
                        reader.endArray();
                    }else{
                        reader.skipValue();
                    }
                }
                reader.endObject();

            } catch (ClientProtocolException e) {
                _throwEx = true;
                ActivityHelper.debug("ClientProtocolException");
            } catch (IOException e) {
                _throwEx = true;
                ActivityHelper.debug("IOException");
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        _progress.dismiss();

        if(_throwEx)
            Toast.makeText(_context,
                    _context.getString(R.string.connection_error_title), Toast.LENGTH_LONG).show();

        if(!_errors.isEmpty())
            AlertDialogHelper.showAlertDialog(_context,
                    _context.getString(R.string.error_title), TextUtils.join("\n", _errors), true);
        if(_listener != null)
            _listener.onTaskCompleted();
    }
}
