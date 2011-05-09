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
package org.eclipse.skalli.core.internal.persistence.xstream;

import java.io.File;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.api.java.EntityFilter;
import org.eclipse.skalli.api.java.PersistenceService;
import org.eclipse.skalli.core.internal.persistence.AbstractPersistenceService;
import org.eclipse.skalli.log.Log;
import org.eclipse.skalli.model.ext.DataMigration;
import org.eclipse.skalli.model.ext.EntityBase;
import org.eclipse.skalli.model.ext.ExtensionService;
import org.osgi.service.component.ComponentContext;

/**
 * Helper class for marshalling/unmarshalling entities to/from the file system
 * and caching of undeleted and deleted entity instances.
 *
 * This class evaluates the system property "workdir". Setting this property
 * allows to choose an arbitrary storage directory (e.g. for testing purposes). If not
 * set, the current directory is used by default. In both cases, this class expects
 * to find a folder named <code>"storage"</code> in the given directory.
 */
public class PersistenceServiceXStream extends AbstractPersistenceService implements PersistenceService {

    private static final Logger LOG = Log.getLogger(PersistenceServiceXStream.class);
    private static final String PROPERTIES_FILE = "/skalli.properties"; //$NON-NLS-1$
    private static final String PROPERTY_WORKDIR = "workdir"; //$NON-NLS-1$
    private static final String STORAGE_BASE = "storage" + IOUtils.DIR_SEPARATOR; //$NON-NLS-1$

    public static final String ENTITY_PREFIX = "entity-"; //$NON-NLS-1$

    private File storageDirectory;
    private final DataModelContainer cache = new DataModelContainer();
    private final DataModelContainer deleted = new DataModelContainer();

    private XStreamPersistence xstreamPersistence;

    /**
     * Creates a new, unitialized <code>PersistenceServiceXStream</code>.
     * Note, this class should not be instantiated directly except for testing purposes.
     */
    public PersistenceServiceXStream() {
        initializeStorage();
    }

    protected void activate(ComponentContext context) {
        initializeStorage();
        LOG.info("Persistence service activated"); //$NON-NLS-1$
    }

    protected void deactivate(ComponentContext context) {
        xstreamPersistence = null;
        cache.clearAll();
        deleted.clearAll();
        LOG.info("Persistence service deactivated"); //$NON-NLS-1$
    }

    @Override
    protected synchronized void bindExtensionService(ExtensionService<?> extensionService) {
        super.bindExtensionService(extensionService);
        cache.clearAll();
        deleted.clearAll();
    }

    private void initializeStorage() {
        if (storageDirectory == null) {
            String workdir = null;
            try {
                // try to get working directory from configuration file
                Properties properties = new Properties();
                properties.load(getClass().getResourceAsStream(PROPERTIES_FILE));
                workdir = properties.getProperty(PROPERTY_WORKDIR);
                if (StringUtils.isBlank(workdir)) {
                    LOG.warning("property '" + PROPERTY_WORKDIR + "' not defined in configuration file '"
                            + PROPERTIES_FILE + "' - " +
                            " falling back to system property '" + PROPERTY_WORKDIR + "'.");
                }
            } catch (Exception e) {
                LOG.warning("cannot read configuration file '" + PROPERTIES_FILE + "' - " +
                        "falling back to system properties.");
            }

            if (StringUtils.isBlank(workdir)) {
                // fall back: get working directory from system property
                workdir = System.getProperty(PROPERTY_WORKDIR);
                if (StringUtils.isBlank(workdir)) {
                    LOG.warning("cannot get system property '" + PROPERTY_WORKDIR + "' - " +
                            "falling back to working directory.");
                }
            }

            if (workdir != null) {
                File workingDirectory = new File(workdir);
                if (workingDirectory.exists() && workingDirectory.isDirectory()) {
                    storageDirectory = new File(workingDirectory, STORAGE_BASE);
                } else {
                    LOG.warning("Working directory " + workingDirectory.getAbsolutePath()
                            + " not found - falling back to current directory.");
                }
            }
            if (storageDirectory == null) {
                storageDirectory = new File(STORAGE_BASE);
            }
            LOG.info("use storage folder '" + storageDirectory.getAbsolutePath() + "' for persistence.");
        }
        xstreamPersistence = new XStreamPersistence(storageDirectory);
    }

    protected File getStorageDirectory() {
        return storageDirectory;
    }

    protected void setStorageDirectory(File storageDirectory) {
        this.storageDirectory = storageDirectory;
        xstreamPersistence = new XStreamPersistence(storageDirectory);
        cache.clearAll();
        deleted.clearAll();
    }

