package org.jokerd.opensocial.sandbox;

import java.io.IOException;
import java.util.List;

import org.jokerd.opensocial.api.model.ActivityEntry;
import org.jokerd.opensocial.api.model.ActivityObject;
import org.jokerd.opensocial.api.model.Person;
import org.jokerd.opensocial.cursors.ActivityListCursor;
import org.jokerd.opensocial.facebook.FacebookeActivityBuilder;
import org.jokerd.opensocial.facebook.FacebookePersonProfileBuilder;
import org.jokerd.opensocial.facebook.calls.FacebookCallHandler;
import org.jokerd.opensocial.oauth.OAuthHelper;
import org.ubimix.commons.json.JsonObject;
import org.ubimix.commons.json.rpc.IRpcCallHandler;
import org.ubimix.commons.json.rpc.IRpcCallHandler.IRpcCallback;
import org.ubimix.commons.json.rpc.RpcRequest;
import org.ubimix.commons.json.rpc.RpcResponse;

/**
 * @author kotelnikov
 */
public class FacebookCallSandbox {

    public static void main(String[] args) throws Exception {
        new FacebookCallSandbox().run();
    }

    private final String ACTIVITIES_URL = "https://graph.facebook.com/me/home";

    private final IRpcCallHandler fCallDispatcher;

    private int fIdCounter;

    private final SandboxUtils fSandbox = new SandboxUtils("facebook.com");

    private final String PROFILE_URL = "https://graph.facebook.com/me/";

    public FacebookCallSandbox() throws IOException {
        OAuthHelper helper = fSandbox.newOAuthHelper();
        fCallDispatcher = new FacebookCallHandler(helper);
    }

    private RpcRequest newRequest(String url) {
        String callId = "id" + fIdCounter++;
        return new RpcRequest(callId, url, new JsonObject());
    }

    public void run() {
        fCallDispatcher.handle(newRequest(PROFILE_URL), new IRpcCallback() {
            @Override
            public void finish(RpcResponse response) {
                if (response.hasErrors()) {
                    System.out.println("Error! " + response.getError());
                } else {
                    JsonObject result = response.getResultObject();
                    FacebookePersonProfileBuilder profileBuilder = new FacebookePersonProfileBuilder();
                    Person person = profileBuilder.getPersonInfo(result);
                    showActivities(person);
                }
            }
        });
    }

    private void showActivities(final ActivityObject streamSourceInfo) {
        fCallDispatcher.handle(newRequest(ACTIVITIES_URL), new IRpcCallback() {
            @Override
            public void finish(RpcResponse response) {
                if (response.hasErrors()) {
                    System.out.println("Error! " + response.getError());
                } else {
                    JsonObject result = response.getResultObject();
                    FacebookeActivityBuilder builder = new FacebookeActivityBuilder(
                        result,
                        streamSourceInfo);
                    List<ActivityEntry> activities = builder.getActivities();
                    ActivityListCursor cursor = new ActivityListCursor(
                        activities);
                    try {
                        fSandbox.showActivityStream(cursor);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

}
