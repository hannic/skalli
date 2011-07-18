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
package org.eclipse.skalli.model.ext.scrum;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.skalli.api.java.ProjectService;
import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.testutil.ProjectServiceUtils;
import org.eclipse.skalli.testutil.RestUtils;

public class ScrumRestAPITest {
    private List<Project> projects;
    private ProjectService projectService;

    @Before
    public void setup() throws Exception {
        projectService = ProjectServiceUtils.getProjectService();
        projects = projectService.getAll();
        Assert.assertTrue("projects.size() > 0", projects.size() > 0);
    }

    @Test
    public void testValidate() throws Exception {
        RestUtils.validate(projects, ScrumProjectExt.class, "extension-scrum.xsd");
    }
}
