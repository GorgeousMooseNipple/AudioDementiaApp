package lab.android.audiodementia.background;

import java.util.ArrayList;
import lab.android.audiodementia.model.Playlist;

public class PlaylistsUploadedEvent extends ResponseEvent<ArrayList<Playlist>>{
    public PlaylistsUploadedEvent(boolean successfull,
                                  String message,
                                  ArrayList<Playlist> playlists) {
        this.successful = successfull;
        this.message = message;
        this.data = playlists;
    }
}
