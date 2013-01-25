package org.jokerd.sandbox.scheduler;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author kotelnikov
 */
public class Scheduler {

    public interface IDateProvider {
        Date loadNextDate();
    }

    public interface IRegistration {
        void cancel();
    }

    private class Registration implements IRegistration {

        private IDateProvider fDateProvider;

        private Runnable fTask;

        private TimerTask fTimerTask;

        public Registration(Timer timer, Runnable task, IDateProvider provider) {
            fTimer = timer;
            fTask = task;
            fDateProvider = provider;
        }

        @Override
        public synchronized void cancel() {
            if (fTimerTask != null) {
                fTimerTask.cancel();
                fTimerTask = null;
            }
        }

        public synchronized boolean scheduleNext() {
            Date time = fDateProvider.loadNextDate();
            if (time == null) {
                return false;
            }
            fTimerTask = new TimerTask() {
                @Override
                public void run() {
                    fTask.run();
                    scheduleNext();
                }
            };
            fTimer.schedule(fTimerTask, time);
            return true;
        }

    }

    private Timer fTimer = new Timer();

    public void cancel() {
        fTimer.cancel();
    }

    public IRegistration schedule(
        final Runnable task,
        final IDateProvider provider) {
        Registration registration = new Registration(fTimer, task, provider);
        registration.scheduleNext();
        return registration;
    }

}
