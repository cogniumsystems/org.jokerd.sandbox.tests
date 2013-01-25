package org.jokerd.opensocial.sandbox;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jockerd.opensocial.feeds.FeedActivitiesCursor;
import org.jokerd.opensocial.cursors.ActivityMergeCursor;
import org.jokerd.opensocial.cursors.IActivityCursor;

public class FeedSandbox {

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
        List<IActivityCursor> streams = FeedActivitiesCursor
            .getFeedCursors(urls);
        // streams.add(tweetCursor);
        IActivityCursor cursor = new ActivityMergeCursor(streams);
        SandboxUtils.printActivities(outFile, cursor);
    }

}