    protected Set<DataMigration> getMigrations() {
        Set<DataMigration> ret = new HashSet<DataMigration>();
        for (ExtensionService<?> extensionService : getExtensionServices()) {
            if (extensionService.getMigrations() != null) {
                ret.addAll(extensionService.getMigrations());
            }
        }
        return ret;
    }

    protected Map<String, Class<?>> getAliases() {
        Map<String, Class<?>> ret = new HashMap<String, Class<?>>();
        for (ExtensionService<?> extensionService : getExtensionServices()) {
            ret.put(ENTITY_PREFIX + extensionService.getShortName(), extensionService.getExtensionClass());
        }
        return ret;
    }

    protected Set<ClassLoader> getEntityClassLoaders() {
        Set<ClassLoader> classLoaders = new HashSet<ClassLoader>();
        for (ExtensionService<?> extensionService : getExtensionServices()) {
            classLoaders.add(extensionService.getClass().getClassLoader());
        }
        return classLoaders;
    }

    /**
     * Loads all entities of the given class from the storage folder
     * <code>STORAGE_DIR/entityClass.getSimpleName()</code>. Resolves
     * the parent hierarchy of the loaded entities and stores the
     * result in the model caches (deleted entities in {@link #deleted},
     * all other in {@link #cache}).
     *
     * @param entityClass   the class the entities belongs to.
     */
    private synchronized void loadModel(Class<? extends EntityBase> entityClass) {
        if (cache.size(entityClass) > 0) {
            return;
        }

        File storageBase = new File(storageDirectory, entityClass.getSimpleName());
        if (!storageBase.exists()) {
            return;
        }

        LOG.info("Loading models from " + storageBase.getAbsolutePath()); //$NON-NLS-1$

        @SuppressWarnings("unchecked")
        Iterator<File> files = FileUtils.iterateFiles(storageBase, new String[] { "xml" }, true); //$NON-NLS-1$
        while (files.hasNext()) {
            File file = files.next();
            LOG.info("  Loading " + file.getAbsolutePath()); //$NON-NLS-1$
            EntityBase entity = loadEntity(file);
            updateCache(entity);
        }

        resolveParentEntities(entityClass);
    }

    /**
     * Loads an entity from a given file.
     * Note, this method should not be called directly except for testing purposes.
     * @param file  the file to load as entity.
     */
    protected EntityBase loadEntity(File file) {
        return xstreamPersistence.loadFromFile(getEntityClassLoaders(), getMigrations(), getAliases(), file);
    }

