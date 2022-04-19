package lab.android.audiodementia.alerts;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;


public class AlertDialogGenerator {


    public static void MakeAlertDialog(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

}
