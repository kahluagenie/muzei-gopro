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

import android.net.Uri;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

public class GoProService {
    public static final String WEBPAGE_URL = "https://gopro.com/photos/photo-of-the-day/";

    public Photo getPhoto() {
        Document document;
        try {
            String url = WEBPAGE_URL + getDateString();
            document = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        Photo photo = new Photo();
        photo.uri = getPhotoUri(document);
        photo.title = getPhotoTitle(document);
        photo.byline = getPhotoByline(document);

        return photo;
    }

    /**
     * Create a token as "year/month/day". MONTH is zero-based, so increase by 1.
     * Use previous day if it's before 10 am PDT, since it's yesterday's photo.
     * It will also serve us as a URL appender to access previous photos.
     *
     * @return date for today's photo
     */
    public String getDateString() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
        if (calendar.get(Calendar.HOUR_OF_DAY) < 10) {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }

        return calendar.get(Calendar.YEAR)
                + "/" + String.valueOf(calendar.get(Calendar.MONTH) + 1)
                + "/" + calendar.get(Calendar.DAY_OF_MONTH);
    }

    private Uri getPhotoUri(Document document) {
        Uri uri = null;
        Elements elements = document.select("div.span12.text-center.black-background");
//        Elements elements = document.select("div.vid-wrap");
        if (!elements.isEmpty()) {
            Element div = elements.first();
            Element img = div.select("img").first();
            uri = Uri.parse(img.attr("src"));
        }
        return uri;
    }

    private String getPhotoTitle(Document document) {
        String title = document.title();
        title = title.replace("GoPro Photo Of The Day | ", "");
//        title = title.replace("GoPro Channel | ", "");
        return title;
    }

    private String getPhotoByline(Document document) {
        String byline = "";
        Elements elements = document.select("p.gray-font.medium-bottom-margin");
//        Elements elements = document.select("h6#video-author");
        if (!elements.isEmpty()) {
            Element p = elements.first();
            byline = p.text();
        }

        return byline;
    }

    public class Photo {
        Uri uri;
        String title;
        String byline;
    }

}
