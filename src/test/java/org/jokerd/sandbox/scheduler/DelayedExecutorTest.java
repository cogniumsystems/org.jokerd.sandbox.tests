/**
 * 
 */
package org.jokerd.sandbox.scheduler;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

/**
 * @author kotelnikov
 */
public class DelayedExecutorTest extends TestCase {

    private Random fRandom = new Random(System.currentTimeMillis());

    /**
     * @param name
     */
    public DelayedExecutorTest(String name) {
        super(name);
    }

    public void test() throws Exception {
        class TestAction implements Runnable {
            private int fCancellationCounter;

            private int fExecutionCounter;

            private CountDownLatch fLatch = new CountDownLatch(1);

            private int fLimit;

            public TestAction(int limit) {
                fLimit = limit;
            }

            public void await() throws InterruptedException {
                fLatch.await(5, TimeUnit.SECONDS);
            }

            private void checkLimit() {
                if (fExecutionCounter + fCancellationCounter == fLimit) {
                    fLatch.countDown();
                }
            }

            public int getCancellationCounter() {
                return fCancellationCounter;
            }

            public int getExecutionCounter() {
                return fExecutionCounter;
            }

            public synchronized void incCancellationCounter() {
                fCancellationCounter++;
                checkLimit();
            }

            @Override
            public synchronized void run() {
                fExecutionCounter++;
                checkLimit();
            }
        }
        int executionDelay = 150;
        int maxSleepTime = 25;
        int calls = 100;
        TestAction action = new TestAction(calls);
        DelayedExecutor executor = new DelayedExecutor(executionDelay) {
            @Override
            protected void handleCancel(Runnable command) {
                ((TestAction) command).incCancellationCounter();
            }
        };
        for (int i = 0; i < calls; i++) {
            executor.execute(action);
            int timeout = 0;
            while (timeout == 0) {
                timeout = fRandom.nextInt(maxSleepTime);
            }
            Thread.sleep(timeout);
        }
        action.await();
        assertEquals(1, action.getExecutionCounter());
        assertEquals(calls - 1, action.getCancellationCounter());
    }
}
