package com.olegkalugin.muzei.gopro;

import android.net.Uri;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoProService {

    public Uri getPhotoUrl() {
        StringBuilder webPage = new StringBuilder();
        try {
            webPage = fetchWebPage(new URL("http://gopro.com/photos/photo-of-the-day"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        String prefix = "span12 text-center black-background'><img src='";
        String postfix = "'></div>";
        Pattern pattern = Pattern.compile(prefix + "(.*?)" + postfix, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(webPage);
        String photoUrl = "";
        if (matcher.find()) {
            photoUrl = matcher.group();
            photoUrl = photoUrl.replace(prefix, "");
            photoUrl = photoUrl.replace(postfix, "");
        }

        return Uri.parse(photoUrl);
    }

    private StringBuilder fetchWebPage(URL url) {
        InputStream is = null;
        BufferedReader br;
        String line;
        StringBuilder result = new StringBuilder();

        try {
            is = url.openStream();
            br = new BufferedReader(new InputStreamReader(is));

            while ((line = br.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ioe) {
                // nothing to see here
            }
        }

        return result;
    }

}
