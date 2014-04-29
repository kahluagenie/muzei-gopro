package com.olegkalugin.muzei.gopro;

import android.content.Intent;
import android.net.Uri;

import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.MuzeiArtSource;

public class GoProPotdArtSource extends MuzeiArtSource {

    private static final String TAG = "GoProPOTD";
    private static final String SOURCE_NAME = "GoProPotdArtSource";

    private static final int ROTATE_TIME_MILLIS = 24 * 60 * 60 * 1000; // rotate every 24 hours

    public GoProPotdArtSource() {
        super(SOURCE_NAME);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setUserCommands(BUILTIN_COMMAND_ID_NEXT_ARTWORK);
    }

    @Override
    protected void onUpdate(int reason) {
        GoProService goProService = new GoProService();

        publishArtwork(new Artwork.Builder()
                .title("GoPro title")
                .byline("GoPro byline")
                .imageUri(goProService.getPhotoUrl())
                .token("goprotoken")
                .viewIntent(new Intent(Intent.ACTION_VIEW, Uri.parse("http://gopro.com/channel")))
                .build());

        scheduleUpdate(System.currentTimeMillis() + ROTATE_TIME_MILLIS);
    }
}
