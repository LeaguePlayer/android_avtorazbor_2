package ru.amobilestudio.autorazborassistant.app;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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

import ru.amobilestudio.autorazborassistant.asyncs.GetAllPartsAsync;
import ru.amobilestudio.autorazborassistant.asyncs.GetSelectsDataAsync;
import ru.amobilestudio.autorazborassistant.asyncs.OnTaskCompleted;
import ru.amobilestudio.autorazborassistant.fragments.ReservedFragment;
import ru.amobilestudio.autorazborassistant.fragments.SyncFragment;
import ru.amobilestudio.autorazborassistant.helpers.ConnectionHelper;
import ru.amobilestudio.autorazborassistant.helpers.UserInfoHelper;
import ru.amobilestudio.autorazborassistant.receivers.NetworkChangeReceiver;


public class MainActivity extends FragmentActivity implements ActionBar.TabListener, OnTaskCompleted {

    private NetworkChangeReceiver _networkChangeReceiver;

    public AppSectionsPagerAdapter mAppSectionsPagerAdapter;

    private ViewPager _viewPager;

    final private OnTaskCompleted _taskCompleted = this;
    final private Context _context = this;

//    private boolean isUpdateReserve = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(_networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

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

        _viewPager = (ViewPager) findViewById(R.id.pager);
        _viewPager.setAdapter(mAppSectionsPagerAdapter);

        _viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        //add tabs
        actionBar.addTab(actionBar.newTab().setText(getString(R.string.tab1)).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(getString(R.string.tab2)).setTabListener(this));

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = getIntent();
                int isFromLogin = intent.getIntExtra("from_login", 0);

                if(isFromLogin == 1){
                    GetAllPartsAsync allPartsAsync = new GetAllPartsAsync(_context, _taskCompleted);
                    allPartsAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        }, 2000);
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
    protected void onDestroy() {
        super.onDestroy();
        _networkChangeReceiver.cancelMainAsync();
        unregisterReceiver(_networkChangeReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                    dataAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                break;
            case R.id.action_sync:
                if(ConnectionHelper.checkNetworkConnection(this)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(getString(R.string.attention_title));
                    builder.setMessage(getString(R.string.sync_message));

                    // Set up the buttons
                    builder.setPositiveButton(getString(R.string.confirm_text), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            GetAllPartsAsync allPartsAsync = new GetAllPartsAsync(_context, _taskCompleted);
                            allPartsAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
                break;
            case R.id.action_logout_user:
                logOut();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    //when all parts uploads than update fragments lists
    @Override
    public void onTaskCompleted() {
        String reserveFragmentName = makeFragmentName(_viewPager.getId(), 0);
        String syncFragmentName = makeFragmentName(_viewPager.getId(), 1);

        Fragment reservedFragment = getSupportFragmentManager().findFragmentByTag(reserveFragmentName);
        Fragment syncFragment = getSupportFragmentManager().findFragmentByTag(syncFragmentName);

        if(reservedFragment != null && syncFragment != null &&
                reservedFragment.isResumed() && syncFragment.isResumed()){
            ((ReservedFragment) reservedFragment).updateList();
            ((SyncFragment) syncFragment).updateList();
        }
    }

    public ViewPager getViewPager(){
        return _viewPager;
    }

    private static String makeFragmentName(int viewId, int index) {
        return "android:switcher:" + viewId + ":" + index;
    }

    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return new ReservedFragment();
                default:
                    return new SyncFragment();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        _viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    private void logOut(){
        UserInfoHelper.logoutUser(this);

        finish();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
