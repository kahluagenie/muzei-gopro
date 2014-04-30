package com.olegkalugin.muzei.gopro;

import android.content.Intent;
import android.net.Uri;

import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;

public class GoProPotdArtSource extends RemoteMuzeiArtSource {
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
    protected void onTryUpdate(int reason) throws RetryException {
        GoProService goProService = new GoProService();
        GoProService.Photo photo = goProService.getPhoto();

        if (photo == null || photo.uri == null) {
            throw new RetryException();
        }

        publishArtwork(new Artwork.Builder()
                .title(photo.title)
                .byline(photo.byline)
                .imageUri(photo.uri)
                .viewIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(GoProService.WEBPAGE_URL)))
                .build());

        scheduleUpdate(System.currentTimeMillis() + ROTATE_TIME_MILLIS);
    }
}
