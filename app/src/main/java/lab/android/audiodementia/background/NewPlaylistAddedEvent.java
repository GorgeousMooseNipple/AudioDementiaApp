package lab.android.audiodementia.background;

import lab.android.audiodementia.model.Playlist;

public class NewPlaylistAddedEvent {

    private boolean success;
    private String message;
    private Playlist playlist;

    public NewPlaylistAddedEvent(boolean success, String message, Playlist playlist) {
        this.success = success;
        this.message = message;
        this.playlist = playlist;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Playlist getPlaylist() {
        return playlist;
    }

}
