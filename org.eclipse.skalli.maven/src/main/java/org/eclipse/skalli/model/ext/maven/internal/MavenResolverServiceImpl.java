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
package org.eclipse.skalli.model.ext.maven.internal;

import java.text.MessageFormat;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.osgi.service.component.ComponentContext;

import org.eclipse.skalli.api.java.EventListener;
import org.eclipse.skalli.api.java.EventService;
import org.eclipse.skalli.api.java.events.EventCustomizingUpdate;
import org.eclipse.skalli.api.java.tasks.RunnableSchedule;
import org.eclipse.skalli.api.java.tasks.SchedulerService;
import org.eclipse.skalli.common.configuration.ConfigurationService;
import org.eclipse.skalli.log.Log;
import org.eclipse.skalli.model.ext.maven.internal.config.MavenResolverConfig;
import org.eclipse.skalli.model.ext.maven.internal.config.MavenResolverResource;

public class MavenResolverServiceImpl implements MavenResolverService, EventListener<EventCustomizingUpdate> {

    private static final Logger LOG = Log.getLogger(MavenResolverServiceImpl.class);

    private SchedulerService schedulerService;
    private ConfigurationService configService;

    private UUID scheduleId;

    /** Activates this service and starts validation jobs. */
    protected void activate(ComponentContext context) {
        LOG.info("Maven Resolver Service activated"); //$NON-NLS-1$
    }

    /** Deactivates this service and stops validation jobs. */
    protected void deactivate(ComponentContext context) {
        stopAllTasks();
        LOG.info("Maven Resolver Service deactivated"); //$NON-NLS-1$
    }

    protected void bindSchedulerService(SchedulerService schedulerService) {
        LOG.info(MessageFormat.format("bindSchedulerService({0})", schedulerService)); //$NON-NLS-1$LOG.info(MessageFormat.format("bindSchedulerService({0})", schedulerService)); //$NON-NLS-1$
        this.schedulerService = schedulerService;
        synchronizeAllTasks();
    }

    protected void unbindSchedulerService(SchedulerService schedulerService) {
        LOG.info(MessageFormat.format("unbindSchedulerService({0})", schedulerService)); //$NON-NLS-1$
        scheduleId = null;
        this.schedulerService = null;
    }

    protected void bindConfigurationService(ConfigurationService configService) {
        LOG.info(MessageFormat.format("bindConfigurationService({0})", configService)); //$NON-NLS-1$
        this.configService = configService;
        synchronizeAllTasks();
    }

    protected void unbindConfigurationService(ConfigurationService configService) {
        LOG.info(MessageFormat.format("unbindConfigurationService({0})", configService)); //$NON-NLS-1$
        this.configService = null;
        synchronizeAllTasks();
    }

    protected void bindEventService(EventService eventService) {
        LOG.info(MessageFormat.format("bindEventService({0})", eventService)); //$NON-NLS-1$
        eventService.registerListener(EventCustomizingUpdate.class, this);
    }

    protected void unbindEventService(EventService eventService) {
        LOG.info(MessageFormat.format("unbindEventService({0})", eventService)); //$NON-NLS-1$
    }

    synchronized void startAllTasks() {
        if (schedulerService != null) {
            if (scheduleId != null) {
                stopAllTasks();
            }
            if (configService != null) {
                final MavenResolverConfig resolverConfig = configService.readCustomization(
                        MavenResolverResource.MAPPINGS_KEY, MavenResolverConfig.class);
                if (resolverConfig != null) {
                    RunnableSchedule runnableSchedule = new RunnableSchedule(resolverConfig.getSchedule()) {
                        @Override
                        public Runnable getRunnable() {
                            String userId = resolverConfig.getUserId();
                            if (StringUtils.isBlank(userId)) {
                                userId = MavenResolverService.class.getName();
                            }
                            return new MavenResolverRunnable(configService, userId);
                        }
                    };
                    scheduleId = schedulerService.registerSchedule(runnableSchedule);
                }
            }
        }
    }

    synchronized void stopAllTasks() {
        if (schedulerService != null && scheduleId != null) {
            schedulerService.unregisterSchedule(scheduleId);
            scheduleId = null;
        }
    }

    void synchronizeAllTasks() {
        stopAllTasks();
        startAllTasks();
    }

    @Override
    public void onEvent(EventCustomizingUpdate event) {
        if (MavenResolverResource.MAPPINGS_KEY.equals(event.getCustomizationName())) {
            synchronizeAllTasks();
        }
    }
}
