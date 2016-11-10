package com.maitruong.musicplayer;

import android.content.Context;
import android.view.View;
import android.widget.MediaController;

/**
 */

public class MusicController extends MediaController {

    public MusicController(Context context) {
        super(context);
    }

    @Override
    public void hide() {
        // stop it from hiding automatically every three seconds
    }



}
