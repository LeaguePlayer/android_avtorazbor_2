package ru.amobilestudio.autorazborassistant.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

import ru.amobilestudio.autorazborassistant.db.ImagesDataDb;
import ru.amobilestudio.autorazborassistant.fragments.ImageFragment;

/**
 * Created by vetal on 15.06.14.
 */
public class GalleryActivity extends FragmentActivity {

    private ViewPager _viewPager;
    private ImagesDataDb _imagesDataDb;
    private ImageAdapter _imageAdapter;
    private ArrayList<ImagesDataDb.Image> _list;

    private long _part_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        _list = new ArrayList<ImagesDataDb.Image>();
        _imagesDataDb = new ImagesDataDb(this);

        //get paths images
        Intent intent = getIntent();

        _part_id = intent.getLongExtra("part_id", 0);

        if(_part_id > 0){
            _list = _imagesDataDb.fetchListImages(_part_id);
        }

        _imageAdapter = new ImageAdapter(getSupportFragmentManager());

        _viewPager = (ViewPager) findViewById(R.id.pager);
        _viewPager.setAdapter(_imageAdapter);

        Button deleteButton = (Button) findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = _viewPager.getCurrentItem();
                _imagesDataDb.delete(_list.get(pos).get_id());
                reload();
            }
        });
    }

    public void reload(){

        _list = new ArrayList<ImagesDataDb.Image>();
        _list = _imagesDataDb.fetchListImages(_part_id);

        if(_list.isEmpty())
            finish();

        _imageAdapter.notifyDataSetChanged();
        _viewPager.invalidate();
    }

    private class ImageAdapter extends FragmentStatePagerAdapter {

        public ImageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return _list.size();
        }

        @Override
        public Fragment getItem(int position) {
            return new ImageFragment(getApplicationContext(), _list.get(position));
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }
    }
}
