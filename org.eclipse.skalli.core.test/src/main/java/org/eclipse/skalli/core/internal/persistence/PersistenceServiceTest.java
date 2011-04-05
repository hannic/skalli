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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;

import org.eclipse.skalli.api.java.EntityFilter;
import org.eclipse.skalli.model.ext.AbstractIndexer;
import org.eclipse.skalli.model.ext.AliasedConverter;
import org.eclipse.skalli.model.ext.DataMigration;
import org.eclipse.skalli.model.ext.EntityBase;
import org.eclipse.skalli.model.ext.ExtensionEntityBase;
import org.eclipse.skalli.model.ext.ExtensionService;
import org.eclipse.skalli.model.ext.ExtensionValidator;
import org.eclipse.skalli.model.ext.PropertyValidator;

// test for AbstractPersistenceService
public class PersistenceServiceTest {

  private static class TestAbstractPersistenceService extends AbstractPersistenceService {
    @Override
    public void persist(EntityBase objToPersist, String userId) {
    }

    @Override
    public <T extends EntityBase> T getEntity(Class<T> entityClass, UUID uuid) {
      return null;
    }

    @Override
    public <T extends EntityBase> List<T> getEntities(Class<T> entityClass) {
      return null;
    }

    @Override
    public <T extends EntityBase> T getEntity(Class<T> entityClass, EntityFilter<T> filter) {
      return null;
    }

    @Override
    public <T extends EntityBase> T getDeletedEntity(Class<T> entityClass, UUID uuid) {
      return null;
    }

    @Override
    public <T extends EntityBase> List<T> getDeletedEntities(Class<T> entityClass) {
      return null;
    }

    @Override
    public <T extends EntityBase> T loadEntity(Class<T> entityClass, UUID uuid) {
      return null;
    }
  }

  private static class TestExtensionService implements ExtensionService<ExtensionEntityBase> {
    @Override
    public Class<ExtensionEntityBase> getExtensionClass() {
      return ExtensionEntityBase.class;
    }
    @Override
    public Set<DataMigration> getMigrations() {
      return null;
    }
    @Override
    public String getShortName() {
      return ":-)"; //$NON-NLS-1$
    }
    @Override
    public AliasedConverter getConverter(String host) {
      return null;
    }
    @Override
    public Set<String> getProjectTemplateIds() {
      return null;
    }

    @Override
    public String getModelVersion() {
      return null;
    }
    @Override
    public String getNamespace() {
      return null;
    }
    @Override
    public String getXsdFileName() {
      return null;
    }

    @Override
    public AbstractIndexer<ExtensionEntityBase> getIndexer() {
      return null;
    }
    @Override
    public String getCaption(String propertyName) {
      return null;
    }
    @Override
    public String getDescription(String propertyName) {
      return null;
    }
    @Override
    public Set<PropertyValidator> getPropertyValidators(String propertyName, String caption) {
      return Collections.emptySet();
    }
    @Override
    public Set<ExtensionValidator<ExtensionEntityBase>> getExtensionValidators(Map<String, String> captions) {
      return Collections.emptySet();
    }
    @Override
    public String getCaption() {
      return "caption"; //$NON-NLS-1$
    }
    @Override
    public String getDescription() {
      return "description"; //$NON-NLS-1$
    }
  };

  private final TestExtensionService es1 = new TestExtensionService();
  private final TestExtensionService es2 = new TestExtensionService();

  @Test
  public void testBindExtensionService() {
    TestAbstractPersistenceService aps = new TestAbstractPersistenceService();
    aps.bindExtensionService(es1);
  }

  @Test(expected=RuntimeException.class)
  public void testBindExtensionService_twice() {
    TestAbstractPersistenceService aps = new TestAbstractPersistenceService();
    aps.bindExtensionService(es1);
    aps.bindExtensionService(es2);
  }

}

