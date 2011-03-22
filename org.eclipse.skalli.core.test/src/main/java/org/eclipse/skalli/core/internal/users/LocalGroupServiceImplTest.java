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

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Constants;

import org.eclipse.skalli.api.java.GroupService;
import org.eclipse.skalli.common.Group;
import org.eclipse.skalli.common.Services;
import org.eclipse.skalli.testutil.BundleManager;

@SuppressWarnings("nls")
public class LocalGroupServiceImplTest {

  private File tmpDir;

  private GroupService groupService;
  private List<Group> groups;

  private static final String FILTER =
    "(&(" + Constants.OBJECTCLASS + "=" + GroupService.class.getName() + ")" + "(groupService.type=local))";

  @Before
  public void setup() throws Exception {
    new BundleManager(this.getClass()).startBundles();
    groupService = Services.getService(GroupService.class, FILTER);
    if (groupService == null) {
      Assert.fail("local group service not found.");
    }
    groups = groupService.getGroups();
    Assert.assertEquals(2, groups.size());
  }

  @After
  public void tearDown() throws Exception {
    if (tmpDir != null) {
      FileUtils.forceDelete(tmpDir);
    }
  }

  @Test
  public void testIsAdministrator() {
    Assert.assertTrue(groupService.isAdministrator("lc"));
    Assert.assertFalse(groupService.isAdministrator("gh"));
    Assert.assertFalse(groupService.isAdministrator("unknown"));
  }

  @Test
  public void testIsMemberOfGroup() {
    Assert.assertTrue(groupService.isMemberOfGroup("gh", "doctors"));
    Assert.assertFalse(groupService.isMemberOfGroup("lc", "doctors"));
    Assert.assertFalse(groupService.isMemberOfGroup("unknown", "doctors"));
  }
}

