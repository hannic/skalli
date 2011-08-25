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
package org.eclipse.skalli.core.internal.validation;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.api.java.EntityService;
import org.eclipse.skalli.api.java.EventListener;
import org.eclipse.skalli.api.java.EventService;
import org.eclipse.skalli.api.java.IssuesService;
import org.eclipse.skalli.api.java.Validation;
import org.eclipse.skalli.api.java.ValidationService;
import org.eclipse.skalli.api.java.events.EventCustomizingUpdate;
import org.eclipse.skalli.api.java.tasks.RunnableSchedule;
import org.eclipse.skalli.api.java.tasks.Schedule;
import org.eclipse.skalli.api.java.tasks.SchedulerService;
import org.eclipse.skalli.api.java.tasks.Task;
import org.eclipse.skalli.api.rest.monitor.Monitorable;
import org.eclipse.skalli.common.configuration.ConfigurationService;
import org.eclipse.skalli.common.util.CollectionUtils;
import org.eclipse.skalli.log.Log;
import org.eclipse.skalli.model.ext.EntityBase;
import org.eclipse.skalli.model.ext.Issue;
import org.eclipse.skalli.model.ext.Issues;
import org.eclipse.skalli.model.ext.Severity;
import org.eclipse.skalli.model.ext.ValidationException;
import org.osgi.service.component.ComponentContext;
import org.restlet.resource.ServerResource;


public class ValidationServiceImpl implements ValidationService, EventListener<EventCustomizingUpdate>, Monitorable {

    private static final Logger LOG = Log.getLogger(ValidationServiceImpl.class);

    private static final String DEFAULT_USER = ValidationService.class.getName();
    private static final Severity DEFAULT_SEVERITY = Severity.INFO;

    private static final String DEFAULT_NIGHTLY_VALIDATION_DAY = "*"; //$NON-NLS-1$
    private static final String DEFAULT_NIGHTLY_VALIDATION_HOUR = "2"; //$NON-NLS-1$
    private static final String DEFAULT_NIGHLY_VALIDATION_MINUTE = "0"; //$NON-NLS-1$

    private static final long DEFAULT_QUEUED_INITIAL_DELAY = TimeUnit.SECONDS.toMillis(10);
    private static final long DEFAULT_QUEUED_PERIOD = TimeUnit.SECONDS.toMillis(10);
    private static final int DEFAULT_THRESHOLD = 0;

    /** All currently known implementations of EntityService, managed by bindEntityService/unbindEntityService */
    private final Map<String, EntityService<?>> entityServices = new HashMap<String, EntityService<?>>();

    private IssuesService issuesService;
    private SchedulerService schedulerService;
    private ConfigurationService configService;

    /** The unique identifiers of the schedules registered with the scheduler service */
    private final Set<UUID> registeredSchedules = new HashSet<UUID>();

    /** The unique identifier of the {@link QueueValidator} task */
    private UUID taskIdQueueValidator;

    /** Entities queued for re-validation subsequent to a persist */
    private final LinkedBlockingQueue<QueuedEntity<? extends EntityBase>> queuedEntities =
            new LinkedBlockingQueue<QueuedEntity<? extends EntityBase>>();

    /** Activates this service and starts validation jobs. */
    protected void activate(ComponentContext context) {
        LOG.info("Validation Service activated"); //$NON-NLS-1$
    }

    /** Deactivates this service and stops validation jobs. */
    protected void deactivate(ComponentContext context) {
        stopAllTasks();
        LOG.info("Validation Service deactivated"); //$NON-NLS-1$
    }

    @SuppressWarnings("rawtypes")
    protected void bindEntityService(EntityService entityService) {
        synchronized (entityServices) {
            String entityServiceName = entityService.getEntityClass().getName();
            entityServices.put(entityServiceName, entityService);
            LOG.info(MessageFormat.format("bindEntityService({0})", entityServiceName)); //$NON-NLS-1$
        }
    }

    @SuppressWarnings("rawtypes")
    protected void unbindEntityService(EntityService entityService) {
        synchronized (entityServices) {
            String entityServiceName = entityService.getEntityClass().getName();
            entityServices.remove(entityServiceName);
            LOG.info(MessageFormat.format("unbindEntityService({0})", entityServiceName)); //$NON-NLS-1$
        }
    }

    protected void bindIssuesService(IssuesService issuesService) {
        LOG.info(MessageFormat.format("bindIssuesService({0})", issuesService)); //$NON-NLS-1$
        this.issuesService = issuesService;
    }

