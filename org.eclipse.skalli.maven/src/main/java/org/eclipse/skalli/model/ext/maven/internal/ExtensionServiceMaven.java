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
package org.eclipse.skalli.model.ext.maven.internal;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.osgi.service.component.ComponentContext;

import org.eclipse.skalli.common.util.CollectionUtils;
import org.eclipse.skalli.common.util.HostReachableValidator;
import org.eclipse.skalli.common.util.URLValidator;
import org.eclipse.skalli.log.Log;
import org.eclipse.skalli.model.ext.AliasedConverter;
import org.eclipse.skalli.model.ext.DataMigration;
import org.eclipse.skalli.model.ext.ExtensionService;
import org.eclipse.skalli.model.ext.ExtensionServiceBase;
import org.eclipse.skalli.model.ext.PropertyValidator;
import org.eclipse.skalli.model.ext.Severity;
import org.eclipse.skalli.model.ext.maven.MavenProjectExt;

public class ExtensionServiceMaven
        extends ExtensionServiceBase<MavenProjectExt>
        implements ExtensionService<MavenProjectExt>
{

    private static final Logger LOG = Log.getLogger(ExtensionServiceMaven.class);

    private static final String CAPTION = "Maven";
    private static final String DESCRIPTION = "Information related to a Maven project like the group id.";

    private static final Map<String, String> CAPTIONS = CollectionUtils.addAll(ExtensionServiceBase.CAPTIONS,
            new String[][] {
                    { MavenProjectExt.PROPERTY_GROUPID, "GroupId" },
                    { MavenProjectExt.PROPERTY_REACTOR_POM, "Reactor POM Path" },
                    { MavenProjectExt.PROPERTY_SITE_URL, "Maven Site" } });

    private static final Map<String, String> DESCRIPTIONS = CollectionUtils
            .addAll(ExtensionServiceBase.DESCRIPTIONS,
                    new String[][] {
                            { MavenProjectExt.PROPERTY_GROUPID, "Group Identifier of this project" },
                            { MavenProjectExt.PROPERTY_REACTOR_POM,
                                    "Path of the project's reactor 'pom.xml' relative to the project's root source code location" },
                            { MavenProjectExt.PROPERTY_SITE_URL, "Browsable link to the Maven Site of this project" } });

    @Override
    public Class<MavenProjectExt> getExtensionClass() {
        return MavenProjectExt.class;
    }

    protected void activate(ComponentContext context) {
        LOG.info("activated model extension: " + getShortName()); //$NON-NLS-1$
    }

    protected void deactivate(ComponentContext context) {
        LOG.info("deactivated model extension: " + getShortName()); //$NON-NLS-1$
    }

    @Override
    public Set<DataMigration> getMigrations() {
        Set<DataMigration> migrations = new HashSet<DataMigration>();
        migrations.add(new DataMigration1());
        migrations.add(new DataMigration2());
        return migrations;
    }

    @Override
    public String getShortName() {
        return "maven"; //$NON-NLS-1$
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
        return new MavenConverter(host);
    }

    @Override
    public String getModelVersion() {
        return MavenProjectExt.MODEL_VERSION;
    }

    @Override
    public String getNamespace() {
        return MavenProjectExt.NAMESPACE;
    }

    @Override
    public String getXsdFileName() {
        return "model-maven.xsd"; //$NON-NLS-1$
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
        if (MavenProjectExt.PROPERTY_GROUPID.equals(propertyName)) {
            validators.add(new MavenIdValidator(Severity.ERROR, getExtensionClass(), propertyName, caption));
        }
        else if (MavenProjectExt.PROPERTY_REACTOR_POM.equals(propertyName)) {
            validators.add(new RelativePomPathValidator(Severity.ERROR, caption));
        }
        else if (MavenProjectExt.PROPERTY_SITE_URL.equals(propertyName)) {
            validators.add(new URLValidator(Severity.FATAL, getExtensionClass(), propertyName, caption));
            validators.add(new HostReachableValidator(getExtensionClass(), propertyName));
        }
        return validators;
    }
}
