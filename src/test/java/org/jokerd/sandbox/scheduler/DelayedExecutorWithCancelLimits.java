package org.jokerd.sandbox.scheduler;

/**
 * This executor extends the {@link DelayedExecutor} class and adds launching of
 * command after the specified number of cancellations. It allows to execute an
 * operation after a specific period of time or after a specific number of
 * calls. For example it can be useful to save changing resources. If the
 * resource is changed rarely it will be saved after the specified period of
 * time. But if it is changed very frequently then the save operation will be
 * launched after the specified number of "cancellations".
 * 
 * @author kotelnikov
 */
public class DelayedExecutorWithCancelLimits extends DelayedExecutor {

    private int fCancelCounter;

    private int fCancelLimits;

    public DelayedExecutorWithCancelLimits(int timeout, int cancelLimits) {
        super(timeout);
        setCancelLimits(cancelLimits);
    }

    public int getCancelLimits() {
        return fCancelLimits;
    }

    @Override
    protected synchronized void handleCancel(Runnable command) {
        synchronized (getMutex()) {
            fCancelCounter++;
            if (fCancelCounter >= fCancelLimits) {
                handleRun(command);
            }
        }
    }

    @Override
    protected synchronized void handleRun(Runnable command) {
        synchronized (getMutex()) {
            command.run();
            fCancelCounter = 0;
        }
    }

    public void setCancelLimits(int cancelLimits) {
        synchronized (getMutex()) {
            fCancelLimits = cancelLimits;
        }
    }

}