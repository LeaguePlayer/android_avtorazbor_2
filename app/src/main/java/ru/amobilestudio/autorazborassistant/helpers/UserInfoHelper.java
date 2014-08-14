package ru.amobilestudio.autorazborassistant.helpers;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by vetal on 09.06.14.
 */
public class UserInfoHelper {

    public static final String USER_INFO_PREFS = "UserInfoPrefs";

    public static boolean isLogin(Context context){
        SharedPreferences user_info = context.getSharedPreferences(USER_INFO_PREFS, Context.MODE_PRIVATE);

        return user_info.getBoolean("isLogin", false);
    }

    public static void rememberUser(Context context, int id, String fio){
        //TODO: save date login User (Session)
        SharedPreferences settings = context.getSharedPreferences(USER_INFO_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putInt("user_id", id);
        editor.putString("user_fio", fio);
        editor.putBoolean("isLogin", true);

        editor.commit();
    }

    public static void logoutUser(Context context){
        SharedPreferences settings = context.getSharedPreferences(USER_INFO_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putInt("user_id", 0);
        editor.putString("user_fio", "");
        editor.putBoolean("isLogin", false);

        editor.commit();
    }

    public static String getUserFio(Context context){
        SharedPreferences user_info = context.getSharedPreferences(UserInfoHelper.USER_INFO_PREFS, Context.MODE_PRIVATE);

        return user_info.getString("user_fio", "");
    }

    public static int getUserId(Context context){
        SharedPreferences user_info = context.getSharedPreferences(UserInfoHelper.USER_INFO_PREFS, Context.MODE_PRIVATE);

        return user_info.getInt("user_id", 0);
    }
}
