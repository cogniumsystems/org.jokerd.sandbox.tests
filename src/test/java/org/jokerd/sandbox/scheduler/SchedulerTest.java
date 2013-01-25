/**
 * 
 */
package org.jokerd.sandbox.scheduler;

import java.util.Date;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.jokerd.sandbox.scheduler.Scheduler.IDateProvider;

/**
 * @author kotelnikov
 */
public class SchedulerTest extends TestCase {

    /**
     * @param name
     */
    public SchedulerTest(String name) {
        super(name);
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void test() throws Exception {
        final int count = 20;
        final int timeout = 50;
        final CyclicBarrier barrier = new CyclicBarrier(2);
        Scheduler scheduler = new Scheduler();
        final int[] counter = { 0 };
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                counter[0]++;
                System.out.println(counter[0]);
                if (counter[0] == count) {
                    try {
                        barrier.await();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new IDateProvider() {
            long time = System.currentTimeMillis();

            @Override
            public Date loadNextDate() {
                if (counter[0] == count) {
                    return null;
                }
                time = timeout;
                return new Date(time);
            }
        });
        barrier.await(10, TimeUnit.SECONDS);
        System.out.println("Finished.");
    }

}
