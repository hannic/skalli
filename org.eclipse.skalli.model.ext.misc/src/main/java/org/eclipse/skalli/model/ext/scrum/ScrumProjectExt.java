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
package org.eclipse.skalli.model.ext.scrum;

import java.util.Set;
import java.util.TreeSet;

import org.eclipse.skalli.model.core.ProjectMember;
import org.eclipse.skalli.model.ext.ExtensionEntityBase;
import org.eclipse.skalli.model.ext.PropertyName;

public class ScrumProjectExt extends ExtensionEntityBase {

    public static final String MODEL_VERSION = "1.0"; //$NON-NLS-1$
    public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/Model/Extension-Scrum"; //$NON-NLS-1$

    @PropertyName(position = 0)
    public static final String PROPERTY_SCRUM_MASTERS = "scrumMasters"; //$NON-NLS-1$

    @PropertyName(position = 1)
    public static final String PROPERTY_PRODUCT_OWNERS = "productOwners"; //$NON-NLS-1$

    @PropertyName(position = 2)
    public static final String PROPERTY_BACKLOG_URL = "backlogUrl"; //$NON-NLS-1$

    private TreeSet<ProjectMember> scrumMasters = new TreeSet<ProjectMember>();
    private TreeSet<ProjectMember> productOwners = new TreeSet<ProjectMember>();
    private String backlogUrl = ""; //$NON-NLS-1$

    public synchronized Set<ProjectMember> getScrumMasters() {
        if (scrumMasters == null) {
            scrumMasters = new TreeSet<ProjectMember>();
        }
        return scrumMasters;
    }

    public void addScrumMaster(ProjectMember person) {
        if (person != null) {
            getScrumMasters().add(person);
        }
    }

    public void removeScrumMaster(ProjectMember person) {
        if (person != null) {
            getScrumMasters().remove(person);
        }
    }

    public boolean hasScrumMaster(ProjectMember person) {
        return getScrumMasters().contains(person);
    }

    public synchronized Set<ProjectMember> getProductOwners() {
        if (productOwners == null) {
            productOwners = new TreeSet<ProjectMember>();
        }
        return productOwners;
    }

    public void addProductOwner(ProjectMember person) {
        if (person != null) {
            getProductOwners().add(person);
        }
    }

    public void removeProductOwner(ProjectMember person) {
        if (person != null) {
            getProductOwners().remove(person);
        }
    }

    public boolean hasProductOwner(ProjectMember person) {
        return getProductOwners().contains(person);
    }

    public String getBacklogUrl() {
        return backlogUrl;
    }

    public void setBacklogUrl(String backlogUrl) {
        this.backlogUrl = backlogUrl;
    }

}
