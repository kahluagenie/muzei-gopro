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

public class GoProService {
    public static final String WEBPAGE_URL = "https://gopro.com/photos/photo-of-the-day/";

    public Photo getPhoto() {
        Document document;
        try {
            document = Jsoup.connect(WEBPAGE_URL).get();
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
