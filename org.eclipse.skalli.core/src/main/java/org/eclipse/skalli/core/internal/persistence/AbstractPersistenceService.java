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
import java.util.logging.Logger;

import org.eclipse.skalli.api.java.PersistenceService;
import org.eclipse.skalli.log.Log;
import org.eclipse.skalli.model.ext.ExtensionService;

public abstract class AbstractPersistenceService implements PersistenceService {
  private static final Logger LOG = Log.getLogger(AbstractPersistenceService.class);

  private final Set<ExtensionService<?>> extensionServices = new HashSet<ExtensionService<?>>();
  private final Map<String, ExtensionService<?>> extensionNameRegistry = new HashMap<String, ExtensionService<?>>();

  protected synchronized void bindExtensionService(ExtensionService<?> extensionService) {
    if (extensionNameRegistry.get(extensionService.getExtensionClass().getName()) != null) {
      throw new RuntimeException("There is already an extension registered with the following name: " + extensionService.getExtensionClass().getName()); //$NON-NLS-1$
    }
    LOG.fine("Registering extension class: " + extensionService.getExtensionClass().getName()); //$NON-NLS-1$
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

}