    @Override
    public synchronized void persist(EntityBase objToPersist, String userId) {
        if (objToPersist == null) {
            throw new IllegalArgumentException("argument 'objToPersist' must not be null");
        }
        if (StringUtils.isBlank(userId)) {
            throw new IllegalArgumentException("argument 'userId' must not be null or an empty string");
        }

        // load all project models
        loadModel(objToPersist.getClass());

        // generate unique id
        if (objToPersist.getUuid() == null) {
            objToPersist.setUuid(UUID.randomUUID());
        }

        // verify parent is known
        // TODO should be in EntitySeriviceImpl#validate
        if (objToPersist.getParentEntityId() != null) {
            UUID parentUUID = objToPersist.getParentEntityId();
            EntityBase parent = getParentEntity(objToPersist.getClass(), objToPersist);
            if (parent == null) {
                throw new RuntimeException("Parent entity " + parentUUID + " does not exist");
            }
        }

        File file = xstreamPersistence.saveToFile(objToPersist, userId, getAliases());

        // validate reading
        EntityBase validation = loadEntity(objToPersist.getClass(), objToPersist.getUuid());
        if (validation != null) {
            updateCache(validation);
            LOG.fine("validation succeeded (" + validation.getUuid() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            LOG.severe("Validation of " + file.getAbsolutePath() + " failed"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * Adds the given entity to the cache (deleted entities in {@link #deleted},
     * all other in {@link #cache}).
     *
     * Note, this method should not be called directly except for testing purposes.
     *
     * @param entity  the entity to add.
     */
    void updateCache(EntityBase entity) {
        if (entity.isDeleted()) {
            cache.removeEntity(entity);
            deleted.putEntity(entity);
        } else {
            deleted.removeEntity(entity);
            cache.putEntity(entity);
        }
    }

    /**
     * Resolves the parent hierarchies for all entities of the given class.
     * This method resolves the parents both for deleted and non-deleted entities.
     * It assumes, that the entity caches for the given entity class have already
     * been initialized with {@link #loadModel(Class)}.
     *
     * Note, this method should not be called directly except for testing purposes.
     *
     * @param <T>  type of an antity derived from <code>EntityBase</code>.
     * @param entityClass  the class of entities to resolve.
     */
    <T extends EntityBase> void resolveParentEntities(Class<T> entityClass) {
        resolveParentEntities(entityClass, cache.getEntities(entityClass));
        resolveParentEntities(entityClass, deleted.getEntities(entityClass));
    }

    private <T extends EntityBase> void resolveParentEntities(Class<T> entityClass, List<T> entities) {
        for (T entity : entities) {
            UUID parentId = entity.getParentEntityId();
            if (parentId != null) {
                T parentEntity = getParentEntity(entityClass, entity);
                if (parentEntity == null) {
                    LOG.warning("Entity " + entity.getUuid() + " references entity " + parentId
                            + " as parent entity - but there is no such entity");
                    continue;
                }
                if (parentEntity.isDeleted() && !entity.isDeleted()) {
                    LOG.warning("Entity " + entity.getUuid() + " cannot reference deleted entity "
                            + parentId + " as parent entity");
                    continue;
                }
                entity.setParentEntity(parentEntity);
            }
        }
    }

    private <T extends EntityBase> T getParentEntity(Class<T> entityClass, EntityBase entity) {
        UUID parentId = entity.getParentEntityId();
        T parentEntity = null;
        if (parentId != null) {
            if (!entity.isDeleted()) {
                // undeleted entities can only reference undeleted entities
                parentEntity = cache.getEntity(entityClass, parentId);
            }
            else {
                // deleted entities can reference deleted & undeleted entities
                parentEntity = cache.getEntity(entityClass, parentId);
                if (parentEntity == null) {
                    parentEntity = deleted.getEntity(entityClass, parentId);
                }
            }
        }
        return parentEntity;
    }

    /**
     * Loads the entity with the given UUID from a file. The file is searched in
     * <code>STRORAGE_BASE/entityClass.getSimpleName()</code> and it is expected that
     * its name is <code>uuid.toString()</code> with the extension <code>.xml</code>.
     * This method loads the parent hierarchy of the entity, too, if available.
     *
     * @param entityClass  the class the entity belongs to.
     * @param uuid  the unique identifier of the entity.
     *
     * @return  the entity, or <code>null</code> if either the storage directory
     * for entities of the given class does not exist, or there is no file for
     * the entity.
     */
    @Override
    public <T extends EntityBase> T loadEntity(Class<T> entityClass, UUID uuid) {
        File storageBase = new File(storageDirectory, entityClass.getSimpleName());
        if (!storageBase.exists()) {
            return null;
        }
        File file = new File(storageBase, uuid.toString() + ".xml"); //$NON-NLS-1$

        if (!file.isFile()) {
            LOG.info(MessageFormat.format("{0} not found", file.getAbsolutePath()));
            return null;
        }

        T entity = entityClass.cast(loadEntity(file)); // safe, because entities of different type are stored in different folders
        if (entity != null) {
            // resolve the parent entity
            UUID parentId = entity.getParentEntityId();
            if (parentId != null) {
                EntityBase parentEntity = null;
                parentEntity = getParentEntity(entityClass, entity);
                if (parentEntity == null) {
                    // Fallback: try to load it
                    parentEntity = loadEntity(entityClass, parentId);
                }
                if (parentEntity == null) {
                    throw new RuntimeException(MessageFormat.format("Parent entity {0} does not exist", parentId));
                }
                entity.setParentEntity(parentEntity);
            }
            // update the parentEntity of all direct children
            for (T childEntity : cache.getEntities(entityClass)) {
                if (uuid.equals(childEntity.getParentEntityId())) {
                    childEntity.setParentEntity(entity);
                }
            }
        }
        return entity;
    }

    @Override
    public <T extends EntityBase> T getEntity(Class<T> entityClass, UUID uuid) {
        loadModel(entityClass);
        return cache.getEntity(entityClass, uuid);
    }

    @Override
    public <T extends EntityBase> List<T> getEntities(Class<T> entityClass) {
        loadModel(entityClass);
        return cache.getEntities(entityClass);
    }

    @Override
    public <T extends EntityBase> T getEntity(Class<T> entityClass, EntityFilter<T> filter) {
        loadModel(entityClass);
        return cache.getEntity(entityClass, filter);
    }

    @Override
    public <T extends EntityBase> T getDeletedEntity(Class<T> entityClass, UUID uuid) {
        loadModel(entityClass);
        return deleted.getEntity(entityClass, uuid);
    }

    @Override
    public <T extends EntityBase> List<T> getDeletedEntities(Class<T> entityClass) {
        loadModel(entityClass);
        return deleted.getEntities(entityClass);
    }

}
