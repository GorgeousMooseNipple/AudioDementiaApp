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
import lab.android.audiodementia.background.NewPlaylistAddedEvent;
import lab.android.audiodementia.background.RefreshTokenEvent;
import lab.android.audiodementia.client.RestClient;
import lab.android.audiodementia.user.UserSession;

import java.util.HashMap;
import java.util.Map;


public class AddPlaylistDialogListener implements View.OnClickListener {

    private Background background = Background.getInstance();
    private Context context;
    private View view;
    private UserSession session;

    public AddPlaylistDialogListener(Context context, View view, UserSession session) {
        this.context = context;
        this.view = view;
        this.session = session;
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
                        NewPlaylistAddedEvent response = RestClient.addNewPlaylist(playlistTitle, session.getId(), session.getToken());
                        if (response.isUnauthorized()) {
                            RefreshTokenEvent refreshTokenEvent = RestClient.refreshToken(session.getRefresh());
                            if (refreshTokenEvent.isSuccessful()) {
                                session.setToken(refreshTokenEvent.getAccessToken());
                                response = RestClient.addNewPlaylist(playlistTitle, session.getId(), session.getToken());
                            }
                        }
                        background.postEvent(response);
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
