package lab.android.audiodementia.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewSwitcher;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lab.android.audiodementia.R;
import lab.android.audiodementia.activities.BaseActivity;
import lab.android.audiodementia.adapters.OnRecyclerItemClickListener;
import lab.android.audiodementia.adapters.RecyclerViewGenreAdapter;
import lab.android.audiodementia.adapters.RecyclerViewPlaylistAdapter;
import lab.android.audiodementia.background.Background;
import lab.android.audiodementia.background.BackgroundHttpExecutor;
import lab.android.audiodementia.background.RefreshTokenEvent;
import lab.android.audiodementia.client.HttpResponseWithData;
import lab.android.audiodementia.client.RestClient;
import lab.android.audiodementia.model.Genre;
import lab.android.audiodementia.model.Playlist;
import lab.android.audiodementia.alerts.AlertDialogGenerator;
import lab.android.audiodementia.user.UserSession;

public class MainFragment extends Fragment {


    private RecyclerView genresRecycler;
    private RecyclerView playlistsRecycler;
    private UserSession session;
    private ArrayList<Genre> genreList;
    private ArrayList<Playlist> playlistList;
    private ViewSwitcher genresSwitcher;
    private ViewSwitcher playlistsSwitcher;
    private Background background = Background.getInstance();
    private BackgroundHttpExecutor backgroundHttpExecutor;
    private Handler handler;
    private boolean triedToRefresh;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        triedToRefresh = false;
        session = new UserSession(getActivity());
        handler = new Handler();
        backgroundHttpExecutor = new BackgroundHttpExecutor(handler);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle)
    {
        return inflater.inflate(R.layout.main_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle bundle) {
        super.onViewCreated(view, bundle);

        genresRecycler = getView().findViewById(R.id.main_genre_recycler);
        playlistsRecycler = getView().findViewById(R.id.main_playlists_recycler);
        genresSwitcher = getView().findViewById(R.id.main_genres_recycler_switcher);
        playlistsSwitcher = getView().findViewById(R.id.main_playlist_recycler_switcher);

        loadGenres();
        loadPlaylists();
    }

    private void loadGenres() {
        backgroundHttpExecutor.executeWithReturn(RestClient::getGenres, this::onGenresLoaded);
    }

    private void loadPlaylists() {
        Map<String, String> params = new HashMap<>();
        params.put("user_id", String.valueOf(session.getId()));
        params.put("token", session.getToken());
        backgroundHttpExecutor.executeWithReturn(RestClient::getUserPlaylists, params, this::onPlaylistsLoaded);
    }

    public void onGenresLoaded(HttpResponseWithData<List<Genre>> event) {
        if (event.isSuccess()) {
            ArrayList<Genre> genres = (ArrayList<Genre>) event.getData();
            if(genres.size() > 0) {
                this.genreList = genres;
                setDataToGenresRecycler(this.genreList);
            }
        }
        else {
            AlertDialogGenerator.MakeAlertDialog(getActivity(), "Error while loading genres", event.getMessage());
        }
    }

    public void onPlaylistsLoaded(HttpResponseWithData<List<Playlist>> event) {
        if (event.isSuccess()) {
            triedToRefresh = false;
            ArrayList<Playlist> playlists = (ArrayList<Playlist>) event.getData();
            if(playlists.size() > 0) {
                this.playlistList = playlists;
                setDataToPlaylistsRecycler(this.playlistList);
            }
        }
        else {
            if (event.isUnauthorized() && !triedToRefresh) {
                background.postEvent(RestClient.refreshToken(session.getRefresh()));
                triedToRefresh = true;
            }
            AlertDialogGenerator.MakeAlertDialog(getActivity(), "Error while loading playlists", event.getMessage());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshToken(RefreshTokenEvent event) {
        if (event.isSuccessful()) {
            String accessToken = event.getAccessToken();
            session.setToken(accessToken);
            loadPlaylists();
        }
        else {
            AlertDialogGenerator.MakeAlertDialog(getActivity(), "Error refreshing access token", event.getMessage());
        }
    }

    private void switchGenresView(boolean show) {
        if (show) {
            if (genresSwitcher.getCurrentView().getId() == R.id.main_genres_empty)
                genresSwitcher.showNext();
        }
        else {
            if (genresSwitcher.getCurrentView().getId() == R.id.main_genre_recycler)
                genresSwitcher.showNext();
        }
    }

    private void switchPlaylistsView(boolean show) {
        if (show) {
            if (playlistsSwitcher.getCurrentView().getId() == R.id.main_playlists_empty)
                playlistsSwitcher.showNext();
        }
        else {
            if (playlistsSwitcher.getCurrentView().getId() == R.id.main_playlists_recycler)
                playlistsSwitcher.showNext();
        }
    }

    private void setDataToGenresRecycler(ArrayList<Genre> genres) {
        if (genreList.size() != 0)
            switchGenresView(true);
        else
            switchGenresView(false);
        genresRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        RecyclerViewGenreAdapter genresAdapter = new RecyclerViewGenreAdapter(genres);
        genresAdapter.setListener(new OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(Object obj) {
                Genre genre = (Genre) obj;
                loadGenreSongs(genre);
            }

            @Override
            public void onItemClick(Object obj, int position) {

            }
        });
        genresRecycler.setAdapter(genresAdapter);
    }

    private void setDataToPlaylistsRecycler(ArrayList<Playlist> playlists) {
        if (playlistList.size() != 0)
            switchPlaylistsView(true);
        else
            switchPlaylistsView(false);
        playlistsRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        RecyclerViewPlaylistAdapter playlistsAdapter = new RecyclerViewPlaylistAdapter(playlists);
        playlistsAdapter.setClickListener(new OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(Object obj) {
                Playlist playlist = (Playlist)obj;
                loadPlaylistSongs(playlist);
            }

            @Override
            public void onItemClick(Object obj, int position) {

            }
        });
        playlistsRecycler.setAdapter(playlistsAdapter);
    }

    private void loadGenreSongs(Genre genre) {
        if (getActivity() != null) {
            Bundle bundle = new Bundle();
            bundle.putString("data_type", "genre");
            bundle.putLong("genre_id", genre.get_ID());
            ((BaseActivity) getActivity()).startSongList(bundle, genre.getGenreName());
        }
    }

    private void loadPlaylistSongs(Playlist playlist) {
        if (getActivity() != null) {
            Bundle bundle = new Bundle();
            bundle.putString("data_type", "playlist");
            bundle.putLong("playlist_id", playlist.get_ID());
            ((BaseActivity) getActivity()).startSongList(bundle, playlist.getTitle());
        }
    }
}
