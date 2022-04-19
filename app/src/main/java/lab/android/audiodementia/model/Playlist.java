package lab.android.audiodementia.model;

public class Playlist {

    private long _ID;
    private String title;
    private long trackCount;

    public Playlist(long _ID, String title, long trackCount) {
        this._ID = _ID;
        this.title = title;
        this.trackCount = trackCount;
    }

    public String getTitle() {
        return title;
    }

    public long getTrackCount() {
        return trackCount;
    }

    public long get_ID() {
        return _ID;
    }
}
