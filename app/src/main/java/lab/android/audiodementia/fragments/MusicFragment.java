package lab.android.audiodementia.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lab.android.audiodementia.R;
import lab.android.audiodementia.activities.BaseActivity;
import lab.android.audiodementia.adapters.BottomReachedListener;
import lab.android.audiodementia.adapters.OnRecyclerItemClickListener;
import lab.android.audiodementia.adapters.RecyclerViewAlbumAdapter;
import lab.android.audiodementia.adapters.RecyclerViewSongAdapter;
import lab.android.audiodementia.background.AlbumsByTitleLoadedEvent;
import lab.android.audiodementia.background.Background;
import lab.android.audiodementia.background.HttpHandler;
import lab.android.audiodementia.background.SongsLoadedEvent;
import lab.android.audiodementia.background.SubmitSongEvent;
import lab.android.audiodementia.background.SubmitSongListEvent;
import lab.android.audiodementia.client.HttpResponseWithData;
import lab.android.audiodementia.client.RestClient;
import lab.android.audiodementia.model.Album;
import lab.android.audiodementia.model.Song;
import lab.android.audiodementia.alerts.AlertDialogGenerator;

public class MusicFragment extends Fragment
        implements AdapterView.OnItemSelectedListener, SearchView.OnQueryTextListener {

    private Background background = Background.getInstance();
    private SearchView searchView;
    private Spinner searchSpinner;
    private RecyclerView recyclerView;
    private ViewSwitcher switcher;
    private TextView searchMessage;
    private ProgressBar loadMoreProgress;
    private RecyclerViewSongAdapter songAdapter;
    private RecyclerViewAlbumAdapter albumAdapter;
    private RequiredData dataToLoad;
    private RequiredData loadedData;
    private ArrayList<Song> songList;
    private ArrayList<Album> albumList;
    private long lastSongId;
    private long lastAlbumId;
    private String lastQuery;
    private boolean needMore;
    private boolean listUpdated;
    private HttpHandler httpHandler;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        lastSongId = 0;
        lastAlbumId = 0;
        needMore = true;
        listUpdated = false;
        httpHandler = new HttpHandler();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle) {
        return inflater.inflate(R.layout.music_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle bundle) {
        super.onViewCreated(view, bundle);

        recyclerView = getView().findViewById(R.id.music_recycler);

        searchSpinner = getView().findViewById(R.id.search_spinner);
        ArrayAdapter<?> adapter =
                ArrayAdapter.createFromResource(getActivity(), R.array.music_search_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        searchSpinner.setAdapter(adapter);

        searchSpinner.setOnItemSelectedListener(this);

        searchView = getView().findViewById(R.id.search_input);
        searchView.setOnQueryTextListener(this);

        dataToLoad = RequiredData.LOAD_SONGS;
        loadedData = RequiredData.LOAD_SONGS;
        switcher = getView().findViewById(R.id.music_recycler_switcher);
        loadMoreProgress = getView().findViewById(R.id.music_load_progress);
        searchMessage = getView().findViewById(R.id.music_search_message);
    }

    @Override
    public void onResume() {
        super.onResume();
        background.register(this);
        if (loadedData != null && loadedData == RequiredData.LOAD_SONGS)
            if (songList != null && songList.size() != 0)
                switchRecyclerView(true);
        if (loadedData != null && loadedData == RequiredData.LOAD_ARTISTS)
            if (songList != null && songList.size() != 0)
                switchRecyclerView(true);
        if (loadedData != null && loadedData == RequiredData.LOAD_ALBUMS)
            if (albumList != null && albumList.size() != 0)
                switchRecyclerView(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        background.unregister(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        switch (position)
        {
            case 0:
                dataToLoad = RequiredData.LOAD_SONGS;
                break;
            case 1:
                dataToLoad = RequiredData.LOAD_ALBUMS;
                break;
            case 2:
                dataToLoad = RequiredData.LOAD_ARTISTS;
            default:
                Toast.makeText(getContext(), searchSpinner.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
                break;
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public boolean onQueryTextSubmit(final String query) {
        lastSongId = 0;
        lastAlbumId = 0;
        lastQuery = query;
        loadedData = dataToLoad;
        needMore = true;
        if (songList != null)
            songList.clear();
        if (albumList != null)
            albumList.clear();
        loadMore();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    private void switchRecyclerView(boolean show) {
        if (show) {
            if (switcher.getCurrentView().getId() == R.id.music_search_message)
                switcher.showNext();
        }
        else {
            if (switcher.getCurrentView().getId() == R.id.music_recycler)
                switcher.showNext();
        }
    }

    private void loadSongs(final String title) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("title", title);
        params.put("last_id", String.valueOf(lastSongId));
        httpHandler.executeWithReturn(RestClient::getSongsByTitle, params, this::onSongsLoaded);
    }

    public void onSongsLoaded(HttpResponseWithData<List<Song>> event) {
        if (loadMoreProgress.getVisibility() == View.VISIBLE)
            loadMoreProgress.setVisibility(View.GONE);
        if (event.isSuccess()) {
            setRecyclerDataSong((ArrayList<Song>) event.getData());
        }
        else {
            AlertDialogGenerator.MakeAlertDialog(getActivity(), "Loading songs error", event.getMessage());
        }
    }

    private void loadAlbums(final String title) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("title", title);
        params.put("last_id", String.valueOf(lastAlbumId));
        httpHandler.executeWithReturn(RestClient::getAlbumsByTitle, params, this::onAlbumsLoaded);
    }

    public void onAlbumsLoaded(HttpResponseWithData<List<Album>> event) {
        if (loadMoreProgress.getVisibility() == View.VISIBLE)
            loadMoreProgress.setVisibility(View.GONE);
        if (event.isSuccess()) {
            setRecyclerDataAlbum((ArrayList<Album>) event.getData());
        }
        else {
            AlertDialogGenerator.MakeAlertDialog(getActivity(), "Loading albums error", event.getMessage());
        }
    }

    private void loadArtists(final String title) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("title", title);
        params.put("last_id", String.valueOf(lastSongId));
        httpHandler.executeWithReturn(RestClient::getSongsByArtist, params, this::onSongsLoaded);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SongsLoadedEvent event) {
        if (loadMoreProgress.getVisibility() == View.VISIBLE)
            loadMoreProgress.setVisibility(View.GONE);
        if (event.isSuccess()) {
            setRecyclerDataSong(event.getSongList());
        }
        else {
            AlertDialogGenerator.MakeAlertDialog(getActivity(), "Loading songs error", event.getMessage());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(AlbumsByTitleLoadedEvent event) {
        if (loadMoreProgress.getVisibility() == View.VISIBLE)
            loadMoreProgress.setVisibility(View.GONE);
        if (event.isSuccess()) {
            setRecyclerDataAlbum(event.getAlbumList());
        }
        else {
            AlertDialogGenerator.MakeAlertDialog(getActivity(), "Loading albums error", event.getMessage());
        }
    }

    private void setRecyclerDataSong(ArrayList<Song> newSongs) {
        if (songList == null)
            songList = new ArrayList<>();
        if (songList.size() != 0 || newSongs.size() != 0) {
            switchRecyclerView(true);
            if (lastSongId == 0) {
                recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
                songList.addAll(newSongs);
                songAdapter = new RecyclerViewSongAdapter(songList);
                songAdapter.setBottomReachedListener(new BottomReachedListener() {
                    @Override
                    public void onBottomReached(int position) {
                        if (needMore)
                            loadMore();
                    }
                });
                songAdapter.setClickListener(new OnRecyclerItemClickListener() {
                    @Override
                    public void onItemClick(Object obj) {

                    }

                    @Override
                    public void onItemClick(Object obj, int position) {
                        if (listUpdated)
                            MusicFragment.this.submitSongList(position);
                        else
                            MusicFragment.this.submitSong(position);
                    }
                });
                recyclerView.setAdapter(songAdapter);
                lastSongId = songList.get(songList.size() - 1).get_ID();
                listUpdated = true;
            }
            else {
                if (newSongs.size() != 0) {
                    songList.addAll(newSongs);
                    songAdapter.notifyDataSetChanged();
                    lastSongId = songList.get(songList.size() - 1).get_ID();
                    listUpdated = true;
                }
                else
                    needMore = false;
            }
        }
        else {
            searchMessage.setText("Песен не найдено");
            switchRecyclerView(false);
        }
    }

    private void setRecyclerDataAlbum(ArrayList<Album> newAlbums) {
        if (albumList == null)
            albumList = new ArrayList<>();
        if (albumList.size() != 0 || newAlbums.size() != 0) {
            switchRecyclerView(true);
            if (lastAlbumId == 0) {
                recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
                albumList.addAll(newAlbums);
                albumAdapter = new RecyclerViewAlbumAdapter(albumList);
                albumAdapter.setBottomReachedListener(new BottomReachedListener() {
                    @Override
                    public void onBottomReached(int position) {
                        if (needMore)
                            loadMore();
                    }
                });
                albumAdapter.setClickListener(new OnRecyclerItemClickListener() {
                    @Override
                    public void onItemClick(Object obj) {
                        Album album = (Album) obj;
                        openAlbum(album);
                    }

                    @Override
                    public void onItemClick(Object obj, int position) {

                    }
                });
                recyclerView.setAdapter(albumAdapter);
                lastAlbumId = albumList.get(albumList.size() - 1).get_ID();
            }
            else {
                if (newAlbums.size() != 0) {
                    albumList.addAll(newAlbums);
                    albumAdapter.notifyDataSetChanged();
                    lastAlbumId = albumList.get(albumList.size() - 1).get_ID();
                }
                else
                    needMore = false;
            }
        }
        else {
            searchMessage.setText("Альбомов не найдено");
            switchRecyclerView(false);
        }
    }

    private void loadMore() {
        loadMoreProgress.setVisibility(View.VISIBLE);
        switch (loadedData) {
            case LOAD_SONGS:
                loadSongs(lastQuery);
                break;
            case LOAD_ALBUMS:
                loadAlbums(lastQuery);
                break;
            case LOAD_ARTISTS:
                loadArtists(lastQuery);
                break;
        }
    }

    private enum RequiredData {
        LOAD_SONGS,
        LOAD_ALBUMS,
        LOAD_ARTISTS;
    }

    private void openAlbum(Album album) {
        if (getActivity() != null) {
            Bundle bundle = new Bundle();
            bundle.putString("data_type", "album");
            bundle.putLong("album_id", album.get_ID());
            ((BaseActivity) getActivity()).startSongList(bundle, album.getTitle());
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
