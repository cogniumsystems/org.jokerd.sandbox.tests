package org.jokerd.opensocial.sandbox;

import org.jokerd.opensocial.tweeter.TweetActivitiesCursor;

/**
 * @author kotelnikov
 */
public class TwitterActivitySandbox {

    public static void main(String[] args) throws Exception {
        SandboxUtils sandbox = new SandboxUtils("twitter.com");
        sandbox.showActivityStream(TweetActivitiesCursor.class, args);
    }

}
