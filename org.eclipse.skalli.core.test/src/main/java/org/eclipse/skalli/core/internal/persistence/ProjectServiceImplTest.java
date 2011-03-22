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

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleException;

import org.eclipse.skalli.api.java.PersistenceService;
import org.eclipse.skalli.api.java.ProjectTemplateService;
import org.eclipse.skalli.model.core.DefaultProjectTemplate;
import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.core.ProjectMember;
import org.eclipse.skalli.model.core.ProjectNature;
import org.eclipse.skalli.model.ext.people.PeopleProjectExt;
import org.eclipse.skalli.testutil.BundleManager;

@SuppressWarnings("nls")
public class ProjectServiceImplTest {

  private static class TestProjectTemplate1 extends DefaultProjectTemplate {
    @Override
    public String getId() {
      return "projecttemplate1";
    }
    @Override
    public ProjectNature getProjectNature() {
      return ProjectNature.PROJECT;
    }
  }
  private static class TestComponentTemplate1 extends DefaultProjectTemplate {
    @Override
    public String getId() {
      return "comptemplate1";
    }
    @Override
    public ProjectNature getProjectNature() {
      return ProjectNature.COMPONENT;
    }
  }
  private static class TestComponentTemplate2 extends DefaultProjectTemplate {
    @Override
    public String getId() {
      return "comptemplate2";
    }
    @Override
    public ProjectNature getProjectNature() {
      return ProjectNature.COMPONENT;
    }
  }

  private List<Project> projects;
  private List<Project> deletedprojects;
  protected UUID[] uuids = new UUID[9];
  protected Object[] mocks;
  protected PersistenceService mockIPS;
  protected ProjectTemplateService mockTS;
  private ProjectServiceImpl ps;
  private ProjectMember m1;
  private ProjectMember m2;
  private ProjectMember l1;
  private ProjectMember l2;

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

  private Project createDeletedProject(UUID uuid, String projectId, Project parent, String[] tags) {
    Project ret = createProject(uuid, projectId, parent, tags);
    ret.setDeleted(true);
    return ret;
  }

