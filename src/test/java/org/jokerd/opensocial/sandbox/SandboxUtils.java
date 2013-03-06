/**
 * 
 */
package org.jokerd.opensocial.sandbox;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.sql.Date;
import java.util.Map;

import org.jokerd.opensocial.api.model.ActivityEntry;
import org.jokerd.opensocial.api.model.ActivityObject;
import org.jokerd.opensocial.cursors.ActivityRangeCursor;
import org.jokerd.opensocial.cursors.ActivitySectionsCursor;
import org.jokerd.opensocial.cursors.ActivitySectionsCursor.TimeGroupListener.Level;
import org.jokerd.opensocial.cursors.IActivityCursor;
import org.jokerd.opensocial.cursors.StreamException;
import org.jokerd.opensocial.oauth.FileBasedOAuthInfoStore;
import org.jokerd.opensocial.oauth.OAuthHelper;
import org.jokerd.opensocial.scheduler.ActivitySerializerStream;

import org.ubimix.commons.json.ext.DateFormatter;
import org.ubimix.commons.json.ext.FormattedDate;

/**
 * @author kotelnikov
 */
public class SandboxUtils {

    public static void printActivities(File outFile, IActivityCursor cursor)
        throws IOException,
        StreamException {
        int maxNumber = 1000;
        long time = System.currentTimeMillis() - DateFormatter.getDays(3);
        FormattedDate earlestDate = DateFormatter.formatDate(new Date(time));

        cursor = new ActivityRangeCursor(cursor, maxNumber, earlestDate);
        cursor = new ActivitySectionsCursor(
            cursor,
            new ActivitySectionsCursor.TimeGroupListener(Level.DAY) {
                @Override
                public void beginGroup(ActivityEntry entry) {
                    FormattedDate date = getDate(entry);
                    System.out.println("============="
                        + date.getDay()
                        + "/"
                        + date.getMonth()
                        + "/"
                        + date.getYear()
                        + "=============");
                }
            });
        cursor = new ActivitySectionsCursor(
            cursor,
            new ActivitySectionsCursor.TimeGroupListener(Level.HOUR) {
                @Override
                public void beginGroup(ActivityEntry entry) {
                    FormattedDate date = getDate(entry);
                    System.out.println("-------------"
                        + (date.getHour() + 1)
                        + "h00"
                        + "-------------");
                }
            });
        cursor = new ActivitySectionsCursor(
            cursor,
            new ActivitySectionsCursor.TimeGroupListener(Level.HOUR) {

                @Override
                public void beginGroup(ActivityEntry entry) {
                    String target = getTarget(entry);
                    System.out.println("* [" + target + "]");
                }

                private String getTarget(ActivityEntry activityEntry) {
                    ActivityObject target = activityEntry.getTarget();
                    String str = target != null ? target.getDisplayName() : "";
                    return str;
                }

                @Override
                public boolean sameGroup(
                    ActivityEntry prev,
                    ActivityEntry current) {
                    if (!super.sameGroup(prev, current)) {
                        return false;
                    }
                    String first = getTarget(prev);
                    String second = getTarget(current);
                    return first.equals(second);
                }

            });
        try {
            FileWriter writer = new FileWriter(outFile);
            writer.write("[\n");
            int counter = 0;
            ActivitySerializerStream serializer = new ActivitySerializerStream(
                cursor);
            while (cursor.loadNext()) {
                ActivityEntry activityEntry = cursor.getCurrent();
                if (counter > 0) {
                    writer.write("\n,\n");
                }
                counter++;
                writer.write(activityEntry.toString());
                String shift = " ";
                String message = activityEntry.getTitle();
                {
                    FormattedDate date = ActivitySectionsCursor.TimeGroupListener
                        .getDate(activityEntry);
                    String by = activityEntry.getActor().getDisplayName();
                    System.out.print(shift
                        + "["
                        + date.getHour()
                        + "h"
                        + date.getMinutes()
                        + "] ");
                    if (message != null) {
                        System.out.println(message);
                    } else {
                        System.out.println();
                    }
                    String content = activityEntry.getContent();
                    if (content != null) {
                        System.out.println(shift + content);
                    }
                    System.out.println(shift + "Published by '" + by + "'.");
                }
                shift += " | ";
                {
                    ActivityObject object = activityEntry.getObject();
                    String title = object.getDisplayName();
                    if (title != null
                        && message != null
                        && !message.equals(title)) {
                        System.out.println(shift + "Title: " + title);
                    }
                    String url = object.getUrl();
                    if (url != null) {
                        System.out.println(shift + "URL: " + url);
                    }
                    String content = object.getContent();
                    if (content != null) {
                        System.out.println(shift + content);
                    }
                }
                System.out.println();

                // System.out.println(activityEntry.getPublished()
                // + "\t-\t"
                // + activityEntry.getTarget().getDisplayName());
            }
            writer.write("]\n");
            writer.flush();
            writer.close();
        } finally {
            cursor.close();
        }
    }

    private final String fBaseDir;

    private String fNetworkName;

    public SandboxUtils(String networkName) throws IOException {
        // TODO: fixme
        this(
           "../../org.jokerd/workdir",
            networkName);
    }

    public SandboxUtils(String baseDir, String networkName, String... args)
        throws IOException {
        fBaseDir = baseDir;
        fNetworkName = networkName;
    }

    public String getBaseDir() {
        return fBaseDir;
    }

    // 0. Check if has access key
    // 1. Get authorization request
    // 2. Set authorization response
    // 3. Get access keys
    // 4. Call for a service with the restricted access

    public String getNetworkName() {
        return fNetworkName;
    }

    protected IActivityCursor newCursor(
        Class<? extends IActivityCursor> cursorType,
        String... args) throws Exception {
        OAuthHelper oauthHelper = newOAuthHelper(args);
        Constructor<? extends IActivityCursor> constructor = cursorType
            .getConstructor(OAuthHelper.class);
        IActivityCursor cursor = constructor.newInstance(oauthHelper);
        return cursor;
    }

    protected OAuthHelper newOAuthHelper(String... args)
        throws UnsupportedEncodingException,
        IOException {
        String networkName = getNetworkName();
        Map<String, String> map = OAuthHelper.getParams(args);
        OAuthHelper oauthHelper = FileBasedOAuthInfoStore.getOAuthHelper(
            fBaseDir + "/access/" + networkName + "-oauth.json",
            fBaseDir + "/access/" + networkName + "-oauth.json-template",
            map);
        return oauthHelper;
    }

    protected void setNetworkName(String networkName) {
        fNetworkName = networkName;
    }

    public void showActivityStream(
        Class<? extends IActivityCursor> cursorType,
        String... args) throws Exception {
        IActivityCursor cursor = newCursor(cursorType);
        showActivityStream(cursor);
    }

    protected void showActivityStream(final IActivityCursor cursor)
        throws StreamException,
        IOException {
        try {
            String outputFileName = fBaseDir
                + "/output/"
                + getNetworkName()
                + ".json";
            File outFile = new File(outputFileName);
            outFile.getParentFile().mkdirs();
            printActivities(outFile, cursor);
        } finally {
            cursor.close();
        }
    }

}
