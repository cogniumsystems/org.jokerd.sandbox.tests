/**
 * 
 */
package org.jokerd.opensocial.api.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jockerd.opensocial.feeds.FeedActivityBuilder;
import org.jokerd.opensocial.api.events.Activitystreams.Get;
import org.jokerd.opensocial.api.model.ActivityEntry;
import org.jokerd.opensocial.api.model.Collection;
import org.jokerd.opensocial.api.model.DomainName;
import org.jokerd.opensocial.api.model.GroupId;
import org.ubimix.commons.events.calls.CallListener;

/**
 * @author kotelnikov
 */
public class ActivitystreamsTest extends ServiceCallTest {

    /**
     * @param name
     */
    public ActivitystreamsTest(String name) {
        super(name);
    }

    protected String getMessage(String firstName, String lastName) {
        return "Hello, " + firstName + " " + lastName + "!";
    }

    public void test() throws Exception {
        DomainName feedDomain = new DomainName("feed");
        String uri = "http://www.nytimes.com/services/xml/rss/nyt/HomePage.xml";
        // uri = "https://twitter.com/statuses/user_timeline/mkotelnikov.rss";
        GroupId feedId = new GroupId(feedDomain, uri);
        assertEquals(uri, feedId.getLocalIdDecoded());

        fServerEventManager.addListener(
            Activitystreams.Get.class,
            new CallListener<Activitystreams.Get>() {
                @Override
                protected void handleRequest(Get event) {
                    List<ActivityEntry> activities = new ArrayList<ActivityEntry>();
                    try {
                        GroupId groupId = event.getGroupId();
                        String feedUri = groupId.getLocalIdDecoded();
                        FeedActivityBuilder.readActivities(feedUri, activities);
                    } catch (Throwable t) {
                        event.onError(t);
                    } finally {
                        event.setActivities(activities);
                    }
                }
            });

        Activitystreams.Get get = new Activitystreams.Get();
        get.setGroupId(feedId);

        fClientEventManager.fireEvent(
            get,
            new CallListener<Activitystreams.Get>() {
                @Override
                protected void handleResponse(Get event) {
                    System.out.println(event);
                    Collection<ActivityEntry> activities = event
                        .getActivities();
                    List<ActivityEntry> list = activities.getEntries();
                    for (ActivityEntry activityEntry : list) {
                        System.out
                            .println("=============================================");
                        System.out.println(activityEntry.getPublished()
                            + "\t-\t"
                            + activityEntry.getTarget());
                    }
                }
            });
        Set<Throwable> errors = get.getErrors();
        assertNull(errors);
    }
}
