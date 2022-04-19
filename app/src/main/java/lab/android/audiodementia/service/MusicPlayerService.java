package lab.android.audiodementia.service;


import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;

import lab.android.audiodementia.background.Background;
import lab.android.audiodementia.background.SubmitSongEvent;
import lab.android.audiodementia.background.SubmitSongListEvent;
import lab.android.audiodementia.fragments.PlayerFragment;
import lab.android.audiodementia.model.Song;

public class MusicPlayerService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener,
        AudioManager.OnAudioFocusChangeListener, MediaPlayer.OnBufferingUpdateListener {

    private Background background = Background.getInstance();
    private MediaPlayer player;
    private ArrayList<Song> songList;
    private boolean paused = false;
    private boolean prepared = true;
    private boolean preparing = false;
    private boolean skipping = false;
    private int resumePosition;
    private int bufferingPercent;

    public void setSongList(ArrayList<Song> songList) {
        this.songList = songList;
    }

    private int currentPosition;

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    private MediaSessionCompat mediaSession;

    final MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();

    final PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
            .setActions(
                    PlaybackStateCompat.ACTION_PLAY
                            | PlaybackStateCompat.ACTION_STOP
                            | PlaybackStateCompat.ACTION_PAUSE
                            | PlaybackStateCompat.ACTION_PLAY_PAUSE
                            | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                            | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);

    private AudioManager audioManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new PlayerServiceBinder();
    }

    public class PlayerServiceBinder extends Binder {
        public MediaSessionCompat.Token getMediaSessionToken() {
            return mediaSession.getSessionToken();
        }

        public MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        currentPosition = 0;
        songList = new ArrayList<>();
        player = new MediaPlayer();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        initMediaPlayer();
        initMediaSession();
        background.register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaSession.release();
        player.release();
        stopSelf();
        background.unregister(this);
    }

    public int getPlayerPosition() {
        if (player != null)
            return player.getCurrentPosition();
        return 0;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mediaSession, intent);
        return super.onStartCommand(intent, flags, startId);
    }

    public void initMediaPlayer() {
        player.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setOnPreparedListener(this);
        player.setOnErrorListener(this);
        player.setOnCompletionListener(this);
    }

    // TODO: Media Session

    public void initMediaSession() {
        mediaSession = new MediaSessionCompat(this, "PlayerService");
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mediaSession.setCallback(mediaSessionCallback);

        Context appContext = getApplicationContext();

        Intent intent = new Intent(appContext, PlayerFragment.class);

        mediaSession.setSessionActivity(
                PendingIntent.getActivity(
                        appContext, 0, intent, 0));

        Intent mediaButtonIntent = new Intent(
                Intent.ACTION_MEDIA_BUTTON, null, appContext, MediaButtonReceiver.class);
        mediaSession.setMediaButtonReceiver(
                PendingIntent.getBroadcast(appContext, 0, mediaButtonIntent, 0));
    }

    MediaSessionCompat.Callback mediaSessionCallback = new MediaSessionCompat.Callback() {

        private boolean skipped = false;

        private Song setSongMeta() {
            Song song = songList.get(currentPosition);

            MediaMetadataCompat metadata = metadataBuilder
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.getTitle())
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.getAlbum())
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.getArtist())
                    .putString(MediaMetadataCompat.METADATA_KEY_ART_URI, song.getMediumCoverUrl())
                    .putLong("song_id", song.get_ID())
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.getDuration()).build();
            mediaSession.setMetadata(metadata);
            return song;
        }

        private void prepareForPlaying() {
            mediaSession.setActive(true);
        }

        @Override
        public void onPlay() {

            prepared = false;
            if (!requestAudioFocus())
                return;

            resumePosition = 0;

            mediaSession.setActive(true);

            mediaSession.setPlaybackState(
                    stateBuilder.setState(PlaybackStateCompat.STATE_CONNECTING,
                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());

//            if (player.isPlaying() || preparing || paused)
//                player.reset();
            player.reset();

            Song song = setSongMeta();

            try {
                player.setDataSource(song.getUri());
                player.prepareAsync();
                preparing = true;
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }

            paused = false;

            registerNoisyReceiver();
        }

        @Override
        public void onPause() {
            if (player.isPlaying()) {
                paused = true;
                resumePosition = player.getCurrentPosition();
                player.pause();
                mediaSession.setPlaybackState(
                        stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                                PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());

                unregisterNoisyReceiver();
            }
        }

        @Override
        public void onStop() {
            prepared = false;
            removeAudioFocus();
            player.stop();
            mediaSession.setActive(false);
            unregisterNoisyReceiver();

            mediaSession.setPlaybackState(
                    stateBuilder.setState(PlaybackStateCompat.STATE_STOPPED,
                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
        }

        @Override
        public void onSkipToNext() {
            prepared = false;
            skipping = true;
            mediaSession.setPlaybackState(
                    stateBuilder.setState(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT,
                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
            currentPosition++;
            if (currentPosition >= songList.size())
                currentPosition = 0;
            if (paused) {
                resumePosition = 0;
                Song song = setSongMeta();
                skipped = true;
                try {
                    player.reset();
                    player.setDataSource(song.getUri());
                    player.prepareAsync();
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
                return;
            }
            onPlay();
        }

        @Override
        public void onSkipToPrevious() {
            prepared = false;
            skipping = true;
            mediaSession.setPlaybackState(
                    stateBuilder.setState(PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS,
                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
            currentPosition--;
            if (currentPosition < 0)
                currentPosition = songList.size() - 1;
            if (paused) {
                resumePosition = 0;
                Song song = setSongMeta();
                skipped = true;
                try {
                    player.reset();
                    player.setDataSource(song.getUri());
                    player.prepareAsync();
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
                return;
            }
            onPlay();
        }

        @Override
        public void onSeekTo(long pos) {
            try {
//                if (bufferingPercent != 0) {
//                    if ((float) (pos / player.getDuration()) < (float) (bufferingPercent / 100f))
//                        player.seekTo((int) pos);
//                    else
//                        player.seekTo((int) ((float) player.getDuration() * (float) bufferingPercent / 100f));
//                } else
//                    player.seekTo((int) pos);
                player.seekTo((int)pos);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void onCustomAction(String action, Bundle extras) {
            if (action.equals("update_position"))
            {
                mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                        player.getCurrentPosition(), 1).build());
            }
            if (action.equals("play_resume")) {
                if (paused) {
                    if (!skipped) {
                        paused = false;
                        registerNoisyReceiver();
                        player.start();
                        player.seekTo(resumePosition);
                        mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                                player.getCurrentPosition(), 1).build());
                    }
                    else {
                        skipped = false;
                        paused = false;
                        mediaSession.setActive(true);
                        if (prepared) {
                            registerNoisyReceiver();
                            mediaSession.setPlaybackState(
                                    stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
                            player.start();
                        }
                    }
                }
                else
                    onPlay();
            }
        }
    };

    // TODO: MediaPlayer Listener

    @Override
    public void onPrepared(MediaPlayer mp) {
        prepared = true;
        preparing = false;
        if (!paused) {
            registerNoisyReceiver();
            mediaSession.setPlaybackState(
                    stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
            player.start();
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        bufferingPercent = percent;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        player.stop();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mediaSessionCallback.onSkipToNext();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SubmitSongEvent event) {
        this.currentPosition = event.getPosition();
        mediaSessionCallback.onPlay();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SubmitSongListEvent event) {
        this.songList = event.getSongList();
        this.currentPosition = event.getPosition();
        mediaSessionCallback.onPlay();
    }

    private BroadcastReceiver noisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mediaSessionCallback.onPause();
        }
    };

    private void registerNoisyReceiver() {
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(noisyReceiver, intentFilter);
    }

    private void unregisterNoisyReceiver() {
        unregisterReceiver(noisyReceiver);
    }

    // TODO: Audiofocus
    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange)
        {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (player.isPlaying())
                    player.setVolume(1f, 1f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS :
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (player.isPlaying())
                    mediaSessionCallback.onPause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (player.isPlaying())
                    player.setVolume(0.1f, 0.1f);
                break;
        }
    }

    private boolean requestAudioFocus() {

        int audioFocusResult = audioManager.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        return audioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
//        try {
//            if(audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN) ==
//                    AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
//            {
//                gotAudioFocus = true;
//            }
//        }
//        catch (NullPointerException ex)
//        {
//            return false;
//        }
//        return gotAudioFocus;
    }

    private boolean removeAudioFocus() {

        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.abandonAudioFocus(this);
    }
}
