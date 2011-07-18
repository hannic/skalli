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
package org.eclipse.skalli.model.ext.info.internal;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.osgi.service.component.ComponentContext;

import org.eclipse.skalli.common.util.CollectionUtils;
import org.eclipse.skalli.common.util.EmailValidator;
import org.eclipse.skalli.common.util.HostReachableValidator;
import org.eclipse.skalli.common.util.URLValidator;
import org.eclipse.skalli.log.Log;
import org.eclipse.skalli.model.ext.AbstractIndexer;
import org.eclipse.skalli.model.ext.AliasedConverter;
import org.eclipse.skalli.model.ext.ExtensionService;
import org.eclipse.skalli.model.ext.ExtensionServiceBase;
import org.eclipse.skalli.model.ext.PropertyValidator;
import org.eclipse.skalli.model.ext.Severity;
import org.eclipse.skalli.model.ext.info.InfoProjectExt;

public class ExtensionServiceInfo
        extends ExtensionServiceBase<InfoProjectExt>
        implements ExtensionService<InfoProjectExt>
{

    private static final Logger LOG = Log.getLogger(ExtensionServiceInfo.class);

    private static final String CAPTION = "Info";
    private static final String DESCRIPTION = "Additional information related to the project";

    private static final Map<String, String> CAPTIONS = CollectionUtils.addAll(ExtensionServiceBase.CAPTIONS,
            new String[][] {
                    { InfoProjectExt.PROPERTY_MAILING_LIST, "Mailing Lists" },
                    { InfoProjectExt.PROPERTY_PAGE_URL, "Project Homepage" } });

    private static final Map<String, String> DESCRIPTIONS = CollectionUtils.addAll(ExtensionServiceBase.DESCRIPTIONS,
            new String[][] {
                    { InfoProjectExt.PROPERTY_MAILING_LIST, "Mailing lists provided by this project" },
                    { InfoProjectExt.PROPERTY_PAGE_URL, "Browsable link to the project's homepage" } });

    @Override
    public Class<InfoProjectExt> getExtensionClass() {
        return InfoProjectExt.class;
    }

    protected void activate(ComponentContext context) {
        LOG.info("activated model extension: " + getShortName()); //$NON-NLS-1$
    }

    protected void deactivate(ComponentContext context) {
        LOG.info("deactivated model extension: " + getShortName()); //$NON-NLS-1$
    }

    @Override
    public String getShortName() {
        return "info"; //$NON-NLS-1$
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
        return new InfoConverter(host);
    }

    @Override
    public String getModelVersion() {
        return InfoProjectExt.MODEL_VERSION;
    }

    @Override
    public String getNamespace() {
        return InfoProjectExt.NAMESPACE;
    }

    @Override
    public String getXsdFileName() {
        return "model-info.xsd";
    }

    @Override
    public AbstractIndexer<InfoProjectExt> getIndexer() {
        return new InfoIndexer();
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
    public Set<PropertyValidator> getPropertyValidators(String propertyName, String caption) {
        caption = getCaption(propertyName, caption);
        Set<PropertyValidator> validators = new HashSet<PropertyValidator>();
        if (InfoProjectExt.PROPERTY_MAILING_LIST.equals(propertyName)) {
            validators.add(new EmailValidator(Severity.FATAL, getExtensionClass(), propertyName,
                    MessageFormat.format("{0} must be a valid e-mail address", caption),
                    null));
        }
        else if (InfoProjectExt.PROPERTY_PAGE_URL.equals(propertyName)) {
            validators.add(new URLValidator(Severity.FATAL, getExtensionClass(), propertyName, caption));
            validators.add(new HostReachableValidator(getExtensionClass(), propertyName));
        }
        return validators;
    }
}
