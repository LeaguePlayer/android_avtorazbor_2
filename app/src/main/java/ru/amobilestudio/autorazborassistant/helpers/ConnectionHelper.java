package ru.amobilestudio.autorazborassistant.helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import ru.amobilestudio.autorazborassistant.app.R;

/**
 * Created by vetal on 07.06.14.
 */
public class ConnectionHelper {

    public static boolean checkNetworkConnection(Context context)
    {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if(!isConnected){

            //show error dialog
            AlertDialogHelper.showAlertDialog(context, R.string.connection_error_title, R.string.connection_error_text, true);
        }

        return isConnected;
    }
}
