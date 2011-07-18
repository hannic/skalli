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
package org.eclipse.skalli.model.ext.devinf.internal;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.osgi.service.component.ComponentContext;

import org.eclipse.skalli.common.util.CollectionUtils;
import org.eclipse.skalli.common.util.HostReachableValidator;
import org.eclipse.skalli.common.util.URLValidator;
import org.eclipse.skalli.log.Log;
import org.eclipse.skalli.model.ext.AbstractIndexer;
import org.eclipse.skalli.model.ext.AliasedConverter;
import org.eclipse.skalli.model.ext.ExtensionService;
import org.eclipse.skalli.model.ext.ExtensionServiceBase;
import org.eclipse.skalli.model.ext.PropertyValidator;
import org.eclipse.skalli.model.ext.Severity;
import org.eclipse.skalli.model.ext.devinf.DevInfProjectExt;

public class ExtensionServiceDevInf
        extends ExtensionServiceBase<DevInfProjectExt>
        implements ExtensionService<DevInfProjectExt>
{

    private static final Logger LOG = Log.getLogger(ExtensionServiceDevInf.class);

    private static final String CAPTION = "Development Infrastructure";
    private static final String DESCRIPTION =
            "Information related to the project's development infrastructure like " +
                    "used source repositories, continous integration server and quality metrics.";

    private static final Map<String, String> CAPTIONS = CollectionUtils.addAll(ExtensionServiceBase.CAPTIONS,
            new String[][] {
                    { DevInfProjectExt.PROPERTY_SCM_URL, "Source Code" },
                    { DevInfProjectExt.PROPERTY_SCM_LOCATIONS, "Repositories" },
                    { DevInfProjectExt.PROPERTY_BUGTRACKER_URL, "Bugtracker" },
                    { DevInfProjectExt.PROPERTY_CI_URL, "Build" },
                    { DevInfProjectExt.PROPERTY_METRICS_URL, "Quality" },
                    { DevInfProjectExt.PROPERTY_REVIEW_URL, "Code Review" },
                    { DevInfProjectExt.PROPERTY_JAVADOCS_URL, "Javadoc" } });

    private static final Map<String, String> DESCRIPTIONS = CollectionUtils
            .addAll(ExtensionServiceBase.DESCRIPTIONS,
                    new String[][] {
                            { DevInfProjectExt.PROPERTY_SCM_URL,
                                    "Browsable link to the project's source code repository" },
                            { DevInfProjectExt.PROPERTY_SCM_LOCATIONS, "Source code repositories used by this project" },
                            { DevInfProjectExt.PROPERTY_BUGTRACKER_URL,
                                    "Browsable link to the project's issue management system" },
                            { DevInfProjectExt.PROPERTY_CI_URL,
                                    "Browsable link to the project's continous integration and build system" },
                            { DevInfProjectExt.PROPERTY_METRICS_URL,
                                    "Browsable link to the project's quality metrics system" },
                            { DevInfProjectExt.PROPERTY_REVIEW_URL,
                                    "Browsable link to the project's code review system" },
                            { DevInfProjectExt.PROPERTY_JAVADOCS_URL, "Browsable link to the Javadoc of this project" } });

    @Override
    public Class<DevInfProjectExt> getExtensionClass() {
        return DevInfProjectExt.class;
    }

    protected void activate(ComponentContext context) {
        LOG.info("activated model extension: " + getShortName()); //$NON-NLS-1$
    }

    protected void deactivate(ComponentContext context) {
        LOG.info("deactivated model extension: " + getShortName()); //$NON-NLS-1$
    }

    @Override
    public String getShortName() {
        return "devInf"; //$NON-NLS-1$
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
        return new DevInfConverter(host);
    }

    @Override
    public String getModelVersion() {
        return DevInfProjectExt.MODEL_VERSION;
    }

    @Override
    public String getNamespace() {
        return DevInfProjectExt.NAMESPACE;
    }

    @Override
    public String getXsdFileName() {
        return "model-devinf.xsd"; //$NON-NLS-1$
    }

    @Override
    public AbstractIndexer<DevInfProjectExt> getIndexer() {
        return new DevInfIndexer();
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
        if (DevInfProjectExt.PROPERTY_SCM_URL.equals(propertyName)) {
            validators.add(new URLValidator(Severity.FATAL, getExtensionClass(), propertyName, caption));
            validators.add(new HostReachableValidator(getExtensionClass(), propertyName));
        }
        else if (DevInfProjectExt.PROPERTY_SCM_LOCATIONS.equals(propertyName)) {
            validators.add(new SCMValidator(caption));
        }
        else if (DevInfProjectExt.PROPERTY_BUGTRACKER_URL.equals(propertyName)) {
            validators.add(new URLValidator(Severity.FATAL, getExtensionClass(), propertyName, caption));
            validators.add(new HostReachableValidator(getExtensionClass(), propertyName));
        }
        else if (DevInfProjectExt.PROPERTY_CI_URL.equals(propertyName)) {
            validators.add(new URLValidator(Severity.FATAL, getExtensionClass(), propertyName, caption));
            validators.add(new HostReachableValidator(getExtensionClass(), propertyName));
        }
        else if (DevInfProjectExt.PROPERTY_METRICS_URL.equals(propertyName)) {
            validators.add(new URLValidator(Severity.FATAL, getExtensionClass(), propertyName, caption));
            validators.add(new HostReachableValidator(getExtensionClass(), propertyName));
        }
        else if (DevInfProjectExt.PROPERTY_REVIEW_URL.equals(propertyName)) {
            validators.add(new URLValidator(Severity.FATAL, getExtensionClass(), propertyName, caption));
            validators.add(new HostReachableValidator(getExtensionClass(), propertyName));
        }
        else if (DevInfProjectExt.PROPERTY_JAVADOCS_URL.equals(propertyName)) {
            validators.add(new URLValidator(Severity.FATAL, getExtensionClass(), propertyName, caption));
            validators.add(new HostReachableValidator(getExtensionClass(), propertyName));
        }
        return validators;
    }
}
