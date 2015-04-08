package com.infmme.githubtracker.app.util;

import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHIssueComment;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHThread;

import java.io.IOException;
import java.util.List;

/**
 * infm created it with love on 4/8/15. Enjoy ;)
 */
public class GHThreadPreview {
    public String timeLapsed;
    public String mainMessage;
    public String detailedMessage;

    public static GHThreadPreview fromGHThread(GHThread currThread) throws IOException {
        GHThreadPreview result = new GHThreadPreview();
        result.timeLapsed = currThread.getUpdatedAt().toString();
        result.mainMessage = currThread.getTitle();

        String type = currThread.getType();
        if ("Issue".equals(type)) {
            List<GHIssueComment> comments = currThread.getBoundIssue().getComments();
            if (!comments.isEmpty())
                result.detailedMessage = comments.get(comments.size() - 1).getBody();
        } else if ("PullReqeuest".equals(type)) {
            GHPullRequest pullRequest = currThread.getBoundPullRequest();
            List<GHIssueComment> comments = pullRequest.getComments();
            if (!comments.isEmpty())
                result.detailedMessage = comments.get(comments.size() - 1).getBody();
        } else if ("Commit".equals(type)) {
            GHCommit commit = currThread.getBoundCommit();
            result.detailedMessage = commit.getCommitShortInfo().getMessage();
        } else {
            result.detailedMessage = currThread.getRepository().getFullName();
        }
        return result;
    }

    private GHThreadPreview() {}
}
