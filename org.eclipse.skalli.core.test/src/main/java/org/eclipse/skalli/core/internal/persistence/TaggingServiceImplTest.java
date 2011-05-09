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

import static org.easymock.EasyMock.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.skalli.api.java.PersistenceService;
import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.ext.Taggable;

public class TaggingServiceImplTest {

    private List<Project> projects;
    private UUID uuid1 = UUID.randomUUID();
    private UUID uuid2 = UUID.randomUUID();
    private UUID uuid3 = UUID.randomUUID();
    private Object[] mocks;
    private PersistenceService mockIPS;
    private TaggingServiceImpl ts;

    private Project createProject(UUID uuid, String projectId, Project parent, String[] tags) {
        Project ret = new Project();
        ret.setProjectId(projectId);
        ret.setUuid(uuid);
        if (parent != null) {
            ret.setParentEntity(parent);
        }
        if (tags != null) {
            for (String tag : tags) {
                ret.addTag(tag);
            }
        }
        return ret;
    }

    @Before
    public void setup() {
        projects = new LinkedList<Project>();
        projects.add(createProject(uuid1, "project1", null, new String[] {})); //$NON-NLS-1$
        projects.add(createProject(uuid2, "project2", projects.get(0), new String[] { "tagBoth", "tag2" })); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        projects.add(createProject(uuid3, "project3", projects.get(1), new String[] { "tagBoth", "tag3" })); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        mockIPS = createNiceMock(PersistenceService.class);
        mocks = new Object[] { mockIPS };
        ts = new TaggingServiceImpl();
        ts.bindPersistenceService(mockIPS);

        reset(mocks);

        mockIPS.getEntities(eq(Project.class));
        expectLastCall().andReturn(projects).anyTimes();

        replay(mocks);
    }

    @Test
    public void testGetAvailableTags() {
        Set<String> res = ts.getAvailableTags();
        Assert.assertNotNull(res);
        Assert.assertEquals(3, res.size());
    }

    @Test
    public void testGetTaggables() {
        Map<String, Set<Taggable>> res = ts.getTaggables();
        Assert.assertNotNull(res);
        Assert.assertEquals(2, res.get("tagBoth").size());
        Assert.assertEquals(1, res.get("tag2").size());
        Assert.assertEquals(1, res.get("tag3").size());
    }

}
