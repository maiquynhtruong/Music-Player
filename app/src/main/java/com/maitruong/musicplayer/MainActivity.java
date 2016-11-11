package com.maitruong.musicplayer;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.MediaController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.jar.Manifest;

public class MainActivity extends AppCompatActivity implements MediaController.MediaPlayerControl{

    private ArrayList<Song> songList;
    private ListView songView;
    private final static int READ_EXTERNAL_STORAGE_PERMISSION = 1;
    private MusicService musicService;
    private Intent playIntent;
    private boolean musicBound = false; // Will control Service from the main class, so we will need to
                                        // bind the service with this class
    private MusicController controller;
    private boolean paused = false; // to interact with the control when user returning to app after leaving it
    private boolean playBackPaused = false; // interact with the control when playback is paused


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        songView = (ListView) findViewById(R.id.song_list);

        songList = new ArrayList<Song>();

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_EXTERNAL_STORAGE_PERMISSION);
        } else {
            getSongList();
        }
        Collections.sort(songList, new Comparator<Song>() {
            @Override
            public int compare(Song o1, Song o2) {
                return o1.getTitle().compareTo(o2.getTitle());
            }
        });

        SongAdapter adapter = new SongAdapter(this, songList);
        songView.setAdapter(adapter);

        setUpController();


    }

    // connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // IBinder is the communication channel
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService(); // Service instance that MainActivity can interact with
            musicService.setSongList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    protected void onStart() {
        // start Service instance or create one when MainActivity idnstance start. This service will be connected by musicConnection and will be passed list of songs.
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class); // specifies what service to start
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE); // when a connection is made,
                                                                                // pass the song list and have control of the service latter
            startService(playIntent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (paused) {
            setUpController();
            paused = false;
        }
    }

    @Override
    protected void onStop() {
        controller.hide();
        super.onStop();
    }

    public void getSongList(){
        ContentResolver resolver = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        Cursor cursor = resolver.query(songUri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int titleCol = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int idCol = cursor.getColumnIndex(MediaStore.Audio.Media._ID);

            do {
                String artist = cursor.getString(artistCol);
                String title = cursor.getString(titleCol);
                long id = cursor.getLong(idCol);
                Song curSong = new Song(id, artist, title);
                songList.add(curSong);
            } while (cursor.moveToNext());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getSongList();
            }
        }
    }

    /**
     * Set the song and play it
     * @param view
     */
    public void songPicked(View view) {
        musicService.setSong(Integer.parseInt(view.getTag().toString()));
        musicService.playSong();
        if (playBackPaused) {
            setUpController();
            playBackPaused = false;

        }
        controller.show();
    }

    /**
     * Implements end button
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_shuffle:
                musicService.setShuffle();
                break;
            case R.id.action_end:
                stopService(playIntent);
                musicService = null;
                //System.exit(0); // will exit out of the app
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        unbindService(musicConnection);
        stopService(playIntent);
        musicService = null;
        super.onDestroy();
    }

    private void setUpController() {
        controller = new MusicController(this);

        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
                controller.show();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
                controller.show();
            }
        });
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.song_list));
        controller.setEnabled(true);

    }

    public void playNext() {
        musicService.playNext();
        if (playBackPaused) {
            setUpController();
            playBackPaused = false;
        }
        controller.show();
    }

    public void playPrev() {
        musicService.playPrev();
        if (playBackPaused) {
            // reset controller and update playBackPaused
            setUpController();
            playBackPaused = false;
        }
        controller.show();
    }

    @Override
    public void start() {
        musicService.start();
    }

    @Override
    public void pause() {
        musicService.pause();
        playBackPaused = true;
    }

    @Override
    public int getDuration() {
        if (musicService != null && musicBound && musicService.isPlaying())
            return musicService.getDuration();
        else
            return 0;
    }

    @Override
    public int getCurrentPosition() {
        return musicService.getCurrentPosition();
    }

    @Override
    public void seekTo(int pos) {
        musicService.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        if (musicService != null && musicBound)
            return musicService.isPlaying();
        else
            return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
}
