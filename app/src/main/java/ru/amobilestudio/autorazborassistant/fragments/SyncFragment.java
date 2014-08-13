package ru.amobilestudio.autorazborassistant.fragments;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import ru.amobilestudio.autorazborassistant.adapters.PartsSyncCursorAdapter;
import ru.amobilestudio.autorazborassistant.app.AddPartActivity;
import ru.amobilestudio.autorazborassistant.app.R;
import ru.amobilestudio.autorazborassistant.db.PartsDataDb;
import ru.amobilestudio.autorazborassistant.helpers.UserInfoHelper;

/**
 * Created by vetal on 10.06.14.
 */
public class SyncFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener{

    private PartsSyncCursorAdapter _cursorAdapter;
    private PartsDataDb _partsDataDb;
    private ListView _listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sync, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        _listView = getListView();
        _listView.setEmptyView(getView().findViewById(R.id.empty_text));
        _partsDataDb = new PartsDataDb(getActivity());

        String[] from = new String[] {PartsDataDb.COLUMN_PART_ID, PartsDataDb.COLUMN_PART_NAME, PartsDataDb.COLUMN_PART_UPDATE_DATE};
        int[] to = new int[] {R.id.part_id, R.id.part_name, R.id.part_update};

        _cursorAdapter = new PartsSyncCursorAdapter(getActivity(), R.layout.part_sync_item, null, from, to, 0);
        _listView.setOnItemClickListener(this);
        _listView.setAdapter(_cursorAdapter);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new SyncPartsCursorLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        _cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(getActivity(), AddPartActivity.class);
        intent.putExtra("part_id", l);

        getActivity().startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateList();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void updateList(){
        _cursorAdapter.swapCursor(_partsDataDb.fetchForSyncParts(UserInfoHelper.getUserId(getActivity())));
        _listView.invalidateViews();
        getActivity().getActionBar().getTabAt(1).setText(getString(R.string.tab2) +
                " (" + _cursorAdapter.getCount() +")");
    }

    static class SyncPartsCursorLoader extends CursorLoader {

        private Context _context;

        public SyncPartsCursorLoader(Context context) {
            super(context);
            _context = context;
        }

        @Override
        public Cursor loadInBackground() {
            PartsDataDb dataDb = new PartsDataDb(_context);

            return dataDb.fetchForSyncParts(UserInfoHelper.getUserId(_context));
        }
    }
}
