package lab.android.audiodementia.model;

import android.graphics.Bitmap;

public class Album {

    private long _ID;
    private String title;
    private String artist;
    private long artistId;
    private String releaseDate;
    private String smallCover;
    private String mediumCover;


    public Album(long _ID, String title, String artist, String smallCover, String mediumCover) {
        this._ID = _ID;
        this.title = title;
        this.artist = artist;
        this.smallCover = smallCover;
        this.mediumCover = mediumCover;
    }

    public long get_ID() {
        return _ID;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getSmallCover() {
        return smallCover;
    }

    public String getMediumCover() {
        return mediumCover;
    }
}
