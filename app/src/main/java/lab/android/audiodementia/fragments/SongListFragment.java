package lab.android.audiodementia.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lab.android.audiodementia.R;
import lab.android.audiodementia.adapters.BottomReachedListener;
import lab.android.audiodementia.adapters.OnRecyclerItemClickListener;
import lab.android.audiodementia.adapters.RecyclerViewSongAdapter;
import lab.android.audiodementia.background.Background;
import lab.android.audiodementia.background.BackgroundHttpExecutor;
import lab.android.audiodementia.background.SubmitSongEvent;
import lab.android.audiodementia.background.SubmitSongListEvent;
import lab.android.audiodementia.client.HttpResponseWithData;
import lab.android.audiodementia.client.RestClient;
import lab.android.audiodementia.model.Song;
import lab.android.audiodementia.alerts.AlertDialogGenerator;

public class SongListFragment extends Fragment {

    private Background background = Background.getInstance();
    private RecyclerView recycler;
    private ProgressBar progressBar;
    private ViewSwitcher switcher;
    private TextView emptyMessage;
    private ArrayList<Song> songList;
    private String dataType;
    private long lastSongId;
    private long entityId;
    private boolean loadMoreScrolling;
    private boolean needMore;
    private boolean listUpdated;
    private RecyclerViewSongAdapter songAdapter;
    private BackgroundHttpExecutor backgroundHttpExecutor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lastSongId = 0;
        loadMoreScrolling = true;
        listUpdated = true;
        songList = new ArrayList<>();
        songAdapter = new RecyclerViewSongAdapter(songList);
        needMore = true;
        backgroundHttpExecutor = new BackgroundHttpExecutor();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.song_list_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recycler = getView().findViewById(R.id.song_list_recycler);
        progressBar = getView().findViewById(R.id.song_list_load_progress);
        switcher = getView().findViewById(R.id.song_list_switcher);
        emptyMessage = getView().findViewById(R.id.song_list_empty_message);
        try {
            dataType = getArguments().getString("data_type");
            switch (dataType) {
                case "album":
                    entityId = getArguments().getLong("album_id");
                    break;
                case "playlist":
                    entityId = getArguments().getLong("playlist_id");
                    break;
                case "genre":
                    entityId = getArguments().getLong("genre_id");
                    break;
                default:
                    entityId = 0;
                    break;
            }
            if (entityId > 0) {
                loadMore(dataType, entityId);
            }
        }
        catch (NullPointerException ex) {
            emptyMessage.setText("Error while creating page");
            switchRecyclerView(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void loadMore(String dataType, long entityId) {
        switch (dataType) {
            case "album":
                loadMoreScrolling = false;
                loadAlbumSongs(entityId);
                break;
            case "playlist":
                loadPlaylistSongs(entityId, lastSongId);
                break;
            case "genre":
                loadGenreSongs(entityId, lastSongId);
                break;
            default:
                break;
        }
    }

    private void loadAlbumSongs(final long albumId) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("id", String.valueOf(albumId));
        backgroundHttpExecutor.executeWithReturn(RestClient::getAlbumSongs, params, this::onSongsLoaded);
    }

    private void loadPlaylistSongs(final long playlistId, final long lastId) {
        if (progressBar.getVisibility() == View.GONE)
            progressBar.setVisibility(View.VISIBLE);
        Map<String, String> params = new HashMap<String, String>();
        params.put("id", String.valueOf(playlistId));
        params.put("last_id", String.valueOf(lastSongId));
        backgroundHttpExecutor.executeWithReturn(RestClient::getPlaylistSongs, params, this::onSongsLoaded);
    }

    private void loadGenreSongs(final long genreId, final long lastId) {
        if (progressBar.getVisibility() == View.GONE)
            progressBar.setVisibility(View.VISIBLE);
        Map<String, String> params = new HashMap<String, String>();
        params.put("id", String.valueOf(genreId));
        params.put("last_id", String.valueOf(lastSongId));
        backgroundHttpExecutor.executeWithReturn(RestClient::getGenreSongs, params, this::onSongsLoaded);
    }

    public void onSongsLoaded(HttpResponseWithData<List<Song>> event) {
        if (progressBar.getVisibility() == View.VISIBLE)
            progressBar.setVisibility(View.GONE);
        if (event.isSuccess()) {
            ArrayList<Song> songs = (ArrayList<Song>) event.getData();
            if(songs.size() > 0) {
                recyclerLoadData((ArrayList<Song>) event.getData());
            }
        }
        else {
            AlertDialogGenerator.MakeAlertDialog(getActivity(), "Songs loading error", event.getMessage());
        }
    }

    private void switchRecyclerView(boolean show) {
        if (show) {
            if (switcher.getCurrentView().getId() == R.id.song_list_empty_message)
                switcher.showNext();
        }
        else {
            if (switcher.getCurrentView().getId() == R.id.song_list_recycler)
                switcher.showNext();
        }
    }

    private void recyclerLoadData(ArrayList<Song> songs) {
        if (songList.size() != 0 || songs.size() != 0) {
            switchRecyclerView(true);
            if (lastSongId == 0) {
                needMore = true;
                songList.addAll(songs);
                songAdapter = new RecyclerViewSongAdapter(songs);
                if (loadMoreScrolling)
                    songAdapter.setBottomReachedListener(new BottomReachedListener() {
                        @Override
                        public void onBottomReached(int position) {
                            if (needMore)
                                loadMore(dataType, entityId);
                        }
                    });
                songAdapter.setClickListener(new OnRecyclerItemClickListener() {
                    @Override
                    public void onItemClick(Object obj) {

                    }

                    @Override
                    public void onItemClick(Object obj, int position) {
                        if (listUpdated)
                            submitSongList(position);
                        else
                            submitSong(position);
                    }
                });
                recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
                recycler.setAdapter(songAdapter);
                lastSongId = songList.get(songList.size() - 1).get_ID();
                listUpdated = true;
            }
            else {
                if (songs.size() == 0)
                    needMore = false;
                songList.addAll(songs);
                songAdapter.notifyDataSetChanged();
                lastSongId = songList.get(songList.size() - 1).get_ID();
                listUpdated = true;
            }
        }
        else {
            switchRecyclerView(false);
        }
    }

    private void submitSongList(final int position) {
        background.execute(new Runnable() {
            @Override
            public void run() {
                background.postEvent(new SubmitSongListEvent(songList, position));
            }
        });
        listUpdated = false;
    }

    private void submitSong(final int position) {
        background.execute(new Runnable() {
            @Override
            public void run() {
                background.postEvent(new SubmitSongEvent(position));
            }
        });
    }
}
