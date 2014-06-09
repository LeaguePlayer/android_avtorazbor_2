package ru.amobilestudio.autorazborassistant.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import ru.amobilestudio.autorazborassistant.app.R;

/**
 * Created by vetal on 07.06.14.
 */
public class AlertDialogHelper {

    public static void showAlertDialog(Context context, String title, String message, boolean showOk){
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);

        dialog.setTitle(title);
        dialog.setMessage(message);

        if(showOk){
            dialog.setPositiveButton(R.string.confirm_text, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {}
            });
        }

        dialog.show();
    }

    public static void showAlertDialog(Context context, int title, int message, boolean showOk){
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);

        dialog.setTitle(context.getString(title));
        dialog.setMessage(context.getString(message));

        if(showOk){
            dialog.setPositiveButton(R.string.confirm_text, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {}
            });
        }

        dialog.show();
    }
}
