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
package org.eclipse.skalli.model.ext.people.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.osgi.service.component.ComponentContext;

import org.eclipse.skalli.log.Log;
import org.eclipse.skalli.model.core.PeopleProvider;
import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.core.ProjectMember;
import org.eclipse.skalli.model.ext.people.PeopleProjectExt;

public final class CorePeopleProvider implements PeopleProvider {
    private static final Logger LOG = Log.getLogger(CorePeopleProvider.class);

    private static final String ROLE_LEAD = "projectlead"; //$NON-NLS-1$
    private static final String ROLE_MEMBER = "projectmember"; //$NON-NLS-1$

    protected void activate(ComponentContext context) {
        LOG.info(this.getClass().getSimpleName() + " component activated"); //$NON-NLS-1$
    }

    protected void deactivate(ComponentContext context) {
        LOG.info(this.getClass().getSimpleName() + " component deactivated"); //$NON-NLS-1$
    }

    @Override
    public Map<String, Set<ProjectMember>> getPeople(Project project) {
        Map<String, Set<ProjectMember>> ret = new HashMap<String, Set<ProjectMember>>();
        PeopleProjectExt ext = project.getExtension(PeopleProjectExt.class);
        if (ext != null) {
            ret.put(ROLE_LEAD, ext.getLeads());
            ret.put(ROLE_MEMBER, ext.getMembers());
        }
        return ret;
    }

    @Override
    public void addPerson(Project project, String role, ProjectMember person) {
        if (project.isInherited(PeopleProjectExt.class)) {
            throw new IllegalArgumentException("Cannot add person to inherited people extension");
        }
        PeopleProjectExt ext = project.getExtension(PeopleProjectExt.class);
        if (ext == null) {
            ext = new PeopleProjectExt();
            project.addExtension(ext);
        }
        if (StringUtils.equalsIgnoreCase(role, ROLE_LEAD)) {
            ext.addLead(person);
        } else if (StringUtils.equalsIgnoreCase(role, ROLE_MEMBER)) {
            ext.addMember(person);
        }
    }
}
