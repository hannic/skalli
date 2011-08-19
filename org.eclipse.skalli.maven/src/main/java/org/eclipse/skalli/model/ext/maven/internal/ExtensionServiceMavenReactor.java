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

import java.util.logging.Logger;

import org.eclipse.skalli.log.Log;
import org.eclipse.skalli.model.ext.AbstractIndexer;
import org.eclipse.skalli.model.ext.AliasedConverter;
import org.eclipse.skalli.model.ext.ExtensionService;
import org.eclipse.skalli.model.ext.ExtensionServiceBase;
import org.eclipse.skalli.model.ext.maven.MavenReactorProjectExt;
import org.osgi.service.component.ComponentContext;

public class ExtensionServiceMavenReactor
        extends ExtensionServiceBase<MavenReactorProjectExt>
        implements ExtensionService<MavenReactorProjectExt>
{

    private static final Logger LOG = Log.getLogger(ExtensionServiceMavenReactor.class);

    private static final String CAPTION = "Maven Reactor";
    private static final String DESCRIPTION = "Information related to a Maven reactor project and its modules";

    @Override
    public Class<MavenReactorProjectExt> getExtensionClass() {
        return MavenReactorProjectExt.class;
    }

    protected void activate(ComponentContext context) {
        LOG.info("activated model extension: " + getShortName()); //$NON-NLS-1$
    }

    protected void deactivate(ComponentContext context) {
        LOG.info("deactivated model extension: " + getShortName()); //$NON-NLS-1$
    }

    @Override
    public String getShortName() {
        return "mavenReactor"; //$NON-NLS-1$
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
        return new MavenReactorConverter(host);
    }

    @Override
    public String getModelVersion() {
        return MavenReactorProjectExt.MODEL_VERSION;
    }

    @Override
    public String getNamespace() {
        return MavenReactorProjectExt.NAMESPACE;
    }

    @Override
    public String getXsdFileName() {
        return "model-maven-reactor.xsd"; //$NON-NLS-1$
    }

    @Override
    public AbstractIndexer<MavenReactorProjectExt> getIndexer() {
        return new MavenReactorIndexer();
      }

}
