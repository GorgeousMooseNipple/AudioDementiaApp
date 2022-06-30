package lab.android.audiodementia.background;

import java.net.HttpURLConnection;

import lab.android.audiodementia.model.Playlist;

public class NewPlaylistAddedEvent {

    private boolean success;
    private int statusCode;
    private String message;
    private Playlist playlist;

    public NewPlaylistAddedEvent(boolean success, int statusCode, String message, Playlist playlist) {
        this.success = success;
        this.statusCode = statusCode;
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

    public boolean isUnauthorized() {
        return this.statusCode == HttpURLConnection.HTTP_UNAUTHORIZED;
    }
}
