package com.infmme.githubtracker.app.service;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import org.kohsuke.github.GHNotificationStream;
import org.kohsuke.github.GitHub;

import java.io.IOException;

/**
 * infm created it with love on 4/7/15. Enjoy ;)
 */
public class FetchService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public FetchService(String name) {
        super(FetchService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String accessToken = intent.getStringExtra("accessToken");
        if (!TextUtils.isEmpty(accessToken) && !"invalid".equals(accessToken)) {
            final String logTag = "FetchData";
            try {
                GitHub github =
                        GitHub.connectUsingOAuth("56dd50eb5a9fc8966693333e4b54aae59da58535");
                if (!github.isCredentialValid()) {
                    Log.e(logTag, "Auth failed " + github);
                    return;
                }
                Log.d(logTag, "Me: " + github.getMyself().getName());
                GHNotificationStream stream = github.listNotifications();
                stream.nonBlocking(true);
/*
                        for (GHThread thread : stream) {
                            Log.d(logTag, String.format("Repo: %s; Title: %s; type: %s; reason: " +
                                                                "%s;",
                                                        thread.getRepository(),
                                                        thread.getTitle(), thread.getType(),
                                                        thread.getReason()));
                        }
*/
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
