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
package org.eclipse.skalli.model.core.internal;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.skalli.common.Services;
import org.eclipse.skalli.common.User;
import org.eclipse.skalli.common.UserService;
import org.eclipse.skalli.common.UserServiceUtil;
import org.eclipse.skalli.model.core.PeopleProvider;
import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.core.ProjectMember;
import org.eclipse.skalli.model.ext.AbstractIndexer;

public class ProjectIndexer extends AbstractIndexer<Project> {

    private static final String MEMBERS_DISPLAY_NAME = "membersDisplayName"; //$NON-NLS-1$
    private static final String ALL_MEMBERS = "allMembers"; //$NON-NLS-1$

    @Override
    protected void indexFields(Project project) {
        UserService userService = UserServiceUtil.getUserService();
        Set<ProjectMember> allMembers = getAllMembers(project);

        addField(Project.PROPERTY_UUID, project.getUuid().toString(), true, true);
        addField(Project.PROPERTY_PROJECTID, project.getProjectId(), true, true);
        addField(Project.PROPERTY_NAME, project.getName(), true, true);
        addField(Project.PROPERTY_DESCRIPTION, project.getDescription(), true, true);
        addField(Project.PROPERTY_TAGS, project.getTags(), true, true);
        addField(ALL_MEMBERS, allMembers, true, true);
        if (project.getParentProject() != null) {
            addField(Project.PROPERTY_PARENT_PROJECT, project.getParentProject().toString(), true, true);
        }
        addField(Project.PROPERTY_TEMPLATEID, project.getProjectTemplateId(), true, true);

        for (ProjectMember member : allMembers) {
            User user = userService.getUserById(member.getUserID());
            if (user != null) {
                addField(MEMBERS_DISPLAY_NAME, user.getDisplayName(), false, true);
            }
        }
    }

    private Set<ProjectMember> getAllMembers(Project project) {
        Set<ProjectMember> ret = new TreeSet<ProjectMember>();
        for (PeopleProvider pp : Services.getServices(PeopleProvider.class)) {
            for (Set<ProjectMember> people : pp.getPeople(project).values()) {
                ret.addAll(people);
            }
        }
        return ret;
    }

    @Override
    public Set<String> getDefaultSearchFields() {
        Set<String> ret = new HashSet<String>();
        ret.add(Project.PROPERTY_PROJECTID);
        ret.add(Project.PROPERTY_NAME);
        ret.add(Project.PROPERTY_DESCRIPTION);
        ret.add(ALL_MEMBERS);
        ret.add(Project.PROPERTY_TAGS);
        ret.add(MEMBERS_DISPLAY_NAME);
        return ret;
    }

}
