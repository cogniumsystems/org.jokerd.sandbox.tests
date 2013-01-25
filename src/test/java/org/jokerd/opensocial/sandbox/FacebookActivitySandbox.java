package org.jokerd.opensocial.sandbox;

import org.jokerd.opensocial.facebook.FacebookActivitiesCursor;

/**
 * @author kotelnikov
 */
public class FacebookActivitySandbox {

    public static void main(String[] args) throws Exception {
        SandboxUtils sandbox = new SandboxUtils("facebook.com");
        sandbox.showActivityStream(FacebookActivitiesCursor.class, args);
    }

}
