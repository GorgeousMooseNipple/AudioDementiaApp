package lab.android.audiodementia.background;


import java.util.ArrayList;

import lab.android.audiodementia.model.Song;

public class SongsLoadedEvent {

    private boolean success;
    private String message;
    private ArrayList<Song> songList;

    public SongsLoadedEvent(boolean success, String message, ArrayList<Song> songList) {
        this.success = success;
        this.message = message;
        this.songList = songList;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public ArrayList<Song> getSongList() {
        return songList;
    }
}