    protected void unbindIssuesService(IssuesService issuesService) {
        LOG.info(MessageFormat.format("unbindIssuesService({0})", issuesService)); //$NON-NLS-1$
        this.issuesService = null;
    }

    protected void bindSchedulerService(SchedulerService schedulerService) {
        LOG.info(MessageFormat.format("bindSchedulerService({0})", schedulerService)); //$NON-NLS-1$
        this.schedulerService = schedulerService;
        synchronizeAllTasks();
    }

    protected void unbindSchedulerService(SchedulerService schedulerService) {
        LOG.info(MessageFormat.format("unbindSchedulerService({0})", schedulerService)); //$NON-NLS-1$
        registeredSchedules.clear();
        taskIdQueueValidator = null;
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

    @Override
    public synchronized <T extends EntityBase> void queue(Validation<T> validation) {
        Map<Validation<T>, Validation<T>> validations = CollectionUtils.asMap(validation, validation);
        queueAll(validations);
    }

    @Override
    public synchronized <T extends EntityBase> void queueAll(Class<T> entityClass, Severity minSeverity, String userId) {
        EntityService<T> entityService = getEntityService(entityClass);
        if (entityService != null) {
            Map<Validation<T>, Validation<T>> validations = new HashMap<Validation<T>, Validation<T>>();
            List<T> enitites = entityService.getAll();
            for (T entity : enitites) {
                Validation<T> validation = new Validation<T>(entityClass, entity.getUuid(), minSeverity, userId);
                validations.put(validation, validation);
            }
            queueAll(validations);
        }
    }

    private <T extends EntityBase> void queueAll(Map<Validation<T>, Validation<T>> newEntries) {
        // first, update those entries that already are scheduled...
        Iterator<QueuedEntity<?>> oldEntries = queuedEntities.iterator();
        while (oldEntries.hasNext()) {
            Validation<?> oldEntry = oldEntries.next();
            Validation<T> newEntry = newEntries.get(oldEntry);
            if (newEntry != null) {
                // relaxing severity is ok (e.g. from FATAL to WARNING), but not vice versa;
                // otherwise we would not get issues that the previous caller has requested
                if (oldEntry.getMinSeverity().compareTo(newEntry.getMinSeverity()) < 0) {
                    oldEntry.setMinSeverity(newEntry.getMinSeverity());
                }
                oldEntry.setUserId(newEntry.getUserId());
                newEntries.remove(newEntry);
                LOG.info(MessageFormat.format("{0}: updated in queue", oldEntry));
            }
        }
        // ...then schedule the remaining...
        for (Validation<T> newEntry : newEntries.keySet()) {
            if (!offerQueueEntry(newEntry)) {
                // should not happen since we use a queue without bounds, but in case...
                LOG.warning(MessageFormat.format("Failed to schedule entity {0} for validation", newEntry.getEntityId()));
            }
            LOG.info(MessageFormat.format("{0}: queued", newEntry));
        }
        // ...and mark existing issues of the entity as stale
        markIssuesAsStale(newEntries);
    }

    @Override
    public synchronized <T extends EntityBase> boolean isQueued(T entity) {
        Class<?> entityClass = entity.getClass();
        UUID entityId = entity.getUuid();
        for (Validation<?> queuedEntity : queuedEntities) {
            if (entityClass.equals(queuedEntity.getEntityClass())
                    && entityId.equals(queuedEntity.getEntityId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public <T extends EntityBase> void validate(Class<T> entityClass, UUID entityId, Severity minSeverity, String userId) {
        EntityService<T> entityService = getEntityService(entityClass);
        if (entityService != null) {
            T entity = entityService.getByUUID(entityId);
            validateAndPersist(entityService, entity, minSeverity, userId);
        }
    }

    @Override
    public <T extends EntityBase> void validateAll(Class<T> entityClass, Severity minSeverity, String userId) {
        EntityService<T> entityService = getEntityService(entityClass);
        if (entityService != null) {
            List<T> entities = entityService.getAll();
            for (T entity : entities) {
                validateAndPersist(entityService, entity, minSeverity, userId);
            }
        }
    }

    private <T extends EntityBase> void validateAndPersist(Validation<T> entry, Severity defaultSeverity) {
        EntityService<T> entityService = getEntityService(entry.getEntityClass());
        if (entityService != null) {
            T entity = entityService.getByUUID(entry.getEntityId());
            if (entity != null) {
                Severity minSeverity = entry.getMinSeverity();
                if (minSeverity == null) {
                    minSeverity = defaultSeverity;
                }
                validateAndPersist(entityService, entity, minSeverity, entry.getUserId());
            }
        }
    }

    private <T extends EntityBase> void validateAndPersist(EntityService<T> entityService,
            T entity, Severity minSeverity, String userId) {
        SortedSet<Issue> issues = null;
        try {
            issues = entityService.validate(entity, minSeverity);
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, MessageFormat.format("Validation of entity {0} failed:\n{1}",
                    entity.getUuid(), e.getMessage()), e);
            return;
        }
        if (issuesService != null) {
            try {
                issuesService.persist(entity.getUuid(), issues, userId);
                SortedSet<Issue> fatalIssues = Issues.getIssues(issues, Severity.FATAL);
                if (fatalIssues.size() > 0) {
                    LOG.warning(Issues.getMessage(
                            MessageFormat.format("Entity {0} has {1} FATAL issues", entity.getUuid(), fatalIssues.size()),
                            fatalIssues));
                } else if (LOG.isLoggable(Level.FINE)) {
                    LOG.info(Issues.getMessage(
                            MessageFormat.format("Entity {0}: validated ({1} issues found)", entity.getUuid(), issues.size()),
                            issues));
                }
            } catch (ValidationException e) { // should not happen, but in case...
                LOG.log(Level.SEVERE, MessageFormat.format("Failed to persist issues for entity {0}:\n{1}",
                        entity.getUuid(), e.getMessage()), e);
            }
        }
    }

    /**
     * Sets the "stale" flag on previously persisted issues reported for the entities specified
     * in the given validation entries.
     */
    private <T extends EntityBase> void markIssuesAsStale(Map<Validation<T>, Validation<T>> validations) {
        if (issuesService != null) {
            for (Validation<T> validation : validations.keySet()) {
                UUID entityId = validation.getEntityId();
                Issues issues = issuesService.getByUUID(entityId);
                if (issues == null) {
                    issues = new Issues(entityId);
                }
                issues.setStale(true);
                try {
                    issuesService.persist(issues, validation.getUserId());
                } catch (ValidationException e) { // should not happen, but in case...
                    LOG.warning(MessageFormat.format("Failed to persist validation issues for entity {0}:\n{1}",
                            entityId, e.getMessage()));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends EntityBase> EntityService<T> getEntityService(Class<T> entityClass) {
        return (EntityService<T>) entityServices.get(entityClass.getName());
    }

    // package protected for monitoring and testing purposes
    Queue<QueuedEntity<? extends EntityBase>> getQueuedEntities() {
        return queuedEntities;
    }

    // package protected for testing purposes
    QueuedEntity<? extends EntityBase> pollNextQueueEntry() {
        return queuedEntities.poll();
    }

    // package protected for testing purposes
    <T extends EntityBase> boolean offerQueueEntry(Validation<T> newEntry) {
        return queuedEntities.offer(new QueuedEntity<T>(newEntry));
    }

    // package protected for testing purposes
    UUID getTaskIdQueueValidator() {
        return taskIdQueueValidator;
    }

    // package protected for testing purposes
    Set<UUID> getRegisteredSchedules() {
        return registeredSchedules;
    }


    /**
     * Runnable that validates entities queued for re-validation and persists the results.
     */
    final class QueueValidator implements Runnable {

        private Severity defaultSeverity;
        private int threshold;

        /**
         * Creates a validation runnable suitable for periodic re-validation
         * of entities that have been modified.
         *
         * @param minSeverity  default minimal severity of issues to report. This severity is
         * applied if no explicit severity has been specified when the entity was
         * {@link EntityService#scheduleForValidation(UUID, Severity) scheduled for re-validation}.
         * @param threshold  threshold of entries that triggers a "bunch validation",
         * when exceeded, see {@link #run()}.
         */
        public QueueValidator(Severity defaultSeverity, int threshold) {
            this.defaultSeverity = defaultSeverity;
            this.threshold = threshold;
        }

        /**
         * Validates the next entity scheduled for re-validation.
         * Continues to validate entities from the queue, until the size of the
         * queue drops below the defined threshold ("bunch validation").
         */
        @Override
        public void run() {
            do {
                QueuedEntity<? extends EntityBase> entry = pollNextQueueEntry();
                if (entry == null) {
                    break;
                }
                entry.setStartedAt(System.currentTimeMillis());
                validateAndPersist(entry, defaultSeverity);
                LOG.info(MessageFormat.format("{0}: done", entry));
            } while (queuedEntities.size() >= threshold);
        }
    }

    /**
     * Runnable that queues all entities of a given type for validation.
     */
    final class QueueRunnable implements Runnable {

        private Severity minSeverity;
        private String entityClassName;
        private String userId;

        /**
         * Creates a validation runnable suitable for periodic re-validation
         * of all known entites.
         *
         * @param minSeverity  minimal severity of issues to report.
         */
        public QueueRunnable(Severity minSeverity, String entityClassName, String userId) {
            this.minSeverity = minSeverity;
            this.entityClassName = entityClassName;
            this.userId = userId;
        }

        @Override
        public void run() {
            for (EntityService<?> entityService : entityServices.values()) {
                Class<?> entityClass = entityService.getEntityClass();
                if (entityClass.getName().equals(entityClassName)
                        || entityClass.getSimpleName().equals(entityClassName)) {
                    queueAll(entityService.getEntityClass(), minSeverity, userId);
                    break;
                }
            }
        }
    };

    /**
     * Runnable that queues all known entities for validation.
     */
    final class QueueAllRunnable implements Runnable {

        private Severity minSeverity;
        private String userId;

        /**
         * Creates a validation runnable suitable for periodic re-validation
         * of all known entites.
         *
         * @param minSeverity  minimal severity of issues to report.
         */
        public QueueAllRunnable(Severity minSeverity, String userId) {
            this.minSeverity = minSeverity;
            this.userId = userId;
        }

        @Override
        public void run() {
            for (EntityService<?> entityService : entityServices.values()) {
                queueAll(entityService.getEntityClass(), minSeverity, userId);
            }
        }
    };

    /**
     * Runnable that validates all known entities of a given entity type
     * and persists the results.
     */
    final class ValidateRunnable implements Runnable {

        private Severity minSeverity;
        private String entityClassName;
        private String userId;

        /**
         * Creates a validation runnable suitable for periodic re-validation
         * of all known entites.
         *
         * @param minSeverity  minimal severity of issues to report.
         */
        public ValidateRunnable(Severity minSeverity, String entityClassName, String userId) {
            this.minSeverity = minSeverity;
            this.entityClassName = entityClassName;
            this.userId = userId;
        }

        @Override
        public void run() {
            for (EntityService<?> entityService : entityServices.values()) {
                Class<?> entityClass = entityService.getEntityClass();
                if (entityClass.getName().equals(entityClassName)
                        || entityClass.getSimpleName().equals(entityClassName)) {
                    validateAll(entityService.getEntityClass(), minSeverity, userId);
                    break;
                }
            }
        }
    };

    /**
     * Runnable that validates all known entities and persists the results.
     */
    final class ValidateAllRunnable implements Runnable {

        private Severity minSeverity;
        private String userId;

        /**
         * Creates a validation runnable suitable for periodic re-validation
         * of all known entites.
         *
         * @param minSeverity  minimal severity of issues to report.
         */
        public ValidateAllRunnable(Severity minSeverity, String userId) {
            this.minSeverity = minSeverity;
            this.userId = userId;
        }

        @Override
        public void run() {
            for (EntityService<?> entityService : entityServices.values()) {
                validateAll(entityService.getEntityClass(), minSeverity, userId);
            }
        }
    };

    /**
     * <code>RunnableSchedule</code> wrapper for a validation configuration.
     */
    final class ValidationSchedule extends RunnableSchedule {

        private ValidationConfig config;

        public ValidationSchedule(ValidationConfig config) {
            super(config.getSchedule());
            this.config = config;
        }

        @Override
        public Runnable getRunnable() {
            return getRunnableFromConfig(config);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(super.toString());
            sb.append(" config='");
            sb.append(config.toString());
            sb.append("'");
            return sb.toString();
        }
    }

    Runnable getRunnableFromConfig(ValidationConfig config) {
        ValidationAction action = config.getAction();
        Severity minSeverity = config.getMinSeverity();
        String userId = config.getUserId();
        if (StringUtils.isBlank(userId)) {
            userId = DEFAULT_USER;
        }
        String entityType = config.getEntityType();
        if (StringUtils.isBlank(entityType)) {
            switch (action) {
            case QUEUE:
            case VALIDATE:
                LOG.warning(MessageFormat.format(
                        "Ignoring invalid schedule entry ''{0}'': entity type required for action {1}",
                        toString(), action));
                return null;
            }
        }
        switch (action) {
        case QUEUED:
            return new QueueValidator(minSeverity, config.getThreshold());
        case QUEUE:
            return new QueueRunnable(minSeverity, entityType, userId);
        case QUEUE_ALL:
            return new QueueAllRunnable(minSeverity, userId);
        case VALIDATE:
            return new ValidateRunnable(minSeverity, entityType, userId);
        case VALIDATE_ALL:
            return new ValidateAllRunnable(minSeverity, userId);
        }
        return null;
    }

    synchronized void startAllTasks() {
        if (schedulerService != null) {
            if (taskIdQueueValidator != null || registeredSchedules.size() > 0) {
                stopAllTasks();
            }
            boolean startDefaultQueueTask = true;
            boolean startDefaultNightlyTask = true;
            if (configService != null) {
                ValidationsConfig validationConfigs = configService.readCustomization(ValidationsResource.MAPPINGS_KEY,
                        ValidationsConfig.class);
                if (validationConfigs != null) {
                    for (ValidationConfig validationConfig : validationConfigs.getValidationConfigs()) {
                        ValidationAction action = validationConfig.getAction();
                        if (ValidationAction.QUEUED.equals(action)) {
                            startDefaultQueueTask = false;
                        } else if (ValidationAction.QUEUE_ALL.equals(action)) {
                            startDefaultNightlyTask = false;
                        }
                        ValidationSchedule schedule = new ValidationSchedule(validationConfig);
                        UUID scheduleId = schedulerService.registerSchedule(schedule);
                        registeredSchedules.add(scheduleId);
                        LOG.info(MessageFormat.format("Custom schedule {0}: registered", schedule)); //$NON-NLS-1$
                    }
                }
            }
            startDefaultQueueTask(startDefaultQueueTask);
            startDefaultNightlyTask(startDefaultNightlyTask);
        }
    }

    // register default task for the queue validation
    private void startDefaultQueueTask(boolean start) {
        if (start) {
            Task task = new Task(
                    new QueueValidator(DEFAULT_SEVERITY, DEFAULT_THRESHOLD),
                    DEFAULT_QUEUED_INITIAL_DELAY, DEFAULT_QUEUED_PERIOD);
            taskIdQueueValidator = schedulerService.registerTask(task);
            LOG.info(MessageFormat.format("Default task {0}: registered (id={1})", task, taskIdQueueValidator)); //$NON-NLS-1$
        }
    }

    // register default "nightly queue all" schedule
    private void startDefaultNightlyTask(boolean start) {
        if (start) {
            Schedule schedule = new Schedule(DEFAULT_NIGHTLY_VALIDATION_DAY,
                    DEFAULT_NIGHTLY_VALIDATION_HOUR, DEFAULT_NIGHLY_VALIDATION_MINUTE);
            RunnableSchedule runnableSchedule = new RunnableSchedule(schedule) {
                @Override
                public Runnable getRunnable() {
                    return new QueueAllRunnable(DEFAULT_SEVERITY, DEFAULT_USER);
                }
            };
            UUID scheduleId = schedulerService.registerSchedule(runnableSchedule);
            registeredSchedules.add(scheduleId);
            LOG.info(MessageFormat.format("Default schedule {0}: registered (id={1})", runnableSchedule, scheduleId)); //$NON-NLS-1$
        }
    }

    synchronized void stopAllTasks() {
        if (schedulerService != null) {
            for (UUID key : registeredSchedules) {
                schedulerService.unregisterSchedule(key);
            }
            if (taskIdQueueValidator != null) {
                schedulerService.unregisterTask(taskIdQueueValidator);
            }
        }
        registeredSchedules.clear();
        taskIdQueueValidator = null;
    }

    void synchronizeAllTasks() {
        stopAllTasks();
        startAllTasks();
    }

    @Override
    public void onEvent(EventCustomizingUpdate event) {
        if (ValidationsResource.MAPPINGS_KEY.equals(event.getCustomizationName())) {
            synchronizeAllTasks();
        }
    }

    // interface Monitorable

    static final String SERVICE_COMPONENT_NAME = "org.eclipse.skalli.core.validation"; //$NON-NLS-1$

    @Override
    public String getServiceComponentName() {
        return SERVICE_COMPONENT_NAME;
    }

    @Override
    public Set<String> getResourceNames() {
        return CollectionUtils.asSet(QueueMonitorResource.RESOURCE_NAME);
    }

    @Override
    public Class<? extends ServerResource> getServerResource(String resourceName) {
        if (QueueMonitorResource.RESOURCE_NAME.equals(resourceName)) {
            return QueueMonitorResource.class;
        }
        return null;
    }
}
