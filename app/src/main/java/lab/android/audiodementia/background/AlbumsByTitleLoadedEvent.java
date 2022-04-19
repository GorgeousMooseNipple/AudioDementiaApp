package lab.android.audiodementia.background;


import java.util.ArrayList;

import lab.android.audiodementia.model.Album;

public class AlbumsByTitleLoadedEvent {

    private boolean success;
    private String message;
    private ArrayList<Album> albumList;

    public AlbumsByTitleLoadedEvent(boolean success, String message, ArrayList<Album> albumList) {
        this.success = success;
        this.message = message;
        this.albumList = albumList;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public ArrayList<Album> getAlbumList() {
        return albumList;
    }
}
