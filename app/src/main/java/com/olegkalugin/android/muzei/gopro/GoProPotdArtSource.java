/*
   Copyright 2014 Oleg Kalugin

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.olegkalugin.android.muzei.gopro;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;
import com.google.android.apps.muzei.api.UserCommand;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import retrofit.RestAdapter;

public class GoProPotdArtSource extends RemoteMuzeiArtSource {
    private static final String TAG = "GoProPOTD";
    private static final String SOURCE_NAME = "GoProPotdArtSource";
    private static final int CUSTOM_COMMAND_ID_SHARE = 1;
    private static final String POTD_URL = "http://potd.olegkalugin.com";

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
        Date date = new Date();
        DateFormat potdDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateParam = potdDateFormat.format(date);
        Photo photo = getPhoto(dateParam);

        if (photo == null || photo.uri == null) {
            throw new RetryException();
        }

        if (getCurrentArtwork() == null || !photo.title.equals(getCurrentArtwork().getTitle())) {
            publishArtwork(new Artwork.Builder()
                    .title(photo.title)
                    .byline(photo.byline)
                    .imageUri(Uri.parse(photo.uri))
                    .token(dateParam)
                    .viewIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(photo.source)))
                    .build());
        }

        scheduleUpdate(calculateNextUpdateTime());
    }

    private Photo getPhoto(String dateParam) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(POTD_URL)
                .build();

        PotdService potdService = restAdapter.create(PotdService.class);
        return potdService.getPhoto(dateParam);
    }

    /**
     * Schedule updates for 12 am and 12 pm.
     *
     * @return next update time in milliseconds
     */
    private long calculateNextUpdateTime() {
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

        if (hourOfDay < 12) {
            calendar.set(Calendar.HOUR_OF_DAY, 12);
        } else {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
        }

        calendar.set(Calendar.MINUTE, 0);
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

    public class Photo {
        String uri;
        String title;
        String byline;
        String source;
    }
}
