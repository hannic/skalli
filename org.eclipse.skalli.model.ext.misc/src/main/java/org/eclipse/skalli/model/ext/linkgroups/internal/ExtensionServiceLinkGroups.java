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
package org.eclipse.skalli.model.ext.linkgroups.internal;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.osgi.service.component.ComponentContext;

import org.eclipse.skalli.log.Log;
import org.eclipse.skalli.model.ext.AbstractIndexer;
import org.eclipse.skalli.model.ext.AliasedConverter;
import org.eclipse.skalli.model.ext.ExtensionService;
import org.eclipse.skalli.model.ext.ExtensionServiceBase;
import org.eclipse.skalli.model.ext.PropertyValidator;
import org.eclipse.skalli.model.ext.linkgroups.LinkGroupValidator;
import org.eclipse.skalli.model.ext.linkgroups.LinkGroupsProjectExt;

public class ExtensionServiceLinkGroups
        extends ExtensionServiceBase<LinkGroupsProjectExt>
        implements ExtensionService<LinkGroupsProjectExt>
{

    private static final Logger LOG = Log.getLogger(ExtensionServiceLinkGroups.class);

    private static final String CAPTION = "Additional Links";
    private static final String DESCRIPTION = "Information related to the project that is maintained elsewhere and linkable.";

    @Override
    public Class<LinkGroupsProjectExt> getExtensionClass() {
        return LinkGroupsProjectExt.class;
    }

    protected void activate(ComponentContext context) {
        LOG.info("activated model extension: " + getShortName()); //$NON-NLS-1$
    }

    protected void deactivate(ComponentContext context) {
        LOG.info("deactivated model extension: " + getShortName()); //$NON-NLS-1$
    }

    @Override
    public String getShortName() {
        return "linkGroups"; //$NON-NLS-1$
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
        return new LinkGroupsConverter(host);
    }

    @Override
    public String getModelVersion() {
        return LinkGroupsProjectExt.MODEL_VERSION;
    }

    @Override
    public String getNamespace() {
        return LinkGroupsProjectExt.NAMESPACE;
    }

    @Override
    public String getXsdFileName() {
        return "linksgroups.xsd"; //$NON-NLS-1$
    }

    @Override
    public AbstractIndexer<LinkGroupsProjectExt> getIndexer() {
        return new LinkGroupsIndexer();
    }

    @Override
    public Set<PropertyValidator> getPropertyValidators(String propertyName, String caption) {
        caption = getCaption(propertyName, caption);
        Set<PropertyValidator> validators = new HashSet<PropertyValidator>();
        if (LinkGroupsProjectExt.PROPERTY_LINKGROUPS.equals(propertyName)) {
            validators.add(new LinkGroupValidator(getExtensionClass(), propertyName));
        }
        return validators;
    }
}
