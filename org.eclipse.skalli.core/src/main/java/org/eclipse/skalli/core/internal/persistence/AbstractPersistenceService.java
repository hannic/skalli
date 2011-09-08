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
package org.eclipse.skalli.core.internal.persistence;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.skalli.api.java.PersistenceService;
import org.eclipse.skalli.model.ext.DataMigration;
import org.eclipse.skalli.model.ext.ExtensionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractPersistenceService implements PersistenceService {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractPersistenceService.class);

    private final Set<ExtensionService<?>> extensionServices = new HashSet<ExtensionService<?>>();
    private final Map<String, ExtensionService<?>> extensionNameRegistry = new HashMap<String, ExtensionService<?>>();

    protected static final String ENTITY_PREFIX = "entity-"; //$NON-NLS-1$

    protected synchronized void bindExtensionService(ExtensionService<?> extensionService) {
        if (extensionNameRegistry.get(extensionService.getExtensionClass().getName()) != null) {
            throw new RuntimeException(
                    "There is already an extension registered with the following name: " + extensionService.getExtensionClass().getName()); //$NON-NLS-1$
        }
        LOG.debug("Registering extension class: " + extensionService.getExtensionClass().getName()); //$NON-NLS-1$
        extensionNameRegistry.put(extensionService.getExtensionClass().getName(), extensionService);
        extensionServices.add(extensionService);
    }

    protected synchronized void unbindExtensionService(ExtensionService<?> extensionService) {
        extensionNameRegistry.remove(extensionService.getExtensionClass().getName());
        extensionServices.remove(extensionService);
    }

    protected Set<ExtensionService<?>> getExtensionServices() {
        return extensionServices;
    }

    protected Set<DataMigration> getMigrations() {
        Set<DataMigration> ret = new HashSet<DataMigration>();
        for (ExtensionService<?> extensionService : extensionServices) {
            if (extensionService.getMigrations() != null) {
                ret.addAll(extensionService.getMigrations());
            }
        }
        return ret;
    }

    protected Map<String, Class<?>> getAliases() {
        Map<String, Class<?>> ret = new HashMap<String, Class<?>>();
        for (ExtensionService<?> extensionService : extensionServices) {
            ret.put(ENTITY_PREFIX + extensionService.getShortName(), extensionService.getExtensionClass());
        }
        return ret;
    }

    protected Set<ClassLoader> getEntityClassLoaders() {
        Set<ClassLoader> classLoaders = new HashSet<ClassLoader>();
        for (ExtensionService<?> extensionService : extensionServices) {
            classLoaders.add(extensionService.getClass().getClassLoader());
        }
        return classLoaders;
    }

}
