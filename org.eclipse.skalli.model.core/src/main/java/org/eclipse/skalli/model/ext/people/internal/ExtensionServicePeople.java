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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.skalli.common.User;
import org.eclipse.skalli.common.util.CollectionUtils;
import org.eclipse.skalli.log.Log;
import org.eclipse.skalli.model.core.ProjectMember;
import org.eclipse.skalli.model.ext.AbstractIndexer;
import org.eclipse.skalli.model.ext.AliasedConverter;
import org.eclipse.skalli.model.ext.ExtensibleEntityBase;
import org.eclipse.skalli.model.ext.ExtensionService;
import org.eclipse.skalli.model.ext.ExtensionServiceBase;
import org.eclipse.skalli.model.ext.people.PeopleProjectExt;
import org.osgi.service.component.ComponentContext;

public class ExtensionServicePeople
        extends ExtensionServiceBase<PeopleProjectExt>
        implements ExtensionService<PeopleProjectExt>
{
    private static final Logger LOG = Log.getLogger(ExtensionServicePeople.class);

    private static final String CAPTION = "Project Members";
    private static final String DESCRIPTION =
            "Information related to project members and leads.";

    private static final Map<String, String> CAPTIONS = CollectionUtils.addAll(ExtensionServiceBase.CAPTIONS,
            new String[][] {
                    { PeopleProjectExt.PROPERTY_LEADS, "Project Leads" },
                    { PeopleProjectExt.PROPERTY_MEMBERS, "Committers" } });

    private static final Map<String, String> DESCRIPTIONS = CollectionUtils.addAll(ExtensionServiceBase.DESCRIPTIONS,
            new String[][] {
                    { PeopleProjectExt.PROPERTY_LEADS, "The leads of this project" },
                    { PeopleProjectExt.PROPERTY_MEMBERS, "The comitters of this project" } });

    @Override
    public Class<PeopleProjectExt> getExtensionClass() {
        return PeopleProjectExt.class;
    }

    protected void activate(ComponentContext context) {
        LOG.info("activated model extension: " + getShortName()); //$NON-NLS-1$
    }

    protected void deactivate(ComponentContext context) {
        LOG.info("deactivated model extension: " + getShortName()); //$NON-NLS-1$
    }

    @Override
    public String getModelVersion() {
        return PeopleProjectExt.MODEL_VERSION;
    }

    @Override
    public String getNamespace() {
        return PeopleProjectExt.NAMESPACE;
    }

    @Override
    public String getXsdFileName() {
        return "model-people.xsd"; //$NON-NLS-1$
    }

    @Override
    public String getShortName() {
        return "people"; //$NON-NLS-1$
    }

    @Override
    public String getCaption() {
        return CAPTION;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public AliasedConverter getConverter(String host) {
        return new PeopleConverter(host);
    }

    @Override
    public AbstractIndexer<PeopleProjectExt> getIndexer() {
        return new PeopleIndexer();
    }

    @Override
    public String getCaption(String propertyName) {
        return CAPTIONS.get(propertyName);
    }

    @Override
    public String getDescription(String propertyName) {
        return DESCRIPTIONS.get(propertyName);
    }

    @Override
    public List<String> getConfirmationWarnings(ExtensibleEntityBase entity, ExtensibleEntityBase modifiedEntity, User modifier) {
        List<String> warnings = new ArrayList<String>();
        PeopleProjectExt extension = entity.getExtension(PeopleProjectExt.class);
        PeopleProjectExt modifiedExtension = modifiedEntity.getExtension(PeopleProjectExt.class);
        if (extension != null && modifiedExtension != null) {
            ProjectMember person = new ProjectMember(modifier.getUserId());
            if (extension.hasLead(person) && !modifiedExtension.hasLead(person)) {
                warnings.add("You are trying to remove yourself from the list of <i>project leads</i>.");
            }
        }
        return warnings;
    }
}
