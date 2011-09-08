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
package org.eclipse.skalli.core.internal.issues;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.eclipse.skalli.api.java.EntityServiceImpl;
import org.eclipse.skalli.api.java.IssuesService;
import org.eclipse.skalli.model.ext.Issue;
import org.eclipse.skalli.model.ext.Issues;
import org.eclipse.skalli.model.ext.Severity;
import org.eclipse.skalli.model.ext.ValidationException;

public class IssuesServiceImpl extends EntityServiceImpl<Issues> implements IssuesService {

    @Override
    public Class<Issues> getEntityClass() {
        return Issues.class;
    }

    @Override
    protected void validateEntity(Issues entity) throws ValidationException {
        SortedSet<Issue> issues = validate(entity, Severity.FATAL);
        if (issues.size() > 0) {
            throw new ValidationException("Issues could not be saved due to the following reasons:", issues);
        }
    }

    @Override
    protected SortedSet<Issue> validateEntity(Issues entity, Severity minSeverity) {
        TreeSet<Issue> issues = new TreeSet<Issue>();
        UUID entityId = entity.getUuid();
        if (entityId == null) {
            issues.add(new Issue(Severity.FATAL, IssuesService.class, entity.getUuid(),
                    "Issues instance is not associated with an entity"));
        }
        for (Issue issue : entity.getIssues()) {
            // don't accept issues for other entities
            if (!issue.getEntityId().equals(entityId)) {
                issues.add(new Issue(Severity.FATAL, IssuesService.class, entityId,
                        MessageFormat.format("Invalid issue detected (requested entity={0} but found entity={1})",
                                issue.getEntityId(), entity.getUuid())));
            }
            // if the issue has no timestamp, set the current system time
            long timestamp = issue.getTimestamp();
            if (timestamp <= 0) {
                issue.setTimestamp(System.currentTimeMillis());
            }
        }
        return issues;
    }

    @Override
    public void persist(UUID entityId, Collection<Issue> issues, String userId) throws ValidationException {
        Issues entity = new Issues(entityId, issues);
        persist(entity, userId);
    }
}