  @Before
  public void setup() throws BundleException {
    new BundleManager(this.getClass()).startBundles();

    for (int i=0; i<uuids.length; ++i) {
      uuids[i] = UUID.randomUUID();
    }

    projects = new LinkedList<Project>();
    projects.add(createProject(uuids[1], "project1", null, new String[] {})); //$NON-NLS-1$
    projects.add(createProject(uuids[2], "project2", projects.get(0), new String[] {"tagBoth", "tag2"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    projects.get(1).setProjectTemplateId("projecttemplate1");
    projects.add(createProject(uuids[3], "project3", projects.get(1), new String[] {"tagBoth", "tag3"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    deletedprojects = new LinkedList<Project>();
    deletedprojects.add(createDeletedProject(uuids[4], "project4", null, new String[] {"tag2"})); //$NON-NLS-1$
    deletedprojects.add(createDeletedProject(uuids[5], "project5", null, new String[] {"tag4"})); //$NON-NLS-1$

    projects.add(createProject(uuids[6], "comp1", projects.get(2), new String[] {}));
    projects.get(3).setProjectTemplateId("comptemplate1");
    projects.add(createProject(uuids[7], "comp2", projects.get(3), new String[] {}));
    projects.get(4).setProjectTemplateId("comptemplate2");
    projects.add(createProject(uuids[8], "comp3", projects.get(2), new String[] {}));
    projects.get(5).setProjectTemplateId("comptemplate1");

    mockIPS = createNiceMock(PersistenceService.class);
    mockTS = createNiceMock(ProjectTemplateService.class);
    mocks = new Object[] {mockIPS, mockTS};
    ps = new ProjectServiceImpl();
    ps.bindPersistenceService(mockIPS);
    ps.bindProjectTemplateService(mockTS);

    reset(mocks);
    recordMocks();
    replay(mocks);

    m1 = new ProjectMember("M1");
    m2 = new ProjectMember("M2");
    l1 = new ProjectMember("L1");
    l2 = new ProjectMember("L2");
  }

  protected void recordMocks() {
    mockIPS.getEntities(eq(Project.class));
    expectLastCall().andReturn(projects).anyTimes();

    mockIPS.getEntity(eq(Project.class), eq(uuids[1]));
    expectLastCall().andReturn(projects.get(0)).anyTimes();

    mockIPS.getEntity(eq(Project.class), eq(uuids[2]));
    expectLastCall().andReturn(projects.get(1)).anyTimes();

    mockIPS.getEntity(eq(Project.class), eq(uuids[3]));
    expectLastCall().andReturn(projects.get(2)).anyTimes();

    mockIPS.getEntity(eq(Project.class), eq(uuids[6]));
    expectLastCall().andReturn(projects.get(3)).anyTimes();

    mockIPS.getEntity(eq(Project.class), eq(uuids[7]));
    expectLastCall().andReturn(projects.get(4)).anyTimes();

    mockIPS.getEntity(eq(Project.class), eq(uuids[8]));
    expectLastCall().andReturn(projects.get(5)).anyTimes();

    mockIPS.getDeletedEntity(eq(Project.class), eq(uuids[4]));
    expectLastCall().andReturn(deletedprojects.get(0)).anyTimes();

    mockIPS.getDeletedEntity(eq(Project.class), eq(uuids[5]));
    expectLastCall().andReturn(deletedprojects.get(1)).anyTimes();

    mockIPS.getDeletedEntities(eq(Project.class));
    expectLastCall().andReturn(deletedprojects).anyTimes();

    mockTS.getProjectTemplateById(eq("default"));
    expectLastCall().andReturn(new DefaultProjectTemplate()).anyTimes();

    mockTS.getProjectTemplateById(eq("projecttemplate1"));
    expectLastCall().andReturn(new TestProjectTemplate1()).anyTimes();

    mockTS.getProjectTemplateById(eq("comptemplate1"));
    expectLastCall().andReturn(new TestComponentTemplate1()).anyTimes();

    mockTS.getProjectTemplateById(eq("comptemplate2"));
    expectLastCall().andReturn(new TestComponentTemplate2()).anyTimes();
  }

  @Test
  public void testGetProjects() {
    List<Project> res = ps.getAll();
    Assert.assertNotNull(res);
    Assert.assertEquals(6, res.size());

    verify(mocks);
  }

  @Test
  public void testGetSortedProjects() {
    List<Project> res = ps.getProjects(new Comparator<Project>() {
      @Override
      public int compare(Project p1, Project p2) {
        // reverse ordering by project id!
        return p2.getProjectId().compareTo(p1.getProjectId());
    }
    });
    Assert.assertNotNull(res);
    Assert.assertEquals(6, res.size());

    Assert.assertEquals(uuids[3], res.get(0).getUuid());
    Assert.assertEquals(uuids[2], res.get(1).getUuid());
    Assert.assertEquals(uuids[1], res.get(2).getUuid());
    Assert.assertEquals(uuids[8], res.get(3).getUuid());
    Assert.assertEquals(uuids[7], res.get(4).getUuid());
    Assert.assertEquals(uuids[6], res.get(5).getUuid());
    verify(mocks);
  }

  @Test
  public void testGetSortedDeletedProjects() {
    List<Project> res = ps.getDeletedProjects(new Comparator<Project>() {
      @Override
      public int compare(Project p1, Project p2) {
        // reverse ordering by project id!
        return p2.getProjectId().compareTo(p1.getProjectId());
    }
    });
    Assert.assertNotNull(res);
    Assert.assertEquals(2, res.size());

    Assert.assertEquals(uuids[5], res.get(0).getUuid());
    Assert.assertEquals(uuids[4], res.get(1).getUuid());
    verify(mocks);
  }

  @Test
  public void testGetProjectByUUID() {
    Project res1 = ps.getByUUID(uuids[2]);
    Assert.assertNotNull(res1);
    Assert.assertEquals(uuids[2], res1.getUuid());

    Project res2 = ps.getByUUID(uuids[3]);
    Assert.assertNotNull(res2);
    Assert.assertEquals(uuids[3], res2.getUuid());

    Project res3 = ps.getByUUID(uuids[4]);
    Assert.assertNull(res3);

    verify(mocks);
  }

  @Test
  public void testGetProjectByProjectId() {
    Project res1 = ps.getProjectByProjectId("project2"); //$NON-NLS-1$
    Assert.assertNotNull(res1);
    Assert.assertEquals(projects.get(1), res1);

    Project res2 = ps.getProjectByProjectId("project_nonExisting"); //$NON-NLS-1$
    Assert.assertNull(res2);

    // try to retrieve deleted project
    Project res3 = ps.getProjectByProjectId("project5"); //$NON-NLS-1$
    Assert.assertNull(res3);

    verify(mocks);
  }

  @Test
  public void testGetSubProjects() {
    List<Project> res1 = ps.getSubProjects(uuids[1]);
    Assert.assertNotNull(res1);
    Assert.assertEquals(1, res1.size());
    Assert.assertEquals(uuids[2], res1.iterator().next().getUuid());

    List<Project> res2 = ps.getSubProjects(uuids[2]);
    Assert.assertNotNull(res2);
    Assert.assertEquals(1, res2.size());
    Assert.assertEquals(uuids[3], res2.iterator().next().getUuid());

    List<Project> res3 = ps.getSubProjects(uuids[3]);
    Assert.assertNotNull(res3);
    Assert.assertEquals(2, res3.size());
    Set<UUID> res3uuids = new HashSet<UUID>(res3.size());
    for (Project p: res3) {
      res3uuids.add(p.getUuid());
    }
    Assert.assertTrue(res3uuids.contains(uuids[6]));
    Assert.assertTrue(res3uuids.contains(uuids[8]));

    List<Project> res4 = ps.getSubProjects(uuids[6]);
    Assert.assertNotNull(res4);
    Assert.assertEquals(1, res4.size());
    Assert.assertEquals(uuids[7], res4.iterator().next().getUuid());

    List<Project> res5 = ps.getSubProjects(uuids[7]);
    Assert.assertNotNull(res5);
    Assert.assertEquals(0, res5.size());

    verify(mocks);
  }

  @Test
  public void testGetParentChain() {
    List<Project> res1 = ps.getParentChain(uuids[1]);
    Assert.assertNotNull(res1);
    Assert.assertEquals(1, res1.size());
    Assert.assertEquals(uuids[1], res1.get(0).getUuid());

    List<Project> res2 = ps.getParentChain(uuids[2]);
    Assert.assertNotNull(res2);
    Assert.assertEquals(2, res2.size());
    Assert.assertEquals(uuids[2], res2.get(0).getUuid());
    Assert.assertEquals(uuids[1], res2.get(1).getUuid());

    List<Project> res3 = ps.getParentChain(uuids[3]);
    Assert.assertNotNull(res3);
    Assert.assertEquals(3, res3.size());
    Assert.assertEquals(uuids[3], res3.get(0).getUuid());
    Assert.assertEquals(uuids[2], res3.get(1).getUuid());
    Assert.assertEquals(uuids[1], res3.get(2).getUuid());

    List<Project> res4 = ps.getParentChain(uuids[6]);
    Assert.assertNotNull(res4);
    Assert.assertEquals(4, res4.size());
    Assert.assertEquals(uuids[6], res4.get(0).getUuid());
    Assert.assertEquals(uuids[3], res4.get(1).getUuid());
    Assert.assertEquals(uuids[2], res4.get(2).getUuid());
    Assert.assertEquals(uuids[1], res4.get(3).getUuid());

    List<Project> res5 = ps.getParentChain(uuids[7]);
    Assert.assertNotNull(res5);
    Assert.assertEquals(5, res5.size());
    Assert.assertEquals(uuids[7], res5.get(0).getUuid());
    Assert.assertEquals(uuids[6], res5.get(1).getUuid());
    Assert.assertEquals(uuids[3], res5.get(2).getUuid());
    Assert.assertEquals(uuids[2], res5.get(3).getUuid());
    Assert.assertEquals(uuids[1], res5.get(4).getUuid());

    List<Project> res6 = ps.getParentChain(uuids[8]);
    Assert.assertNotNull(res6);
    Assert.assertEquals(4, res6.size());
    Assert.assertEquals(uuids[8], res6.get(0).getUuid());
    Assert.assertEquals(uuids[3], res6.get(1).getUuid());
    Assert.assertEquals(uuids[2], res6.get(2).getUuid());
    Assert.assertEquals(uuids[1], res6.get(3).getUuid());

    verify(mocks);
  }

  @Test
  public void testGetNearestParent() {
    Project nearest = ps.getNearestParent(uuids[6], ProjectNature.PROJECT);
    Assert.assertEquals(uuids[3], nearest.getUuid());

    nearest = ps.getNearestParent(uuids[7], ProjectNature.PROJECT);
    Assert.assertEquals(uuids[3], nearest.getUuid());

    nearest = ps.getNearestParent(uuids[8], ProjectNature.PROJECT);
    Assert.assertEquals(uuids[3], nearest.getUuid());

    nearest = ps.getNearestParent(uuids[6], ProjectNature.COMPONENT);
    Assert.assertEquals(uuids[6], nearest.getUuid());

    nearest = ps.getNearestParent(uuids[7], ProjectNature.COMPONENT);
    Assert.assertEquals(uuids[7], nearest.getUuid());

    nearest = ps.getNearestParent(uuids[8], ProjectNature.COMPONENT);
    Assert.assertEquals(uuids[8], nearest.getUuid());

    nearest = ps.getNearestParent(uuids[1], ProjectNature.COMPONENT);
    Assert.assertNull(nearest);

    nearest = ps.getNearestParent(uuids[2], ProjectNature.COMPONENT);
    Assert.assertNull(nearest);

    nearest = ps.getNearestParent(uuids[3], ProjectNature.COMPONENT);
    Assert.assertNull(nearest);

    // top-level projects have no parent
    nearest = ps.getNearestParent(uuids[1], ProjectNature.PROJECT);
    Assert.assertEquals(uuids[1], nearest.getUuid());

    nearest = ps.getNearestParent(uuids[2], ProjectNature.PROJECT);
    Assert.assertEquals(uuids[2], nearest.getUuid());

    nearest = ps.getNearestParent(uuids[3], ProjectNature.PROJECT);
    Assert.assertEquals(uuids[3], nearest.getUuid());
  }


  @Test
  public void getProjectsForTag() {
    List<Project> res1 = ps.getProjectsForTag("tagBoth"); //$NON-NLS-1$
    Assert.assertNotNull(res1);
    Assert.assertEquals(2, res1.size());

    List<Project> res2 = ps.getProjectsForTag("tag2"); //$NON-NLS-1$
    Assert.assertNotNull(res2);
    Assert.assertEquals(1, res2.size());
    Assert.assertEquals(uuids[2], res2.toArray(new Project[1])[0].getUuid());

    List<Project> res3 = ps.getProjectsForTag("tagNonExisting"); //$NON-NLS-1$
    Assert.assertNotNull(res3);
    Assert.assertEquals(0, res3.size());

    // tags exists, but project is deleted
    List<Project> res4 = ps.getProjectsForTag("tag4"); //$NON-NLS-1$
    Assert.assertNotNull(res4);
    Assert.assertEquals(0, res4.size());

    verify(mocks);
  }

  @Test
  public void testGetDeletedProjects() {
    List<Project> res = ps.getDeletedProjects();
    Assert.assertNotNull(res);
    Assert.assertEquals(2, res.size());

    verify(mocks);
  }

  @Test
  public void testGetDeletedProject() {
    Project res1 = ps.getDeletedProject(uuids[4]);
    Assert.assertNotNull(res1);
    Assert.assertEquals(uuids[4], res1.getUuid());

    Project res2 = ps.getDeletedProject(uuids[5]);
    Assert.assertNotNull(res2);
    Assert.assertEquals(uuids[5], res2.getUuid());

    Project res3 = ps.getDeletedProject(uuids[1]);
    Assert.assertNull(res3);

    verify(mocks);
  }

  @Test
  public void testGetAllPeople() {
    Project p = new Project();
    PeopleProjectExt ext = new PeopleProjectExt();
    ext.addMember(m1);
    ext.addMember(m2);
    ext.addMember(l1);
    ext.addLead(l1);
    ext.addLead(l2);
    p.addExtension(ext);

    Set<ProjectMember> res1 = ps.getAllPeople(p);
    Assert.assertEquals(4, res1.size());
    Assert.assertTrue(res1.contains(m1));
    Assert.assertTrue(res1.contains(m2));
    Assert.assertTrue(res1.contains(l1));
    Assert.assertTrue(res1.contains(l2));

  }

  @Test
  public void testGetAllPeopleByRole() {
    Project p = new Project();
    PeopleProjectExt ext = new PeopleProjectExt();
    ext.addMember(m1);
    ext.addMember(m2);
    ext.addMember(l1);
    ext.addLead(l1);
    ext.addLead(l2);
    p.addExtension(ext);

    Map<String, Set<ProjectMember>> res2 = ps.getAllPeopleByRole(p);
    Assert.assertEquals(2, res2.size());

    Set<ProjectMember> res2members = res2.get("projectmember");
    Assert.assertNotNull(res2members);
    Assert.assertTrue(res2members.contains(m1));
    Assert.assertTrue(res2members.contains(m2));
    Assert.assertTrue(res2members.contains(l1));
    Assert.assertFalse(res2members.contains(l2));

    Set<ProjectMember> res2leads = res2.get("projectlead");
    Assert.assertNotNull(res2leads);
    Assert.assertFalse(res2leads.contains(m1));
    Assert.assertFalse(res2leads.contains(m2));
    Assert.assertTrue(res2leads.contains(l1));
    Assert.assertTrue(res2leads.contains(l2));
  }
}

