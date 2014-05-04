package com.olegkalugin.android.muzei.gopro;

import android.net.Uri;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class GoProService {
    public static final String WEBPAGE_URL = "http://gopro.com/photos/photo-of-the-day";

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
        return title;
    }

    private String getPhotoByline(Document document) {
        String byline = "";
        Elements elements = document.select("p.gray-font.medium-bottom-margin");
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
