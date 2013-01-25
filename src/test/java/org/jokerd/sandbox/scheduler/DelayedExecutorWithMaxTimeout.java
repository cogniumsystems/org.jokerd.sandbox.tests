package org.jokerd.sandbox.scheduler;

/**
 * This executor extends the {@link DelayedExecutor} class and adds launching of
 * the command if the command was not executed for a specific maximal period of
 * time. For example it can be useful to save frequently changing resources.
 * 
 * @author kotelnikov
 */
public class DelayedExecutorWithMaxTimeout extends DelayedExecutor {

    private long fLastExecutionTime;

    private long fMaxTimeout;

    public DelayedExecutorWithMaxTimeout(int timeout, long maxTimeout) {
        super(timeout);
        fMaxTimeout = maxTimeout;
    }

    public long getMaxTimeout() {
        return fMaxTimeout;
    }

    @Override
    protected synchronized void handleCancel(Runnable command) {
        synchronized (getMutex()) {
            if (now() - fLastExecutionTime > fMaxTimeout) {
                handleRun(command);
            }
        }
    }

    @Override
    protected synchronized void handleRun(Runnable command) {
        synchronized (getMutex()) {
            command.run();
            fLastExecutionTime = now();
        }
    }

    protected long now() {
        return System.currentTimeMillis();
    }

    public void setMaxTimeout(long maxTimeout) {
        fMaxTimeout = maxTimeout;
    }

}