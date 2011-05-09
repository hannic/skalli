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
package org.eclipse.skalli.api.rest.internal;

import java.util.HashSet;
import java.util.Set;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import org.eclipse.skalli.api.rest.config.ConfigSection;
import org.eclipse.skalli.api.rest.internal.admin.ProjectBackupResource;
import org.eclipse.skalli.api.rest.internal.admin.StatisticsResource;
import org.eclipse.skalli.api.rest.internal.admin.StatusResource;
import org.eclipse.skalli.api.rest.internal.resources.IssuesResource;
import org.eclipse.skalli.api.rest.internal.resources.ProjectResource;
import org.eclipse.skalli.api.rest.internal.resources.ProjectsResource;
import org.eclipse.skalli.api.rest.internal.resources.UserResource;

public class RestApplication extends Application {

    private final static Set<ConfigSection> configSections = new HashSet<ConfigSection>();

    @Override
    public synchronized Restlet createInboundRoot() {
        Router router = new Router(getContext());

        for (ConfigSection configSection : configSections) {
            router.attach("/config/" + configSection.getName(), configSection.getServerResource()); //$NON-NLS-1$
        }

        router.attach("/admin/status", StatusResource.class); //$NON-NLS-1$
        router.attach("/admin/statistics", StatisticsResource.class); //$NON-NLS-1$
        router.attach("/admin/backup", ProjectBackupResource.class); //$NON-NLS-1$

        router.attach("/projects", ProjectsResource.class); //$NON-NLS-1$
        router.attach("/projects/{id}", ProjectResource.class); //$NON-NLS-1$
        router.attach("/projects/{id}/issues", IssuesResource.class); //$NON-NLS-1$

        router.attach("/user/{id}", UserResource.class); //$NON-NLS-1$

        return router;
    }

    protected void bindConfigSection(ConfigSection configSection) {
        configSections.add(configSection);
    }

    protected void unbindConfigSection(ConfigSection configSection) {
        configSections.remove(configSection);
    }

}
