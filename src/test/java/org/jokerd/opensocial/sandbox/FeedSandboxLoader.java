package org.jokerd.opensocial.sandbox;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jockerd.opensocial.feeds.FeedActivitiesCursor;
import org.jokerd.opensocial.api.model.DomainName;
import org.jokerd.opensocial.api.model.ObjectId;
import org.jokerd.opensocial.cursors.IActivityCursor;
import org.jokerd.opensocial.cursors.IActivityCursorProvider;
import org.jokerd.opensocial.cursors.StreamException;
import org.jokerd.opensocial.oauth.FileBasedOAuthInfoStore;
import org.jokerd.opensocial.oauth.OAuthHelper;
import org.jokerd.opensocial.scheduler.ActivityFormatterCursor;
import org.jokerd.opensocial.scheduler.ActivityStreamProvider;
import org.jokerd.opensocial.scheduler.DynamicActivityCursorProvider;
import org.jokerd.opensocial.twitter.TwitterActivitiesCursor;

public class FeedSandboxLoader {

    private static DomainName DN_FEED = new DomainName("feed");

    private static DomainName DN_TWITTER = new DomainName("twitter.com");

    private final static Logger log = Logger.getLogger(FeedSandboxLoader.class
        .getName());

    private static int MIN = (1000 /* one sec */* 60 /* one min */);

    static void handleError(String msg, Throwable e) {
        log.log(Level.FINE, msg, e);
    }

    public static void main(String[] args)
        throws IOException,
        InterruptedException {
        final ActivityStreamProvider streamProvider = new ActivityStreamProvider();
        streamProvider.setProvider(DN_FEED, new IActivityCursorProvider() {
            @Override
            public IActivityCursor getCursor(ObjectId streamId)
                throws StreamException {
                String feedUrl = streamId.getLocalIdDecoded();
                FeedActivitiesCursor cursor = new FeedActivitiesCursor(feedUrl);
                return cursor;
            }
        });
        String baseDir = "../../workdir";
        Map<String, String> map = OAuthHelper.getParams(args);
        final OAuthHelper oauthHelper = FileBasedOAuthInfoStore.getOAuthHelper(
            baseDir + "/access/twitter.com-oauth.json",
            baseDir + "/access/twitter.com-oauth.json-template",
            map);
        streamProvider.setProvider(DN_TWITTER, new IActivityCursorProvider() {
            @Override
            public IActivityCursor getCursor(ObjectId id)
                throws StreamException {
                // FIXME: get the oauthHelper depending on the twitter ID
                String url = "http://api.twitter.com/1/statuses/home_timeline.json?include_entities=true&count=200";
                IActivityCursor cursor = new TwitterActivitiesCursor(
                    oauthHelper,
                    url);
                return cursor;
            }
        });

        final IActivityCursorProvider decoratorProvider = new IActivityCursorProvider() {

            @Override
            public IActivityCursor getCursor(ObjectId parameter)
                throws StreamException {
                IActivityCursor cursor = streamProvider.getCursor(parameter);
                cursor = new ActivityFormatterCursor(cursor);
                // cursor = new ActivitySerializerStream(cursor);
                return cursor;
            }
        };

        final File dir = new File(baseDir + "/data");
        DynamicActivityCursorProvider cache = new DynamicActivityCursorProvider(
            decoratorProvider,
            dir);
        cache.open();
        schedule(
            cache,
            "http://www.nytimes.com/services/xml/rss/nyt/HomePage.xml",
            1);
        schedule(cache, "http://feeds.bbci.co.uk/news/rss.xml", 3);
        schedule(cache, "http://lacantine.org/events/feed.rss", 5);
        schedule(
            cache,
            "http://feeds.feedburner.com/JohnBattellesSearchblog",
            5);
        schedule(cache, "http://feedproxy.google.com/TechCrunch", 2);
        cache.schedule(new ObjectId(DN_TWITTER, "me"), 3 * MIN);

        Thread.sleep(100 * MIN);
        cache.close();
    }

    public static void schedule(
        DynamicActivityCursorProvider cache,
        String url,
        int time) {
        cache.schedule(new ObjectId(DN_FEED, url), time * MIN);
    }

}
