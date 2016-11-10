package com.maitruong.musicplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 */

public class SongAdapter extends BaseAdapter {
    private ArrayList<Song> songList;
    private LayoutInflater inflater;

    public SongAdapter(Context context, ArrayList<Song> songsList) {
        songList = songsList;
        inflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return songList.size();
    }

    @Override
    public Object getItem(int position) {
        return songList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout songLayout = (LinearLayout) inflater.inflate(R.layout.song, null);
        TextView songTitle = (TextView) songLayout.findViewById(R.id.songTitle);
        TextView songArtist = (TextView) songLayout.findViewById(R.id.songArtist);
        Song curSong = songList.get(position);
        songArtist.setText(curSong.getArtist());
        songTitle.setText(curSong.getTitle());
        songLayout.setTag(position); // to be retrieved in getSong() in MainActivity
        return songLayout;
    }
}
