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

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.eclipse.skalli.api.java.EntityFilter;
import org.eclipse.skalli.api.java.EntityServiceImpl;
import org.eclipse.skalli.api.java.GroupService;
import org.eclipse.skalli.common.Group;
import org.eclipse.skalli.log.Log;
import org.eclipse.skalli.model.ext.Issue;
import org.eclipse.skalli.model.ext.Severity;
import org.eclipse.skalli.model.ext.ValidationException;
import org.osgi.service.component.ComponentContext;

/**
 * Implementation of {@link GroupService} using {@link Group} instances.
 * The service expects to find XML files named
 * <tt>&lt;groupId&gt.xml</tt> in the <tt>$workdir/storage/Group</tt>
 * directory.
 */
public class LocalGroupServiceImpl extends EntityServiceImpl<Group> implements GroupService {

    private static final Logger LOG = Log.getLogger(LocalGroupServiceImpl.class);

    /** Unique identifier of the portal admiminstrators group */
    private static final String ADMIN_GROUP = "administrators"; //$NON-NLS-1$

    protected void activate(ComponentContext context) {
        LOG.info("Local Group Service activated"); //$NON-NLS-1$
    }

    protected void deactivate(ComponentContext context) {
        LOG.info("Local Group Service deactivated"); //$NON-NLS-1$
    }

    @Override
    public Class<Group> getEntityClass() {
        return Group.class;
    }

    @Override
    public boolean isAdministrator(String userId) {
        return isMemberOfGroup(userId, ADMIN_GROUP);
    }

    @Override
    public boolean isMemberOfGroup(final String userId, final String groupId) {
        Group group = getGroup(groupId);
        return group != null ? group.hasGroupMember(userId) : false;
    }

    @Override
    public List<Group> getGroups() {
        return getAll();
    }

    @Override
    protected void validateEntity(Group entity) throws ValidationException {
        SortedSet<Issue> issues = validate(entity, Severity.FATAL);
        if (issues.size() > 0) {
            throw new ValidationException("Group could not be saved due to the following reasons:", issues);
        }
    }

    @Override
    protected SortedSet<Issue> validateEntity(Group entity, Severity minSeverity) {
        return new TreeSet<Issue>();
    }

    @Override
    public Group getGroup(final String groupId) {
        Group group = getPersistenceService().getEntity(Group.class, new EntityFilter<Group>() {
            @Override
            public boolean accept(Class<Group> entityClass, Group entity) {
                return entity.getGroupId().equals(groupId);
            }
        });
        return group;
    }
}
