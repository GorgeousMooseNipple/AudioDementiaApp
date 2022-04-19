package lab.android.audiodementia.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
import lab.android.audiodementia.adapters.RecyclerViewPlaylistAdapter;
import lab.android.audiodementia.background.Background;
import lab.android.audiodementia.background.BackgroundHttpExecutor;
import lab.android.audiodementia.background.NewPlaylistAddedEvent;
import lab.android.audiodementia.client.HttpResponseWithData;
import lab.android.audiodementia.client.RestClient;
import lab.android.audiodementia.model.Playlist;
import lab.android.audiodementia.alerts.AlertDialogGenerator;
import lab.android.audiodementia.alerts.AddPlaylistDialogListener;
import lab.android.audiodementia.user.UserSession;

public class PlaylistsFragment extends Fragment {

    private Background background = Background.getInstance();
    private UserSession session;
    private RecyclerView playlistsRecycler;
    private RecyclerViewPlaylistAdapter playlistAdapter;
    private ImageButton addNewPlaylist;
    private ViewSwitcher switcher;
    private ArrayList<Playlist> playlistList;
    private BackgroundHttpExecutor backgroundHttpExecutor;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        session = new UserSession(getActivity());
        backgroundHttpExecutor = new BackgroundHttpExecutor();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle) {
        return inflater.inflate(R.layout.playlists_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle bundle) {
        super.onViewCreated(view, bundle);

        playlistsRecycler = getView().findViewById(R.id.playlists_recycler);
        addNewPlaylist = getView().findViewById(R.id.playlist_add_button);
        addNewPlaylist.setOnClickListener(
                new AddPlaylistDialogListener(getContext(), getView(), session.getId(), session.getToken()));
        switcher = getView().findViewById(R.id.playlists_recycler_switcher);

        playlistAdapter = new RecyclerViewPlaylistAdapter(new ArrayList<Playlist>());

        loadPlaylists();
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

    private void loadPlaylists() {
        Map<String, String> params = new HashMap<>();
        params.put("user_id", String.valueOf(session.getId()));
        params.put("token", session.getToken());
        backgroundHttpExecutor.executeWithReturn(RestClient::getUserPlaylists, params, this::onPlaylistsLoaded);
    }

    public void onPlaylistsLoaded(HttpResponseWithData<List<Playlist>> event) {
        if (event.isSuccess()) {
            this.playlistList = (ArrayList<Playlist>) event.getData();
            setDataToPlaylistsRecycler();
        }
        else {
            AlertDialogGenerator.MakeAlertDialog(getActivity(), "Error while loading playlists", event.getMessage());
        }
    }

    private void switchRecycler(boolean show) {
        if (show) {
            if (switcher.getCurrentView().getId() == R.id.playlists_empty_message)
                switcher.showNext();
        }
        else {
            if (switcher.getCurrentView().getId() == R.id.playlists_recycler)
                switcher.showNext();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(NewPlaylistAddedEvent event) {
        if (event.isSuccess()) {
            Playlist newPlaylist = event.getPlaylist();
            setAddNewPlaylist(newPlaylist);
        }
        else {
            AlertDialogGenerator.MakeAlertDialog(getActivity(), "Error while adding playlist", event.getMessage());
        }
    }

    public void setDataToPlaylistsRecycler() {
        switchRecycler(playlistList.size() != 0);
        playlistsRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        playlistAdapter = new RecyclerViewPlaylistAdapter(playlistList);
        playlistAdapter.setClickListener(new OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(Object obj) {
                Playlist playlist = (Playlist)obj;
                loadPlaylistSongs(playlist);
            }

            @Override
            public void onItemClick(Object obj, int position) {

            }
        });
        playlistsRecycler.setAdapter(playlistAdapter);
    }

    public void setAddNewPlaylist(Playlist playlist) {
        playlistList.add(playlist);
        playlistAdapter.notifyDataSetChanged();
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
