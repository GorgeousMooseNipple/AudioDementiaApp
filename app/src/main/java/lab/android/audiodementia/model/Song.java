package lab.android.audiodementia.model;

public class Song {

    private long _ID;
    private String title;
    private long artistId;
    private String artist;
    private long albumId;
    private String album;
    private long duration;
    private String smallCoverUrl;
    private String mediumCoverUrl;
    private String uri;


    public Song(long _ID, String title, String artist, String album, long duration,
                String smallCoverUrl, String mediumCoverUrl, String uri) {
        this._ID = _ID;
        if (title == null)
            title = "Untitled";
        this.title = title;
        if (artist == null)
            artist = "Unknown";
        this.artist = artist;
        if (album == null)
            album = "Unknown";
        this.album = album;
        this.duration = duration;
        this.smallCoverUrl = smallCoverUrl;
        this.mediumCoverUrl = mediumCoverUrl;
        this.uri = uri;
    }

    public long get_ID() {
        return _ID;
    }

    public String getTitle() {
        return title;
    }

    public long getArtistId() {
        return artistId;
    }

    public String getArtist() {
        return artist;
    }

    public long getAlbumId() {
        return albumId;
    }

    public String getAlbum() {
        return album;
    }

    public long getDuration() {
        return duration;
    }

    public String getSmallCoverUrl() {
        return smallCoverUrl;
    }

    public String getMediumCoverUrl() {
        return mediumCoverUrl;
    }

    public String getUri() {
        return uri;
    }
}
