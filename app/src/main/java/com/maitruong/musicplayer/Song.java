package com.maitruong.musicplayer;

/**
 *
 */

public class Song {
    private long id;
    private String artist;
    private String title;

    public Song(long ID, String songArtist, String songTitle) {
        id = ID;
        artist = songArtist;
        title = songTitle;
    }

    public long getId() {return id;};
    public String getArtist() {return artist;}
    public String getTitle() {return title;}
}
