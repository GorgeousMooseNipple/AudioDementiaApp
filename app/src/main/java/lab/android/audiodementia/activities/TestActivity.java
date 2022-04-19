package lab.android.audiodementia.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import lab.android.audiodementia.R;
import lab.android.audiodementia.background.Background;
import lab.android.audiodementia.service.MusicPlayerService;


public class TestActivity extends AppCompatActivity {

    Background background = Background.getInstance();
    MusicPlayerService.PlayerServiceBinder playerServiceBinder;
    MediaControllerCompat mediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_layout);

        final Button playButton = (Button) findViewById(R.id.play);
        final Button pauseButton = (Button) findViewById(R.id.pause);
        final Button stopButton = (Button) findViewById(R.id.stop);

        bindService(new Intent(this, MusicPlayerService.class), new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                playerServiceBinder = (MusicPlayerService.PlayerServiceBinder) service;
                try {
                    mediaController = new MediaControllerCompat(
                            TestActivity.this, playerServiceBinder.getMediaSessionToken());
                    mediaController.registerCallback(
                            new MediaControllerCompat.Callback() {
                                @Override
                                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                                    if (state == null)
                                        return;
                                    boolean playing =
                                            state.getState() == PlaybackStateCompat.STATE_PLAYING;
                                    playButton.setEnabled(!playing);
                                    pauseButton.setEnabled(playing);
                                    stopButton.setEnabled(playing);

                                }


                            }

                    );
                }
                catch (RemoteException e) {
                    mediaController = null;
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                playerServiceBinder = null;
                mediaController = null;
            }
        }, BIND_AUTO_CREATE);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaController != null)
                    mediaController.getTransportControls().play();
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaController != null)
                    mediaController.getTransportControls().pause();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaController != null)
                    mediaController.getTransportControls().stop();
            }
        });
    }

}
