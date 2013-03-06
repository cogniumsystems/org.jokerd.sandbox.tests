/**
 * 
 */
package org.jokerd.opensocial.sandbox;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jokerd.opensocial.api.model.ActivityEntry;
import org.jokerd.opensocial.cursors.IActivityCursor;
import org.jokerd.opensocial.cursors.StreamException;

/**
 * @author arkub
 */
public class TryCatchActivityCursor implements IActivityCursor {

    private final static Logger log = Logger
        .getLogger(TryCatchActivityCursor.class.getName());

    private IActivityCursor fActivityCursor;

    /**
     * 
     */
    public TryCatchActivityCursor(IActivityCursor activityCursor) {
        fActivityCursor = activityCursor;
    }

    /**
     * @see org.ubimix.commons.cursor.ICursor#close()
     */
    @Override
    public void close() throws StreamException {
        try {
            fActivityCursor.close();
        } catch (Throwable t) {
            handleError("Can not close a cursor", t);
        }
    }

    /**
     * @see org.ubimix.commons.cursor.ICursor#getCurrent()
     */
    @Override
    public ActivityEntry getCurrent() {
        ActivityEntry result = null;
        try {
            result = fActivityCursor.getCurrent();
        } catch (Throwable t) {
            handleError("Can not return the currently loaded item", t);
        }
        return result;
    }

    protected void handleError(String msg, Throwable t) {
        log.log(Level.WARNING, msg, t);
    }

    /**
     * @see org.ubimix.commons.cursor.ICursor#loadNext()
     */
    @Override
    public boolean loadNext() throws StreamException {
        boolean result = false;
        try {
            result = fActivityCursor.loadNext();
        } catch (Throwable t) {
            handleError("Can not load a next item", t);
        }
        return result;
    }

}
