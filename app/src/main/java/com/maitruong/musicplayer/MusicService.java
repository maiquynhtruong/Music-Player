package com.maitruong.musicplayer;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

/**

 */

public class MusicService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener{

    private MediaPlayer player;
    private ArrayList<Song> songList;
    private int songPos;
    private final IBinder musicBind = new MusicBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        songPos = 0;
        player = new MediaPlayer();
        initMusicPlayer();
    }

    void initMusicPlayer() {
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK); // Playback will continue when the device becomes idle
        player.setAudioStreamType(AudioManager.STREAM_MUSIC); //set the stream type to music.
        player.setOnPreparedListener(this); // when MediaPlayer is prepared
        player.setOnErrorListener(this); // when MediaPlayer got error
        player.setOnCompletionListener(this); // when MediaPlayer is completed
    }

    public void playSong() {
        player.reset(); // start music from the beginning
        Song curSong = songList.get(songPos);
        long id = curSong.getId();
        Uri songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
        try {
            player.setDataSource(getApplicationContext(), songUri);
        } catch (IOException e) {
            Log.d("MUSIC SERVICE", "Error setting data source");
            e.printStackTrace();
        }

        player.prepareAsync();
    }

    /**
     * In order for user to select song, we need to set the current song.
     * Will be called when user picks a song from a list.
     * @param songIndex
     */
    public void setSong(int songIndex) {
        songPos = songIndex;
    }
    /**
     * Receives a list of songs to play
     */
    public void setSongList(ArrayList<Song> songsList) {
        songList = songsList;
    }

    /**
     * Interface for communication between client and service
     */
    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // returns the interface for communication between the service and the components that called bindService()
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release(); // releases any resources attached to MediaPlayer object
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        playNext();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        player.start();
    }

    public void playNext() {
        songPos++;
        if (songPos >= songList.size()) songPos = 0;
        playSong();
    }

    public void playPrev() {
        songPos--;
        if (songPos < 0) songPos = songList.size() - 1;
        playSong();
    }

    /**
     * Because the media playback is in Service but the UI comes from Activity, and we already bound the
     * Activity instance to the Service instance so we could control playback from UI. Methods in Activity
     * class added to implement MediaPlayerControl interface will be called when the user attempts to control
     * playback. We will need the Service class to act on this control, and we need to implement these
     * methods in the Service class.
     */

    public void start() {
        player.start();
    }

    public void pause() {
        player.pause();
    }

    public int getDuration() {
        return player.getDuration();
    }

    public int getCurrentPosition() {
        return player.getCurrentPosition();
    }

    public void seekTo(int pos) {
        player.seekTo(pos);
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

}
