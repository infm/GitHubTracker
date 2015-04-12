package com.infmme.githubtracker.app.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.infmme.githubtracker.app.util.GHThreadPreview;
import org.kohsuke.github.GHNotificationStream;
import org.kohsuke.github.GHThread;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * infm created it with love on 4/7/15. Enjoy ;)
 */
public class FetchService extends IntentService {
    private static final String LAST_UPDATED_KEY = "last_updated";
    private static final String ACCESS_TOKEN_KEY = "accessToken";

    private static final String LOGTAG = FetchService.class.getName();

    public FetchService() {
        super(FetchService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Context context = getApplicationContext();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String accessToken = prefs.getString(ACCESS_TOKEN_KEY, "invalid");

            GitHub github = authorize(accessToken);
            if (null != github) {
                Log.d(LOGTAG, "Me: " + github.getMyself().getName());
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(prefs.getLong(LAST_UPDATED_KEY, 0));
                Date lastUpdated = calendar.getTime();

                GHNotificationStream stream = github.listNotifications().since(lastUpdated);
                stream.nonBlocking(true);
                final List<GHThreadPreview> threadList = new ArrayList<GHThreadPreview>();
                for (GHThread thread : stream)
                    threadList.add(GHThreadPreview.fromGHThread(thread));
                Log.d(LOGTAG, String.format("Fetched %d notifications from %s",
                                            threadList.size(),
                                            new SimpleDateFormat("dd/MM/yy HH: mm")
                                                    .format(lastUpdated)));
                for (GHThreadPreview thp : threadList)
                    thp.addToDb(context);

                prefs.edit().putLong(LAST_UPDATED_KEY, System.currentTimeMillis()).commit();
            } else {
                prefs.edit().putString(ACCESS_TOKEN_KEY, "invalid").commit();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private GitHub authorize(String accessToken) {
        try {
            String meaningfulPart = accessToken
                    .substring(0, accessToken.indexOf('&'));
            GitHub github = GitHub.connectUsingOAuth(meaningfulPart);
            if (!github.isCredentialValid()) {
                Log.e(LOGTAG, "Auth failed " + github);
                return null;
            }
            return github;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
