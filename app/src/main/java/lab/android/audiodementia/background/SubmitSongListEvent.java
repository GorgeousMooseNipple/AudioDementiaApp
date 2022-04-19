package lab.android.audiodementia.background;

import java.util.ArrayList;

import lab.android.audiodementia.model.Song;

public class SubmitSongListEvent {

    private ArrayList<Song> songList;
    private int position;

    public SubmitSongListEvent(ArrayList<Song> songList, int position) {
        this.songList = songList;
        this.position = position;
    }

    public ArrayList<Song> getSongList() {
        return songList;
    }

    public int getPosition() {
        return position;
    }
}
