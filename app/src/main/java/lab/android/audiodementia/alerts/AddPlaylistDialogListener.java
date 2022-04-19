package lab.android.audiodementia.alerts;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import lab.android.audiodementia.R;
import lab.android.audiodementia.background.Background;
import lab.android.audiodementia.client.RestClient;

import java.util.HashMap;
import java.util.Map;


public class AddPlaylistDialogListener implements View.OnClickListener {

    private Background background = Background.getInstance();
    private long userId;
    private Context context;
    private View view;
    private String userToken;

    public AddPlaylistDialogListener(Context context, View view, long userId, String userToken) {
        this.context = context;
        this.view = view;
        this.userId = userId;
        this.userToken = userToken;
    }

    @Override
    public void onClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("New playlist");
        View viewInflated = LayoutInflater.from(context).inflate(
                R.layout.add_new_playlist_dialog, (ViewGroup) view, false
        );

        final EditText titleEdit = viewInflated.findViewById(R.id.add_new_playlist_edit);
        builder.setView(viewInflated).setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String playlistTitle = titleEdit.getText().toString();
                if (playlistTitle.isEmpty()) {
                    AlertDialogGenerator.MakeAlertDialog(context, "Error",
                            "New playlist title is required");
                    return;
                }

                background.execute(new Runnable() {
                    @Override
                    public void run() {
                        background.postEvent(RestClient.addNewPlaylist(playlistTitle, userId, userToken));
                    }
                });

                dialog.dismiss();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

}
