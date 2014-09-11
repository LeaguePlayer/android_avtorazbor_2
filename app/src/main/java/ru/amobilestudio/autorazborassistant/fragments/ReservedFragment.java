package ru.amobilestudio.autorazborassistant.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import ru.amobilestudio.autorazborassistant.app.AddPartActivity;
import ru.amobilestudio.autorazborassistant.app.R;
import ru.amobilestudio.autorazborassistant.asyncs.GetReserveAsync;
import ru.amobilestudio.autorazborassistant.asyncs.OnTaskCompleted;
import ru.amobilestudio.autorazborassistant.db.PartsDataDb;
import ru.amobilestudio.autorazborassistant.helpers.ConnectionHelper;
import ru.amobilestudio.autorazborassistant.helpers.UserInfoHelper;

/**
 * Created by vetal on 09.06.14.
 */
public class ReservedFragment extends ListFragment implements View.OnClickListener,
        LoaderManager.LoaderCallbacks<Cursor>, OnTaskCompleted, AdapterView.OnItemClickListener {

    private SimpleCursorAdapter _cursorAdapter;
    private PartsDataDb _partsDataDb;
    private ListView _listView;

    final private OnTaskCompleted _taskCompleted = this;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_reserved, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        _listView = getListView();
        _listView.setEmptyView(getView().findViewById(R.id.empty_text));
        _partsDataDb = new PartsDataDb(getActivity());

        String[] from = new String[] {PartsDataDb.COLUMN_PART_ID, PartsDataDb.COLUMN_PART_NAME};
        int[] to = new int[] {R.id.part_id, R.id.part_name};

        _cursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.part_item, null, from, to, 0);
        _listView.setOnItemClickListener(this);
        _listView.setAdapter(_cursorAdapter);

        //set long click
        /*_listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                ActivityHelper.debug("i = " + i + "; l = " + l);
                return false;
            }
        });*/

        getLoaderManager().initLoader(0, null, this);

        Button button = (Button) getView().findViewById(R.id.plus10);
        button.setOnClickListener(this);

        button = (Button) getView().findViewById(R.id.plus25);
        button.setOnClickListener(this);

        button = (Button) getView().findViewById(R.id.plus50);
        button.setOnClickListener(this);

        button = (Button) getView().findViewById(R.id.plusOther);
        button.setOnClickListener(this);
    }

    /*@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }*/

    @Override
    public void onClick(View view) {

        if(ConnectionHelper.checkNetworkConnection(getActivity())){

            switch (view.getId()){
                case R.id.plus10:
                    showDialog(getActivity(), 10);
                    break;
                case R.id.plus25:
                    showDialog(getActivity(), 25);
                    break;
                case R.id.plus50:
                    showDialog(getActivity(), 50);
                    break;
                case R.id.plusOther:
                    showDialogWithInput(getActivity());
                    break;
            }
        }
    }

    private void showDialog(Activity activity, final int count){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(getString(R.string.attention_title));
        builder.setMessage(getString(R.string.confirm_sure) + " " + count + " деталей?");

        // Set up the buttons
        builder.setPositiveButton(getString(R.string.confirm_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                GetReserveAsync dataAsync = new GetReserveAsync(getActivity(), _taskCompleted);
                dataAsync.execute(count);
            }
        });

        builder.setNegativeButton(getString(R.string.confirm_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void showDialogWithInput(Activity activity){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(getString(R.string.reserve_set_count));

        // Set up the input
        final EditText input = new EditText(activity);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(getString(R.string.confirm_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

                int count = Integer.parseInt(input.getText().toString());
                GetReserveAsync dataAsync = new GetReserveAsync(getActivity(), _taskCompleted);
                dataAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, count);
            }
        });

        builder.setNegativeButton(getString(R.string.confirm_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void updateList(){
        _cursorAdapter.swapCursor(_partsDataDb.fetchAllReservedParts(UserInfoHelper.getUserId(getActivity())));
        _listView.invalidateViews();
        getActivity().getActionBar().getTabAt(0).setText(getString(R.string.tab1) +
                " (" + _cursorAdapter.getCount() +")");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new ReserveCursorLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        _cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onTaskCompleted() {
        updateList();
    }

    @Override
    public void onResume() {
        super.onResume();

        updateList();
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(getActivity(), AddPartActivity.class);
        intent.putExtra("part_id", l);

        getActivity().startActivity(intent);
    }

    static class ReserveCursorLoader extends CursorLoader {

        private Context _context;

        public ReserveCursorLoader(Context context) {
            super(context);
            _context = context;
        }

        @Override
        public Cursor loadInBackground() {
            PartsDataDb dataDb = new PartsDataDb(_context);

            return dataDb.fetchAllReservedParts(UserInfoHelper.getUserId(_context));
        }
    }
}
