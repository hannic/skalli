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
package org.eclipse.skalli.model.core;

import java.util.Collection;
import java.util.Set;

public abstract class ProjectTemplateBase implements ProjectTemplate {

    //TODO get rid of this hard-coded list; make ProjectTemplates persisted entities?
    protected static final String PROJECT_CLASSNAME = "org.eclipse.skalli.model.core.Project"; //$NON-NLS-1$
    protected static final String PEOPLE_EXTENSION_CLASSNAME = "org.eclipse.skalli.model.ext.people.PeopleProjectExt"; //$NON-NLS-1$
    protected static final String NGP_EXTENSION_CLASSNAME = "org.eclipse.skalli.model.ext.sap.NGPProjectExt"; //$NON-NLS-1$
    protected static final String INFO_EXTENSION_CLASSNAME = "org.eclipse.skalli.model.ext.info.InfoProjectExt"; //$NON-NLS-1$
    protected static final String LINKS_EXTENSION_CLASSNAME = "org.eclipse.skalli.model.ext.linkgroups.LinkGroupsProjectExt"; //$NON-NLS-1$
    protected static final String DEVINF_EXTENSION_CLASSNAME = "org.eclipse.skalli.model.ext.devinf.DevInfProjectExt"; //$NON-NLS-1$
    protected static final String MAVEN_EXTENSION_CLASSNAME = "org.eclipse.skalli.model.ext.maven.MavenProjectExt"; //$NON-NLS-1$
    protected static final String SAP_EXTENSION_CLASSNAME = "org.eclipse.skalli.model.ext.sap.SAPProjectExt"; //$NON-NLS-1$
    protected static final String SCRUM_EXTENSION_CLASSNAME = "org.eclipse.skalli.model.ext.scrum.ScrumProjectExt"; //$NON-NLS-1$

    @Override
    public Set<String> getIncludedExtensions() {
        return null; // include all registered extensions by default
    }

    @Override
    public Set<String> getExcludedExtensions() {
        return null; // do not suppress any extensions
    }

    /**
     * Checks if a project assigned to this template can have subprojects
     * assigned to the given template. This method checks only that
     * the project natures of the templates are compatible, i.e. it returns
     * <code>true</code>, if this template is a {@link ProjectNature#PROJECT}
     * template or if both templates are {@link ProjectNature#COMPONENT}
     * templates, and <code>false</code> otherwise.
     */
    @Override
    public boolean isAllowedSubprojectTemplate(ProjectTemplate projectTemplate) {
        return getProjectNature() == ProjectNature.COMPONENT ? projectTemplate.getProjectNature() == ProjectNature.COMPONENT
                : true;
    }

    @Override
    public boolean isEnabled(String extensionClassName) {
        return false;
    }

    @Override
    public boolean isVisible(String extensionClassName) {
        return false;
    }

    @Override
    public Collection<?> getAllowedValues(String extensionClassName, Object propertyId) {
        return null;
    }

    @Override
    public String getCaption(String extensionClassName, Object propertyId) {
        return null;
    }

    @Override
    public Object getDefaultValue(String extensionClassName, Object propertyId) {
        return null;
    }

    @Override
    public Collection<?> getDefaultValues(String extensionClassName, Object propertyId) {
        return null;
    }

    @Override
    public String getDescription(String extensionClassName, Object propertyId) {
        return null;
    }

    @Override
    public String getInputPrompt(String extensionClassName, Object propertyId) {
        return null;
    }

    @Override
    public boolean isEnabled(String extensionClassName, Object propertyId, boolean isAdmin) {
        return true;
    }

    @Override
    public boolean isReadOnly(String extensionClassName, Object propertyId, boolean isAdmin) {
        return false;
    }

    @Override
    public boolean isVisible(String extensionClassName, Object propertyId, boolean isAdmin) {
        return true;
    }

    @Override
    public int getMaxSize(String extensionClassName, Object propertyId) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isNewItemsAllowed(String extensionClassName, Object propertyId) {
        return false;
    }

    @Override
    public float getRank(String extensionClassName) {
        return -1.0f;
    }

}
