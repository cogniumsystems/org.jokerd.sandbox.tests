package org.jokerd.opensocial.sandbox;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jockerd.opensocial.feeds.FeedActivitiesCursor;
import org.jokerd.opensocial.cursors.ActivityMergeCursor;
import org.jokerd.opensocial.cursors.IActivityCursor;
import org.jokerd.opensocial.facebook.FacebookActivitiesCursor;
import org.jokerd.opensocial.twitter.TwitterActivitiesCursor;

public class FeedTwitterFacebookSandbox {

    private static String[] array(String... string) {
        return string;
    }

    public static void main(String[] args) throws IOException {

        String outFileName = "./out.json";
        File outFile = new File(outFileName);
        String[] urls = array(
            "http://www.nytimes.com/services/xml/rss/nyt/HomePage.xml",
            "http://feeds.bbci.co.uk/news/rss.xml",
            "http://www.lemonde.fr/rss/une.xml",
            "http://lacantine.org/events/feed.rss");
        List<IActivityCursor> streams = new ArrayList<IActivityCursor>();

        streams.addAll(FeedActivitiesCursor.getFeedCursors(urls));

        SandboxUtils fbUtils = new SandboxUtils("facebook.com");
        FacebookActivitiesCursor fbCursor = new FacebookActivitiesCursor(
            fbUtils.newOAuthHelper());
        streams.add(new TryCatchActivityCursor(fbCursor));

        SandboxUtils twUtils = new SandboxUtils("twitter.com");
        String url = "http://api.twitter.com/1/statuses/home_timeline.json?include_entities=true&count=200";
        TwitterActivitiesCursor twCursor = new TwitterActivitiesCursor(
            twUtils.newOAuthHelper(),
            url);
        streams.add(twCursor);

        IActivityCursor cursor = new ActivityMergeCursor(streams);
        SandboxUtils.printActivities(outFile, cursor);
    }

}
