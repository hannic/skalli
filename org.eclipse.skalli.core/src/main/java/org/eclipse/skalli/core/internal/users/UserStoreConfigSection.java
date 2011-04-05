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
package org.eclipse.skalli.core.internal.users;

import java.util.logging.Logger;

import org.osgi.service.component.ComponentContext;
import org.restlet.resource.ServerResource;

import org.eclipse.skalli.api.rest.config.ConfigSection;
import org.eclipse.skalli.log.Log;

public class UserStoreConfigSection implements ConfigSection {
  private static final Logger LOG = Log.getLogger(UserStoreConfigSection.class);

  protected void activate(ComponentContext context){
    LOG.info("UserStoreConfig activated"); //$NON-NLS-1$
  }

  protected void deactivate(ComponentContext context) {
  }

  @Override
  public String getName() {
    return "userStore"; //$NON-NLS-1$
  }

  @Override
  public Class<? extends ServerResource> getServerResource() {
    return UserStoreResource.class;
  }


}

