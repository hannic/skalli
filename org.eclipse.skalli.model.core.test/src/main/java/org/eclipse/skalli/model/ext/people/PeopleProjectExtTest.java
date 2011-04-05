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
package org.eclipse.skalli.model.ext.people;

import java.util.Map;
import java.util.TreeSet;

import org.junit.Test;

import org.eclipse.skalli.model.core.ProjectMember;
import org.eclipse.skalli.testutil.PropertyHelper;
import org.eclipse.skalli.testutil.PropertyHelperUtils;

public class PeopleProjectExtTest {
  @Test
  public void testPropertyDefinitions() throws Exception {
    Map<String,Object> values = PropertyHelperUtils.getValues();

    TreeSet<ProjectMember> members = new TreeSet<ProjectMember>();
    ProjectMember HOMER = new ProjectMember("homer");
    ProjectMember MARGE = new ProjectMember("marge");
    members.add(HOMER);
    members.add(MARGE);
    members.add(new ProjectMember("maggy"));
    members.add(new ProjectMember("bart"));
    members.add(new ProjectMember("lisa"));
    values.put(PeopleProjectExt.PROPERTY_MEMBERS, members);
    TreeSet<ProjectMember> leads = new TreeSet<ProjectMember>();
    leads.add(HOMER);
    leads.add(MARGE);
    values.put(PeopleProjectExt.PROPERTY_LEADS, leads);


    Map<Class<?>,String[]> requiredProperties = PropertyHelperUtils.getRequiredProperties();

    PropertyHelper.checkPropertyDefinitions(PeopleProjectExt.class, requiredProperties, values);
  }

}

