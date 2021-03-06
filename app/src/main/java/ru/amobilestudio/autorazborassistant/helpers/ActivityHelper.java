package ru.amobilestudio.autorazborassistant.helpers;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import java.util.List;

/**
 * Created by vetal on 07.06.14.
 */
public class ActivityHelper {

    static final public boolean DEBUG_MODE = false;
    static final public String HOST = DEBUG_MODE ? "http://192.168.0.101:2000/" : "http://razbor72.tmweb.ru/";
    static final public String TAG = "razbor";

    private Context _context;

    public ActivityHelper(Context context) {
        _context = context;
    }

    public static void hideActionBar(Activity activity){
        if (Build.VERSION.SDK_INT < 16)
            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        else{
            View decorView = activity.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);

            ActionBar actionBar = activity.getActionBar();
            actionBar.hide();
        }
    }

    public static String getActivityName(Context context){
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        // get the info from the currently running task
        List< ActivityManager.RunningTaskInfo > taskInfo = am.getRunningTasks(1);
        //Log.d(TAG, "CURRENT Activity ::" + taskInfo.get(0).topActivity.getClassName());

        return taskInfo != null ? taskInfo.get(0).topActivity.getClassName() : "";
    }

    public static void debug(String msg){
        Log.d(TAG, msg);
    }
}
