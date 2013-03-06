package org.jokerd.opensocial.sandbox;

import org.jokerd.opensocial.twitter.TwitterActivitiesCursor;

/**
 * @author kotelnikov
 */
public class TwitterActivitySandbox {

    public static void main(String[] args) throws Exception {
        SandboxUtils sandbox = new SandboxUtils("twitter.com");
        sandbox.showActivityStream(TwitterActivitiesCursor.class, args);
    }

}
