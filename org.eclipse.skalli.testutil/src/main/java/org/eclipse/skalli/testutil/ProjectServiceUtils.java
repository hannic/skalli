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
package org.eclipse.skalli.testutil;

import org.osgi.framework.BundleException;

import org.eclipse.skalli.api.java.ProjectService;
import org.eclipse.skalli.common.Services;

@SuppressWarnings("nls")
public class ProjectServiceUtils {

  public static ProjectService getProjectService() throws BundleException {
    new BundleManager(ProjectServiceUtils.class).startBundles();
    return Services.getRequiredService(ProjectService.class);
  }
}

