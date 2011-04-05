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
package org.eclipse.skalli.model.core;

import java.util.Map;
import java.util.Set;

/**
 * Service that can return people regardless of their roles from a {@link Project}.
 *
 * For each new location where {@link ProjectMember}s are stored, a corresponding implementation of this interface needs to be registered.
 *
 * <p>
 * Remark: This interface "knows" about {@link Project} and {@link ProjectMember} entities and is referenced in bundles contributing model extensions.
 * Hence this interface needs to be located inside the <code>.core.model</code> bundle and cannot be moved to the <code>.common</code> bundle.
 * </p>
 * @author d049863
 *
 */
public interface PeopleProvider {

  public Map<String,Set<ProjectMember>> getPeople(Project project);

  public void addPerson(Project project, String role, ProjectMember person);

}

