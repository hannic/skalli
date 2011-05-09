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
package org.eclipse.skalli.view.ext;

import org.eclipse.skalli.common.User;
import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.core.ProjectTemplate;

public interface ExtensionUtil {

    public void persist(Project project);

    public boolean isUserAdmin();

    public boolean isUserProjectAdmin(Project project);

    public User getLoggedInUser();

    public Navigator getNavigator();

    public ProjectTemplate getProjectTemplate();
}
