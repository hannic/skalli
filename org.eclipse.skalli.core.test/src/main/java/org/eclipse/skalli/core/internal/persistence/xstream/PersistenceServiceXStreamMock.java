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

import java.util.Set;

import org.eclipse.skalli.api.java.StorageService;
import org.eclipse.skalli.model.ext.DataMigration;
import org.eclipse.skalli.model.ext.ExtensionService;

public class PersistenceServiceXStreamMock extends PersistenceServiceXStream {

    /** Array of extension services */
    private ExtensionService<?>[] extensionServices;

    public PersistenceServiceXStreamMock( StorageService storageService, ExtensionService<?>... extensionServices) {
        super(new XStreamPersistence(storageService));
        this.extensionServices = extensionServices;
    }

    @Override
    protected Set<DataMigration> getMigrations() {
        Set<DataMigration> migrations = super.getMigrations();
        if (extensionServices != null) {
            for (ExtensionService<?> extensionService : extensionServices) {
                migrations.addAll(extensionService.getMigrations());
            }
        }
        return migrations;
    }

    @Override
    protected Set<ClassLoader> getEntityClassLoaders() {
        Set<ClassLoader> classLoaders = super.getEntityClassLoaders();
        if (extensionServices != null) {
            for (ExtensionService<?> extensionService : extensionServices) {
                classLoaders.add(extensionService.getClass().getClassLoader());
            }
        }
        return classLoaders;
    }
}
