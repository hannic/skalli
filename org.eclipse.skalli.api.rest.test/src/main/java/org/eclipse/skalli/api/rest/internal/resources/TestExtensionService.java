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
package org.eclipse.skalli.api.rest.internal.resources;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.eclipse.skalli.model.ext.AbstractIndexer;
import org.eclipse.skalli.model.ext.AliasedConverter;
import org.eclipse.skalli.model.ext.DataMigration;
import org.eclipse.skalli.model.ext.ExtensionService;
import org.eclipse.skalli.model.ext.ExtensionValidator;
import org.eclipse.skalli.model.ext.PropertyValidator;
import org.eclipse.skalli.testutil.TestExtension;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

class TestExtensionService implements ExtensionService<TestExtension> {
  @Override
  public Class<TestExtension> getExtensionClass() {
    return TestExtension.class;
  }
  @Override
  public Set<DataMigration> getMigrations() {
    return null;
  }
  @Override
  public String getShortName() {
    return "testExtension";
  }
  @Override
  public AliasedConverter getConverter(String host) {
    return new AliasedConverter() {
      @Override
      public void marshal(Object obj, HierarchicalStreamWriter hierarchicalstreamwriter,
          MarshallingContext marshallingcontext) {
      }

      @Override
      public Object unmarshal(HierarchicalStreamReader hierarchicalstreamreader,
          UnmarshallingContext unmarshallingcontext) {
        return null;
      }

      @Override
      public boolean canConvert(Class class1) {
        return class1.equals(TestExtension.class);
      }

      @Override
      public String getAlias() {
        return null;
      }

      @Override
      public Class<?> getConversionClass() {
        return TestExtension.class;
      }

      @Override
      public String getApiVersion() {
        return "1.0";
      }

      @Override
      public String getNamespace() {
        return "tiffy";
      }

      @Override
      public String getXsdFileName() {
        return "tiffy.xsd";
      }

      @Override
      public String getHost() {
        return "https://localhost";
      }};


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
  public AbstractIndexer<TestExtension> getIndexer() {
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
  public Set<ExtensionValidator<TestExtension>> getExtensionValidators(Map<String, String> captions) {
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

