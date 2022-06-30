package lab.android.audiodementia.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import lab.android.audiodementia.R;
import lab.android.audiodementia.activities.BaseActivity;
import lab.android.audiodementia.adapters.OnRecyclerItemClickListener;
import lab.android.audiodementia.adapters.RecyclerViewPlaylistAdapter;
import lab.android.audiodementia.background.Background;
import lab.android.audiodementia.background.BackgroundHttpExecutor;
import lab.android.audiodementia.background.NewPlaylistAddedEvent;
import lab.android.audiodementia.background.PlaylistsUploadedEvent;
import lab.android.audiodementia.background.RefreshTokenEvent;
import lab.android.audiodementia.client.HttpResponse;
import lab.android.audiodementia.client.HttpResponseWithData;
import lab.android.audiodementia.client.RestClient;
import lab.android.audiodementia.model.Playlist;
import lab.android.audiodementia.alerts.AddPlaylistDialogListener;
import lab.android.audiodementia.alerts.AlertDialogGenerator;
import lab.android.audiodementia.service.MusicPlayerService;
import lab.android.audiodementia.user.UserSession;

public class PlayerFragment extends Fragment {

    private ImageButton addToPlaylistButton;
    private UserSession session;
    private Background background = Background.getInstance();
    private OnDialogClick onDialogClick;
    private MusicPlayerService.PlayerServiceBinder playerServiceBinder;
    private MediaControllerCompat mediaController;
    private ImageButton playPauseButton;
    private ImageButton nextButton;
    private ImageButton prevButton;
    private TextView songTitle;
    private TextView artistName;
    private TextView albumTitle;
    private TextView currentTime;
    private TextView songDuration;
    private ImageView songCover;
    private SeekBar seekBar;
    private MediaMetadataCompat currentSongMeta;
    private long currentSongDuration;
    private Handler updateSongInfoHandler;
    private Handler seekBarHandler;
    private boolean updating;
    private MusicPlayerService service;
    private BackgroundHttpExecutor backgroundHttpExecutor;
    private boolean triedToRefresh;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        triedToRefresh = false;
        session = new UserSession(getActivity());
        onDialogClick = new OnDialogClick();
        updateSongInfoHandler = new Handler();
        seekBarHandler = new Handler();
        playerServiceBinder = ((BaseActivity) getActivity()).getBinder();
        service = playerServiceBinder.getService();
        try {
            mediaController = new MediaControllerCompat(
                    getActivity(), playerServiceBinder.getMediaSessionToken());
            mediaController.registerCallback(
                    new MediaControllerCompat.Callback() {
                        @Override
                        public void onPlaybackStateChanged(PlaybackStateCompat state) {
                            if (state == null)
                                return;
                            if (state.getState() == PlaybackStateCompat.STATE_SKIPPING_TO_NEXT ||
                                    state.getState() == PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS) {
                                updateSongInfoHandler.removeCallbacks(updateSongInfo);
                                updateSongInfoHandler.postDelayed(updateSongInfo, 500);
                                currentTime.setText("00:00/");
                                seekBar.setProgress(0);
                            }
                            if (state.getState() == PlaybackStateCompat.STATE_PAUSED) {
                                playPauseButton.setImageResource(android.R.drawable.ic_media_play);
                            }
                            if (state.getState() == PlaybackStateCompat.STATE_STOPPED) {
                                playPauseButton.setImageResource(android.R.drawable.ic_media_ff);
                            }
                            if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                                if (!updating)
                                    updateSeekbar();
                                playPauseButton.setImageResource(android.R.drawable.ic_media_pause);
                            }
                        }
                    }
            );
        }
        catch (RemoteException e) {
            mediaController = null;
        }
        backgroundHttpExecutor = new BackgroundHttpExecutor();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle) {
        return inflater.inflate(R.layout.player_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle bundle) {
        super.onViewCreated(view, bundle);

        addToPlaylistButton = getView().findViewById(R.id.add_to_playlist_btn);
        loadPlaylists();
        addToPlaylistButton.setOnClickListener(onDialogClick);

        playPauseButton = getView().findViewById(R.id.player_play_btn);
        nextButton = getView().findViewById(R.id.player_next_btn);
        prevButton = getView().findViewById(R.id.player_previous_btn);

        seekBar = getView().findViewById(R.id.player_seekbar);
        seekBar.setMax(100);
        seekBar.setProgress(50);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaController != null &&
                        fromUser &&
                        mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                    // It needs milliseconds, therefore "* 1000"
                    mediaController.getTransportControls().seekTo(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        songTitle = getView().findViewById(R.id.player_song_title);
        artistName = getView().findViewById(R.id.player_artist);
        albumTitle = getView().findViewById(R.id.player_album);
        currentTime = getView().findViewById(R.id.player_current_time);
        songDuration = getView().findViewById(R.id.player_duration_time);
        songCover = getView().findViewById(R.id.player_cover);

        updateSongInfoHandler.post(updateSongInfo);

        updateViewStateOnStart();

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaController != null)
                    mediaController.getTransportControls().skipToNext();
            }
        });

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaController != null)
                    mediaController.getTransportControls().skipToPrevious();
            }
        });

        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaController != null) {
                    if (mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                        mediaController.getTransportControls().pause();
                    }
                    else {
                        mediaController.getTransportControls().sendCustomAction("play_resume", null);
                    }
                }
            }
        });
    }

    public void updateViewStateOnStart() {
        if (mediaController != null) {
            if (mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                updateSeekbar();
                playPauseButton.setImageResource(android.R.drawable.ic_media_pause);
            }
            else if (mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED){
                playPauseButton.setImageResource(android.R.drawable.ic_media_play);
            }
        }
    }

    private void updateSongTime() {

    }

    private Runnable updateSongInfo = new Runnable() {
        @Override
        public void run() {
            if (mediaController != null) {
                try {
                    currentSongMeta = mediaController.getMetadata();
                    songTitle.setText(currentSongMeta.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
                    artistName.setText(currentSongMeta.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
                    albumTitle.setText(currentSongMeta.getString(MediaMetadataCompat.METADATA_KEY_ALBUM));
                    String coverUri = currentSongMeta.getString(MediaMetadataCompat.METADATA_KEY_ART_URI);
                    Picasso.get().load(coverUri).into(songCover);
                    currentSongDuration = currentSongMeta.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
                    String durationTime =
                            String.format(Locale.getDefault(),
                                    "%02d:%02d",
                                    TimeUnit.SECONDS.toMinutes(currentSongDuration),
                                    currentSongDuration -
                                            TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(currentSongDuration)));
                    songDuration.setText(durationTime);
                    seekBar.setMax((int)currentSongDuration);
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    };

    private void updateSeekbar() {
        seekBar.setMax((int)currentSongDuration);
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updating = true;
                    if (mediaController.getPlaybackState().getState() != PlaybackStateCompat.STATE_PLAYING) {
                        updating = false;
                        return;
                    }
                    if (service != null) {
                        int curPos = service.getPlayerPosition();
                        seekBar.setProgress(curPos);
                        String curTime = String.format(Locale.getDefault(),
                                "%02d:%02d/",
                                TimeUnit.SECONDS.toMinutes(curPos),
                                curPos -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(curPos)));
                        currentTime.setText(curTime);
                    }
                    seekBarHandler.postDelayed(this, 1000);
                }
            });
        }
        catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        background.register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        background.unregister(this);
    }

    // PLAYLISTS

    private void loadPlaylists() {
        background.execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(session.getId()));
                params.put("token", session.getToken());
                HttpResponseWithData<List<Playlist>> response = RestClient.getUserPlaylists(params);
                if (response.isUnauthorized()) {
                    RefreshTokenEvent refreshTokenEvent = RestClient.refreshToken(session.getRefresh());
                    if (refreshTokenEvent.isSuccessful()) {
                        session.setToken(refreshTokenEvent.getAccessToken());
                        params.put("token", session.getToken());
                        response = RestClient.getUserPlaylists(params);
                    }
                }
                background.postEvent(response);
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlaylistsLoaded(HttpResponseWithData<List<Playlist>> event) {
        if (event.isSuccess()) {
            triedToRefresh = false;
            onDialogClick.playlists = (ArrayList<Playlist>) event.getData();
            onDialogClick.adapter = new RecyclerViewPlaylistAdapter(onDialogClick.playlists);
        }
        else {
            AlertDialogGenerator.MakeAlertDialog(getActivity(), "Error while loading playlists", event.getMessage());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(NewPlaylistAddedEvent event) {
        if (event.isSuccess()) {
            Playlist newPlaylist = event.getPlaylist();
            onDialogClick.playlists.add(newPlaylist);
            onDialogClick.adapter.notifyDataSetChanged();
        }
        else {
            AlertDialogGenerator.MakeAlertDialog(getActivity(), "Error while adding playlist", event.getMessage());
        }
    }

    private void addSongToPlaylist(final long playlist_id) {
        final long song_id = currentSongMeta.getLong("song_id");
        background.execute(new Runnable() {
            @Override
            public void run() {
                HttpResponse response = RestClient.addSongToPlaylist(song_id, playlist_id, session.getToken());
                if (response.isUnauthorized()) {
                    RefreshTokenEvent refreshTokenEvent = RestClient.refreshToken(session.getRefresh());
                    if (refreshTokenEvent.isSuccessful()) {
                        session.setToken(refreshTokenEvent.getAccessToken());
                        response = RestClient.addSongToPlaylist(song_id, playlist_id, session.getToken());
                    }
                }
                background.postEvent(response);
            }
        });
    }

    private class OnDialogClick implements View.OnClickListener{

        private RecyclerViewPlaylistAdapter adapter;
        private ArrayList<Playlist> playlists;
        private ViewSwitcher switcher;

        OnDialogClick() {
            playlists = new ArrayList<Playlist>();
            adapter = new RecyclerViewPlaylistAdapter(playlists);
        }


        private void switchRecycler() {
            if (switcher != null) {
                if (playlists.size() != 0) {
                    if (switcher.getCurrentView().getId() == R.id.add_to_playlist_empty)
                        switcher.showNext();
                }
                else {
                    if (switcher.getCurrentView().getId() == R.id.add_to_playlist_recycler)
                        switcher.showNext();
                }
            }
        }

        @Override
        public void onClick(View v) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Add song to playlist");
            View viewInflated =
                    LayoutInflater.from(getActivity()).inflate(
                            R.layout.add_to_playlist_dialog, (ViewGroup) getView(), false);
            builder.setView(viewInflated);

            ImageButton newPlaylist = viewInflated.findViewById(R.id.add_to_playlist_new_btn);
            RecyclerView playlistRecycler = viewInflated.findViewById(R.id.add_to_playlist_recycler);
            playlistRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
            switcher = viewInflated.findViewById(R.id.add_to_playlist_switcher);
            adapter.setClickListener(new OnRecyclerItemClickListener() {
                @Override
                public void onItemClick(Object obj) {
                    Playlist playlist = (Playlist)obj;
                    addSongToPlaylist(playlist.get_ID());
                    Toast.makeText(
                            getActivity(), "Song added to playlist  " + playlist.getTitle(),
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onItemClick(Object obj, int position) {
                }
            });
            playlistRecycler.setAdapter(adapter);
            newPlaylist.setOnClickListener(new AddPlaylistDialogListener(getContext(), getView(), session));
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    switcher = null;
                }
            });
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    switcher = null;
                }
            });
            switchRecycler();
            builder.show();
        }
    }
}
