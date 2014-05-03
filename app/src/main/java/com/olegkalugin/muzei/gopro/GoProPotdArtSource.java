package com.olegkalugin.muzei.gopro;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;
import com.google.android.apps.muzei.api.UserCommand;

import java.util.Calendar;
import java.util.TimeZone;

public class GoProPotdArtSource extends RemoteMuzeiArtSource {
    private static final String TAG = "GoProPOTD";
    private static final String SOURCE_NAME = "GoProPotdArtSource";
    private static final int CUSTOM_COMMAND_ID_SHARE = 1;

    public GoProPotdArtSource() {
        super(SOURCE_NAME);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        UserCommand nextArtworkCommand = new UserCommand(BUILTIN_COMMAND_ID_NEXT_ARTWORK);
        UserCommand shareCommand = new UserCommand(CUSTOM_COMMAND_ID_SHARE, "Share photo");
        setUserCommands(nextArtworkCommand, shareCommand);
    }

    @Override
    protected void onTryUpdate(int reason) throws RetryException {
        GoProService goProService = new GoProService();
        GoProService.Photo photo = goProService.getPhoto();

        if (photo == null || photo.uri == null) {
            throw new RetryException();
        }

        if (!photo.uri.equals(getCurrentArtwork().getImageUri())) {
            String token = createToken();
            publishArtwork(new Artwork.Builder()
                    .title(photo.title)
                    .byline(photo.byline)
                    .imageUri(photo.uri)
                    .token(token)
                    .viewIntent(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(GoProService.WEBPAGE_URL + "/" + token)))
                    .build());
        }

        scheduleUpdate(calculateNextUpdateTime());
    }

    /**
     * Create a token as "year/month/day". MONTH is zero-based, so increase by 1.
     * Use previous day if it's before 10 am PDT, since it's yesterday's photo.
     * It will also serve us as a URL appender to access previous photos.
     *
     * @return token for the photo
     */
    private String createToken() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
        if (calendar.get(Calendar.HOUR_OF_DAY) < 10) {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }

        return calendar.get(Calendar.YEAR)
                + "/" + String.valueOf(calendar.get(Calendar.MONTH) + 1)
                + "/" + calendar.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * GoPro seems to update the photo at 10 am PDT every day.
     * We'll schedule updates at 10:15 am and 10 pm just in case they delay it for some reason.
     *
     * @return next update time in milliseconds
     */
    private long calculateNextUpdateTime() {
        // Do all date manipulations in PDT
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

        if (hourOfDay >= 10 && hourOfDay < 22) {
            calendar.set(Calendar.HOUR_OF_DAY, 22);
            calendar.set(Calendar.MINUTE, 0);
        } else {
            if (hourOfDay >= 22) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            calendar.set(Calendar.HOUR_OF_DAY, 10);
            calendar.set(Calendar.MINUTE, 15);
        }

        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    @Override
    protected void onCustomCommand(int id) {
        super.onCustomCommand(id);
        if (CUSTOM_COMMAND_ID_SHARE == id) {
            Artwork currentArtwork = getCurrentArtwork();
            if (currentArtwork == null) {
                Log.w(TAG, "No current artwork, can't share.");
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(GoProPotdArtSource.this,
                                R.string.goprophoto_source_error_no_photo_to_share,
                                Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }

            String detailUrl = currentArtwork.getViewIntent().getDataString();
            String artist = currentArtwork.getByline();

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "My Android wallpaper today is '"
                    + currentArtwork.getTitle().trim()
                    + "' " + artist
                    + ". #MuzeiGoPro\n\n"
                    + detailUrl);
            shareIntent = Intent.createChooser(shareIntent, "Share photo");
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(shareIntent);

        }
    }
}
