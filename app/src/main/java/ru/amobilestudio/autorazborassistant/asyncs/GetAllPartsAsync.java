package ru.amobilestudio.autorazborassistant.asyncs;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.JsonToken;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
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
import ru.amobilestudio.autorazborassistant.helpers.UserInfoHelper;

/**
 * Created by vetal on 10.06.14.
 */
public class GetAllPartsAsync extends AsyncTask<Void, Void, Void> {

    private ArrayList<String> _errors;

    private Context _context;
    private ProgressDialog _progress;

    private PartsDataDb _sqLiteHelper;

    //all parts
//    private ArrayList<Part> _parts;

    public GetAllPartsAsync(Context context) {
        _context = context;
        _progress = new ProgressDialog(context);
        _progress.setTitle(context.getString(R.string.load_message));
        _progress.setMessage(context.getString(R.string.wait_title));
        _progress.setCancelable(true);
        _progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        _sqLiteHelper = new PartsDataDb(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        _progress.show();
    }

    @Override
    protected Void doInBackground(Void... voids) {

        String url = ActivityHelper.HOST + "api/allParts?user_id=" + UserInfoHelper.getUserId(_context);

        try {
            HttpClient httpclient = new DefaultHttpClient(); // for port 80 requests!
            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = httpclient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream inputStream = entity.getContent();

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
                    reader.beginObject();
                    while (reader.hasNext()){
                        String arr_name = reader.nextName();

                        if(arr_name.equals("parts")){
                            reader.beginArray();

                            while(reader.hasNext()){
                                reader.beginObject();

                                ContentValues part = new ContentValues();

                                while(reader.hasNext()){
                                    String part_field = reader.nextName();

                                    if(part_field.equals("id")){
                                        long id = reader.nextLong();
                                        part.put(PartsDataDb.COLUMN_PART_ID, id);
                                        ActivityHelper.debug(id + " part");
                                    }else if(part_field.equals("name")){
                                        part.put(PartsDataDb.COLUMN_PART_NAME, reader.nextString());
                                    }else if(part_field.equals("create_time")){
                                        part.put(PartsDataDb.COLUMN_PART_CREATE_DATE, reader.nextString());
                                    }else if(part_field.equals("update_time") && reader.peek() != JsonToken.NULL){
                                        part.put(PartsDataDb.COLUMN_PART_UPDATE_DATE, reader.nextString());
                                    }else if(part_field.equals("price_sell") && reader.peek() != JsonToken.NULL){
                                        part.put(PartsDataDb.COLUMN_PART_PRICE_SELL, reader.nextDouble());
                                    }else if(part_field.equals("price_buy")  && reader.peek() != JsonToken.NULL){
                                        part.put(PartsDataDb.COLUMN_PART_PRICE_BUY, reader.nextDouble());
                                    }else if(part_field.equals("comment") && reader.peek() != JsonToken.NULL){
                                        part.put(PartsDataDb.COLUMN_PART_COMMENT, reader.nextString());
                                    }else if(part_field.equals("category_id") && reader.peek() != JsonToken.NULL){
                                        part.put(PartsDataDb.COLUMN_PART_CATEGORY_ID, reader.nextInt());
                                    }else if(part_field.equals("car_model_id") && reader.peek() != JsonToken.NULL){
                                        part.put(PartsDataDb.COLUMN_PART_CAR_MODEL_ID, reader.nextInt());
                                    }else if(part_field.equals("location_id") && reader.peek() != JsonToken.NULL){
                                        part.put(PartsDataDb.COLUMN_PART_LOCATION_ID, reader.nextInt());
                                    }else if(part_field.equals("supplier_id") && reader.peek() != JsonToken.NULL){
                                        part.put(PartsDataDb.COLUMN_PART_SUPPLIER_ID, reader.nextInt());
                                    }else if(part_field.equals("used_car_id") && reader.peek() != JsonToken.NULL){
                                        part.put(PartsDataDb.COLUMN_PART_BU_ID, reader.nextInt());
                                    }else if(part_field.equals("status") && reader.peek() != JsonToken.NULL){
                                        part.put(PartsDataDb.COLUMN_PART_STATUS, reader.nextInt());
                                    }else if(part_field.equals("user_id") && reader.peek() != JsonToken.NULL){
                                        part.put(PartsDataDb.COLUMN_PART_USER_ID, reader.nextInt());
                                    }else{
                                        reader.skipValue();
                                    }
                                }
                                _sqLiteHelper.addPart(part);
                                reader.endObject();
                            }
                            reader.endArray();
                        }
                    }
                    reader.endObject();
                }else{
                    reader.skipValue();
                }
            }
            reader.endObject();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void Void) {
        super.onPostExecute(Void);

        _progress.dismiss();

        /*MainActivity activity = (MainActivity) _context;
        ListView listView = (ListView) activity.findViewById(android.R.id.list);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(_context, AddPartActivity.class);
                intent.putExtra("Part", _parts.get(i));

                _context.startActivity(intent);
            }
        });

        ListAdapter listAdapter = new ListAdapter(_context, R.layout.activity_main, _parts);

        TextView emptyText = (TextView) activity.findViewById(R.id.empty_text);
        listView.setEmptyView(emptyText);

        listView.setAdapter(listAdapter);*/
    }

    /*static class ViewHolder {
        public TextView nameView;
        public TextView dateView;
    }

    //custom adapter
    private class ListAdapter extends ArrayAdapter<Part>{

        public ListAdapter(Context context, int resource, ArrayList<Part> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder;

            View rowView = convertView;
            if (rowView == null) {
                LayoutInflater inflater = (LayoutInflater) _context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.part_item, null, true);
                holder = new ViewHolder();
                holder.nameView = (TextView) rowView.findViewById(R.id.part_name);
                holder.dateView = (TextView) rowView.findViewById(R.id.part_date);
                rowView.setTag(holder);
            } else {
                holder = (ViewHolder) rowView.getTag();
            }

            Part p = _parts.get(position);
            holder.nameView.setText(p.toString());
            holder.dateView.setText(p.get_date());

            return rowView;
        }

        @Override
        public int getCount() {
            return _parts.size();
        }

        @Override
        public Part getItem(int position) {
            return _parts.get(position);
        }
    }*/
}