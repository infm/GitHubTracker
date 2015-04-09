package com.infmme.githubtracker.app.util;

import com.infmme.githubtracker.app.R;
import org.kohsuke.github.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * infm created it with love on 4/8/15. Enjoy ;)
 */
public class GHThreadPreview {
    public String timeLapsed;
    public String mainMessage;
    public String detailedMessage;

    public int eventTypeResId;
    public String userPicUrl;

    public String threadUrl;

    public static GHThreadPreview fromGHThread(GHThread currThread) throws IOException {
        GHThreadPreview result = new GHThreadPreview();
        result.timeLapsed = new SimpleDateFormat("HH:mm MM/dd")
                .format(currThread.getUpdatedAt());
        result.mainMessage = currThread.getTitle();

        String type = currThread.getType();
        if ("Issue".equals(type)) {
            GHIssue issue = currThread.getBoundIssue();
            List<GHIssueComment> comments = issue.getComments();
            GHIssueComment comment = null;
            if (!comments.isEmpty()) {
                comment = comments.get(comments.size() - 1);
                result.detailedMessage = comment.getBody();
            }
            result.eventTypeResId = R.drawable.issue_opened;
            result.userPicUrl = (null != comment) ? comment.getUser().getAvatarUrl() : "";
            result.threadUrl = (null != comment) ? issue.getHtmlUrl().toString() : "";
        } else if ("PullRequest".equals(type)) {
            GHPullRequest pullRequest = currThread.getBoundPullRequest();
            List<GHIssueComment> comments = pullRequest.getComments();
            GHIssueComment comment = null;
            if (!comments.isEmpty()) {
                comment = comments.get(comments.size() - 1);
                result.detailedMessage = comment.getBody();
            }
            result.eventTypeResId = R.drawable.pull_request;
            result.userPicUrl = (null != comment) ? comment.getUser().getAvatarUrl() : "";
            result.threadUrl = (null != comment) ? pullRequest.getHtmlUrl().toString() : "";
        } else if ("Commit".equals(type)) {
            GHCommit commit = currThread.getBoundCommit();
            result.detailedMessage = commit.getCommitShortInfo().getMessage();
            result.eventTypeResId = R.drawable.git_commit;
            result.userPicUrl = commit.getAuthor().getAvatarUrl();
            result.threadUrl = commit.getOwner().getSvnUrl() + "/commit/" + commit.getSHA1();
        } else {
            result.detailedMessage = currThread.getRepository().getFullName();
            result.eventTypeResId = R.mipmap.ic_launcher;
            result.userPicUrl = "";
        }

        return result;
    }

    private GHThreadPreview() {}
}
