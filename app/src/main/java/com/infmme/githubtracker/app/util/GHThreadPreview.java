package com.infmme.githubtracker.app.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import com.infmme.githubtracker.app.R;
import com.infmme.githubtracker.app.data.NotificationContract;
import com.infmme.githubtracker.app.data.NotificationDbHelper;
import com.infmme.githubtracker.app.data.NotificationsContentProvider;
import org.kohsuke.github.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * infm created it with love on 4/8/15. Enjoy ;)
 */
public class GHThreadPreview {
    public String timeLapsed;
    public String mainMessage;
    public String detailedMessage;

    public int eventTypeResId;
    public String userPicUrl;
    public String userPicPath;

    public String threadUrl;
    public String repoName;

    private String eventTypeRaw;
    private String userName;

    private static final Map<String, Integer> eventTypeMap;

    static {
        eventTypeMap = new HashMap<String, Integer>();
        eventTypeMap.put("Issue", R.drawable.issue_opened);
        eventTypeMap.put("PullRequest", R.drawable.pull_request);
        eventTypeMap.put("Commit", R.drawable.git_commit);
    }

    public static GHThreadPreview fromCursor(Cursor c) {
        GHThreadPreview result = null;
        if (null != c) {
            result = new GHThreadPreview();
            result.timeLapsed = c.getString(NotificationDbHelper.CNUM_TIME);
            result.mainMessage = c.getString(NotificationDbHelper.CNUM_TITLE);
            result.detailedMessage = c.getString(NotificationDbHelper.CNUM_INFO);

            result.eventTypeRaw = c.getString(NotificationDbHelper.CNUM_TYPE);
            result.eventTypeResId = (eventTypeMap.containsKey(result.eventTypeRaw))
                    ? eventTypeMap.get(result.eventTypeRaw)
                    : R.mipmap.ic_launcher;

            result.userPicPath = c.getString(NotificationDbHelper.CNUM_USER_PIC);
            result.threadUrl = c.getString(NotificationDbHelper.CNUM_THREAD_URL);
            result.repoName = c.getString(NotificationDbHelper.CNUM_REPO_NAME);
        }
        return result;
    }

    public static GHThreadPreview fromGHThread(GHThread currThread) throws IOException {
        GHThreadPreview result = new GHThreadPreview();
        result.timeLapsed = new SimpleDateFormat("MM/dd HH:mm")
                .format(currThread.getUpdatedAt());
        result.mainMessage = currThread.getTitle();

        result.eventTypeRaw = currThread.getType();
        if ("Issue".equals(result.eventTypeRaw)) {
            GHIssue issue = currThread.getBoundIssue();
            List<GHIssueComment> comments = issue.getComments();
            if (!comments.isEmpty()) {
                GHIssueComment comment = comments.get(comments.size() - 1);
                result.detailedMessage = comment.getBody();
                result.userPicUrl = comment.getUser().getAvatarUrl();
            } else {
                result.detailedMessage = issue.getBody();
                result.userPicUrl = issue.getUser().getAvatarUrl();
            }
            result.threadUrl = issue.getHtmlUrl().toString();
            result.userName = issue.getUser().getLogin();
            result.repoName = issue.getRepository().getFullName();
        } else if ("PullRequest".equals(result.eventTypeRaw)) {
            GHPullRequest pullRequest = currThread.getBoundPullRequest();
            List<GHIssueComment> comments = pullRequest.getComments();
            if (!comments.isEmpty()) {
                GHIssueComment comment = comments.get(comments.size() - 1);
                result.detailedMessage = comment.getBody();
                result.userPicUrl = comment.getUser().getAvatarUrl();
            } else {
                result.detailedMessage = pullRequest.getBody();
                result.userPicUrl = pullRequest.getUser().getAvatarUrl();
            }
            result.threadUrl = pullRequest.getHtmlUrl().toString();
            result.userName = pullRequest.getUser().getLogin();
            result.repoName = pullRequest.getRepository().getFullName();
        } else if ("Commit".equals(result.eventTypeRaw)) {
            GHCommit commit = currThread.getBoundCommit();
            result.detailedMessage = commit.getCommitShortInfo().getMessage();
            result.userPicUrl = commit.getAuthor().getAvatarUrl();
            result.threadUrl = commit.getOwner().getSvnUrl() + "/commit/" + commit.getSHA1();
            result.userName = commit.getAuthor().getLogin();
            result.repoName = commit.getOwner().getFullName();
        } else {
            result.detailedMessage = "ERROR OCCURRED";
            result.userPicUrl = currThread.getRepository().getOwner().getAvatarUrl();
            result.threadUrl = currThread.getRepository().getSvnUrl();
            result.userName = currThread.getRepository().getOwner().getLogin();
            result.repoName = "SOME SHIT";
        }

        result.eventTypeResId = (eventTypeMap.containsKey(result.eventTypeRaw))
                ? eventTypeMap.get(result.eventTypeRaw)
                : R.mipmap.ic_launcher;
        return result;
    }

    public void addToDb(Context context) throws IOException {
        ContentValues cv = new ContentValues();
        cv.put(NotificationContract.NotificationEntry.COLUMN_TYPE, eventTypeRaw);
        cv.put(NotificationContract.NotificationEntry.COLUMN_TITLE, mainMessage);
        cv.put(NotificationContract.NotificationEntry.COLUMN_TIME, timeLapsed);

        userPicPath = savePic(context);
        if (null != userPicPath)
            cv.put(NotificationContract.NotificationEntry.COLUMN_USER_PIC, userPicPath);
        else
            throw new IOException("Path to a picture is null");

        cv.put(NotificationContract.NotificationEntry.COLUMN_INFO, detailedMessage);
        cv.put(NotificationContract.NotificationEntry.COLUMN_THREAD_URL, threadUrl);
        cv.put(NotificationContract.NotificationEntry.COLUMN_REPO_NAME, repoName);

        context.getContentResolver().insert(NotificationsContentProvider.CONTENT_URI, cv);
    }

    private GHThreadPreview() {}

    private String savePic(Context context) throws IOException {
        String outputName = userName + "-thumbnail.jpg";
        if (context.getFileStreamPath(outputName).exists())
            return outputName;

        URL url = new URL(userPicUrl);

        InputStream input = null;
        FileOutputStream output = null;

        try {

            input = url.openConnection().getInputStream();
            output = context.openFileOutput(outputName, Context.MODE_PRIVATE);

            int read;
            byte[] data = new byte[1024];
            while ((read = input.read(data)) != -1)
                output.write(data, 0, read);

            return outputName;
        } finally {
            if (output != null)
                output.close();
            if (input != null)
                input.close();
        }
    }
}
