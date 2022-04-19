package lab.android.audiodementia.model;

public class Genre {

    private long _ID;
    private String genreName;

    public Genre(long _ID, String genreName) {
        this._ID = _ID;
        this.genreName = genreName;
    }

    public long get_ID() {
        return _ID;
    }

    public String getGenreName() {
        return genreName;
    }
}
