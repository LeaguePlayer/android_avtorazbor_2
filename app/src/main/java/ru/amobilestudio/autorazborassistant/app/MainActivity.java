package ru.amobilestudio.autorazborassistant.app;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.TextView;

import java.lang.reflect.Field;

import ru.amobilestudio.autorazborassistant.asyncs.GetSelectsDataAsync;
import ru.amobilestudio.autorazborassistant.fragments.ReservedFragment;
import ru.amobilestudio.autorazborassistant.fragments.SyncFragment;
import ru.amobilestudio.autorazborassistant.helpers.ConnectionHelper;
import ru.amobilestudio.autorazborassistant.helpers.UserInfoHelper;


public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

    AppSectionsPagerAdapter mAppSectionsPagerAdapter;

    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO: delete this lines
        //------------------------------------------------------
        //deleteDatabase(DbSQLiteHelper.DATABASE_NAME);
        /*SharedPreferences settings = getSharedPreferences(DataFieldsAsync.DB_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putBoolean("isDb", false);
        editor.commit();*/
        //------------------------------------------------------

        getOverflowMenu();

        TextView helloText = (TextView) findViewById(R.id.helloUser);
        helloText.setVisibility(View.GONE);

        setTitle(UserInfoHelper.getUserFio(this));

        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());

        final ActionBar actionBar = getActionBar();

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        /*GetReserveAsync reserveAsync = new GetReserveAsync(this);
        reserveAsync.execute(2);*/

        //add tabs
        actionBar.addTab(actionBar.newTab().setText(getString(R.string.tab1)).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(getString(R.string.tab2)).setTabListener(this));
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action buttons
        switch (item.getItemId()){
            case R.id.action_update_data:
                if(ConnectionHelper.checkNetworkConnection(this)){
                    GetSelectsDataAsync dataAsync = new GetSelectsDataAsync(this);
                    dataAsync.execute();
                }
                break;
            case R.id.action_sync:
                if(ConnectionHelper.checkNetworkConnection(this)){

                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    // The first section of the app is the most interesting -- it offers
                    // a launchpad into the other demonstrations in this example application.
                    return new ReservedFragment();
                default:
                    // The other sections of the app are dummy placeholders.
                    Fragment fragment = new SyncFragment();
                    /*Bundle args = new Bundle();
                    args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, i + 1);
                    fragment.setArguments(args);*/
                    return fragment;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }
}
