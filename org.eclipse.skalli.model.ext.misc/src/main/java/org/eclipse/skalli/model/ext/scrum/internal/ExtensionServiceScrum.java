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
package org.eclipse.skalli.model.ext.scrum.internal;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.skalli.common.util.CollectionUtils;
import org.eclipse.skalli.common.util.HostReachableValidator;
import org.eclipse.skalli.common.util.URLValidator;
import org.eclipse.skalli.model.ext.AbstractIndexer;
import org.eclipse.skalli.model.ext.AliasedConverter;
import org.eclipse.skalli.model.ext.ExtensionService;
import org.eclipse.skalli.model.ext.ExtensionServiceBase;
import org.eclipse.skalli.model.ext.PropertyValidator;
import org.eclipse.skalli.model.ext.Severity;
import org.eclipse.skalli.model.ext.scrum.ScrumProjectExt;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtensionServiceScrum
        extends ExtensionServiceBase<ScrumProjectExt>
        implements ExtensionService<ScrumProjectExt>
{

    private static final Logger LOG = LoggerFactory.getLogger(ExtensionServiceScrum.class);

    private static final String CAPTION = "Scrum";
    private static final String DESCRIPTION = "Information related to a SCRUM project like " +
            "SCRUM master, product owner and project backlog.";

    private static final Map<String, String> CAPTIONS = CollectionUtils.addAll(ExtensionServiceBase.CAPTIONS,
            new String[][] {
                    { ScrumProjectExt.PROPERTY_SCRUM_MASTERS, "SCRUM Masters" },
                    { ScrumProjectExt.PROPERTY_PRODUCT_OWNERS, "Product Owners" },
                    { ScrumProjectExt.PROPERTY_BACKLOG_URL, "Backlog" } });

    private static final Map<String, String> DESCRIPTIONS = CollectionUtils.addAll(ExtensionServiceBase.DESCRIPTIONS,
            new String[][] {
                    { ScrumProjectExt.PROPERTY_SCRUM_MASTERS, "The SCRUM masters of this project" },
                    { ScrumProjectExt.PROPERTY_PRODUCT_OWNERS, "The product owners assigned to this project" },
                    { ScrumProjectExt.PROPERTY_BACKLOG_URL, "Browsable link to the project's backlog" } });

    private static final Map<String, String> INPUT_PROMPTS = CollectionUtils.asMap(new String[][] {
            { ScrumProjectExt.PROPERTY_BACKLOG_URL, URL_INPUT_PROMPT } });

    @Override
    public Class<ScrumProjectExt> getExtensionClass() {
        return ScrumProjectExt.class;
    }

    protected void activate(ComponentContext context) {
        LOG.info("activated model extension: " + getShortName()); //$NON-NLS-1$
    }

    protected void deactivate(ComponentContext context) {
        LOG.info("deactivated model extension: " + getShortName()); //$NON-NLS-1$
    }

    @Override
    public String getShortName() {
        return "scrum"; //$NON-NLS-1$
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
        return new ScrumConverter(host);
    }

    @Override
    public String getModelVersion() {
        return ScrumProjectExt.MODEL_VERSION;
    }

    @Override
    public String getNamespace() {
        return ScrumProjectExt.NAMESPACE;
    }

    @Override
    public String getXsdFileName() {
        return "model-scrum.xsd"; //$NON-NLS-1$
    }

    @Override
    public AbstractIndexer<ScrumProjectExt> getIndexer() {
        return new ScrumIndexer();
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
    public String getInputPrompt(String propertyName) {
        return INPUT_PROMPTS.get(propertyName);
    }

    @Override
    public Set<PropertyValidator> getPropertyValidators(String propertyName, String caption) {
        caption = getCaption(propertyName, caption);
        Set<PropertyValidator> validators = new HashSet<PropertyValidator>();
        if (ScrumProjectExt.PROPERTY_BACKLOG_URL.equals(propertyName)) {
            validators.add(new URLValidator(Severity.FATAL, getExtensionClass(), propertyName, caption));
            validators.add(new HostReachableValidator(getExtensionClass(), propertyName));
        }
        return validators;
    }
}
