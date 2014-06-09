package ru.amobilestudio.autorazborassistant.asyncs;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.Log;

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
import java.util.ArrayList;
import java.util.List;

import ru.amobilestudio.autorazborassistant.app.MainActivity;
import ru.amobilestudio.autorazborassistant.app.R;
import ru.amobilestudio.autorazborassistant.helpers.ActivityHelper;
import ru.amobilestudio.autorazborassistant.helpers.AlertDialogHelper;
import ru.amobilestudio.autorazborassistant.helpers.UserInfoHelper;

/**
 * Created by vetal on 09.06.14.
 */
public class LoginAsync extends AsyncTask<String, Void, Void> {
    private ArrayList<String> _errors;
    private Context _context;

    private int _user_id;
    private String _user_fio;

    private ProgressDialog _progress;

    public LoginAsync(Context context){
        _context = context;
        _errors = new ArrayList<String>();

        _progress = new ProgressDialog(context);
        _progress.setTitle(context.getString(R.string.wait_title));
        _progress.setMessage(context.getString(R.string.login_title));
        _progress.setCancelable(true);
        _progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        _progress.show();
    }

    @Override
    protected Void doInBackground(String... strings) {
        String login = strings[0];
        String pass = strings[1];

        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(ActivityHelper.HOST + "api/auth");

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("username", login));
            nameValuePairs.add(new BasicNameValuePair("pass", pass));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);

            InputStream inputStream = response.getEntity().getContent();
            //this.errors = JSON.getErrorsFromJSON(inputStream);

            JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));

            reader.beginObject();

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
                            //get User Id
                            if(dataName.equals("user")){
                                reader.beginObject();

                                while (reader.hasNext()){
                                    String nUser = reader.nextName();

                                    if(nUser.equals("id")) _user_id = reader.nextInt();
                                    else if(nUser.equals("fio")) _user_fio = reader.nextString();
                                    else reader.skipValue();
                                }

                                reader.endObject();
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

        } catch (ClientProtocolException e) {
            Log.d(ActivityHelper.TAG, " catch ClientProtocolException");
        } catch (IOException e) {
            Log.d(ActivityHelper.TAG, " catch IOException");
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        _progress.dismiss();

        if(_errors.isEmpty()){

            UserInfoHelper.rememberUser(_context, _user_id, _user_fio);

            Intent intent = new Intent(_context, MainActivity.class);
            _context.startActivity(intent);
        }else
            AlertDialogHelper.showAlertDialog(_context, _context.getString(R.string.error_title), TextUtils.join("\n", _errors), true);
    }
}