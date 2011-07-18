/*******************************************************************************
 * Copyright (c) 2010, 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.core.internal.tasks;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.osgi.service.component.ComponentContext;

import org.eclipse.skalli.api.java.tasks.RunnableSchedule;
import org.eclipse.skalli.api.java.tasks.SchedulerService;
import org.eclipse.skalli.api.java.tasks.Task;
import org.eclipse.skalli.log.Log;

public class SchedulerServiceImpl implements SchedulerService {

    private static final Logger LOG = Log.getLogger(SchedulerServiceImpl.class);

    private static final TimeZone TIMEZONE = TimeZone.getTimeZone("UTC"); //$NON-NLS-1$

    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private Map<UUID, ScheduledFuture<?>> scheduledFutures = new HashMap<UUID, ScheduledFuture<?>>();

    /** Schedules currently registered with this service */
    private Map<UUID, RunnableSchedule> schedules = new HashMap<UUID, RunnableSchedule>();

    /** Activates this service and starts the runner for recurring tasks. */
    protected void activate(ComponentContext context) {
        // start a job for recurring tasks (triggered every minute)
        registerRecurringTaskRunner(1, TimeUnit.MINUTES);
        LOG.info("Scheduler Service activated"); //$NON-NLS-1$
    }

    /** Deactivates this service and cancels all scheduled tasks. */
    protected void deactivate(ComponentContext context) {
        for (ScheduledFuture<?> scheduledFuture : scheduledFutures.values()) {
            scheduledFuture.cancel(true);
        }
        scheduledFutures.clear();
        scheduler.shutdownNow();
        LOG.info("Scheduler Service deactivated"); //$NON-NLS-1$
    }

    @Override
    public synchronized UUID registerTask(Task task) {
        UUID taskId = UUID.randomUUID();
        Runnable runnable = task.getRunnable();
        long initialDelay = task.getInitialDelay();
        ScheduledFuture<?> scheduledFuture = task.isOneShot() ?
                scheduler.schedule(runnable, initialDelay, TimeUnit.MILLISECONDS) :
                scheduler.scheduleAtFixedRate(runnable, initialDelay, task.getPeriod(), TimeUnit.MILLISECONDS);
        scheduledFutures.put(taskId, scheduledFuture);
        LOG.info(MessageFormat.format("Task id=''{0}'' {1}: registered", taskId, task));
        return taskId;
    }

    @Override
    public synchronized void unregisterTask(UUID taskId) {
        ScheduledFuture<?> scheduledFuture = scheduledFutures.get(taskId);
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            scheduledFutures.remove(taskId);
            LOG.info(MessageFormat.format("Task id=''{0}'': unregistered", taskId));
        }
    }

    @Override
    public synchronized boolean isRegisteredTask(UUID taskId) {
        return scheduledFutures.get(taskId) != null;
    }

    @Override
    public synchronized boolean cancel(UUID taskId, boolean hard) {
        ScheduledFuture<?> scheduledFuture = scheduledFutures.get(taskId);
        if (scheduledFuture != null) {
            return scheduledFuture.cancel(hard);
        }
        return false;
    }

    @Override
    public synchronized boolean isDone(UUID taskId) {
        ScheduledFuture<?> scheduledFuture = scheduledFutures.get(taskId);
        return scheduledFuture != null ? scheduledFuture.isDone() : false;
    }

    @Override
    public synchronized UUID registerSchedule(RunnableSchedule schedule) {
        UUID scheduleId = UUID.randomUUID();
        schedules.put(scheduleId, schedule);
        LOG.info(MessageFormat.format("Schedule id=''{0}'' {1}: registered", scheduleId, schedule));
        return scheduleId;
    }

    @Override
    public synchronized RunnableSchedule unregisterSchedule(UUID scheduleId) {
        RunnableSchedule schedule = schedules.get(scheduleId);
        if (schedule != null) {
            schedules.remove(scheduleId);
        }
        LOG.info(MessageFormat.format("Schedule id=''{0}'': unregistered", scheduleId));
        return schedule;
    }

    @Override
    public synchronized boolean isRegisteredSchedule(UUID scheduleId) {
        return schedules.get(scheduleId) != null;
    }

    /**
     * Registers a runnable for recurring tasks.
     * @param period  the time between consecutive runs.
     * @param unit  the unit of the period.
     */
    void registerRecurringTaskRunner(long period, TimeUnit unit) {
        registerTask(new Task(new RecurringTaskRunner(), 0, unit.toMillis(period)));
    }

    /**
     * Runnable that is executed periodically asking all registered
     * schedules, if there are any due task. Starts due tasks as one-shot actions.
     */
    private class RecurringTaskRunner implements Runnable {
        @Override
        public void run() {
            Calendar now = new GregorianCalendar(TIMEZONE, Locale.ENGLISH);
            for (RunnableSchedule schedule : schedules.values()) {
                if (schedule.isDue(now)) {
                    Runnable runnable = schedule.getRunnable();
                    if (runnable != null) {
                        scheduler.schedule(runnable, 0, TimeUnit.MILLISECONDS);
                    }
                }
            }
        }
    }
}
