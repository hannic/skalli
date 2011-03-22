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

import java.util.Set;
import java.util.TreeSet;

import org.eclipse.skalli.model.core.ProjectMember;
import org.eclipse.skalli.model.ext.ExtensionEntityBase;
import org.eclipse.skalli.model.ext.PropertyName;

public class PeopleProjectExt extends ExtensionEntityBase {

  public static final String MODEL_VERSION = "1.0"; //$NON-NLS-1$
  public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/Model/Extension-People"; //$NON-NLS-1$

  @PropertyName(position=0)
  public static final String PROPERTY_LEADS = "leads"; //$NON-NLS-1$

  @PropertyName(position=1)
  public static final String PROPERTY_MEMBERS = "members"; //$NON-NLS-1$

  private TreeSet<ProjectMember> leads = new TreeSet<ProjectMember>();
  private TreeSet<ProjectMember> members = new TreeSet<ProjectMember>();


  public synchronized Set<ProjectMember> getMembers() {
    if (members == null) {
      members = new TreeSet<ProjectMember>();
    }
    return members;
  }
  public void addMember(ProjectMember member) {
    if (member != null) {
      getMembers().add(member);
    }
  }
  public void removeMember(ProjectMember member) {
    if (member != null) {
      getMembers().remove(member);
    }
  }
  public boolean hasMember(ProjectMember member) {
    return getMembers().contains(member);
  }

  public synchronized Set<ProjectMember> getLeads() {
    if (leads == null) {
      leads = new TreeSet<ProjectMember>();
    }
    return leads;
  }
  public void addLead(ProjectMember lead) {
    if (lead != null) {
      getLeads().add(lead);
    }
  }
  public void removeLead(ProjectMember lead) {
    if (lead != null) {
      getLeads().remove(lead);
    }
  }
  public boolean hasLead(ProjectMember lead) {
    return getLeads().contains(lead);
  }
}

