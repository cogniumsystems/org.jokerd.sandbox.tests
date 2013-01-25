/**
 * 
 */
package org.jokerd.sandbox.scheduler;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

/**
 * @author kotelnikov
 */
public class DelayedExecutor implements Executor {

    private int fDelay;

    private Object fMutex = new Object();

    private TimerTask fTask;

    private Timer fTimer = new Timer();

    /**
     * 
     */
    public DelayedExecutor(int delay) {
        fDelay = delay;
    }

    @Override
    public void execute(final Runnable command) {
        synchronized (getMutex()) {
            if (fTask != null) {
                fTask.cancel();
                fTask = null;
            }
            fTask = new TimerTask() {

                @Override
                public boolean cancel() {
                    boolean result = super.cancel();
                    if (result) {
                        handleCancel(command);
                    }
                    return result;
                }

                @Override
                public void run() {
                    handleRun(command);
                    synchronized (getMutex()) {
                        if (fTask == this) {
                            fTask = null;
                        }
                    }
                }

            };
            fTimer.schedule(fTask, fDelay);
        }
    }

    public int getDelay() {
        return fDelay;
    }

    protected Object getMutex() {
        return fMutex;
    }

    protected void handleCancel(Runnable command) {
    }

    protected void handleRun(Runnable command) {
        command.run();
    }

    public void setDelay(int delay) {
        fDelay = delay;
    }

}
