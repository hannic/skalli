/*******************************************************************************
 * Copyright (c) 2010 - 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.model.ext.maven.internal;

import java.text.MessageFormat;

import org.eclipse.skalli.model.ext.maven.MavenCoordinate;
import org.eclipse.skalli.model.ext.maven.MavenReactor;
import org.eclipse.skalli.nexus.NexusArtifact;
import org.eclipse.skalli.nexus.NexusClient;
import org.eclipse.skalli.nexus.NexusSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class NexusVersionsResolver {
    private static final Logger LOG = LoggerFactory.getLogger(NexusVersionsResolver.class);
    private NexusClient nexusClient;

    public NexusVersionsResolver(NexusClient nexusClient) {
        this.nexusClient = nexusClient;
        if (this.nexusClient == null) {
            LOG.warn("Can't calculate versions: No Nexus client available");
        }
    }

    public void addNexusVersions(MavenReactor mavenReactor) {
        if (nexusClient == null || mavenReactor == null) {
            return;
        }
        addNexusVersions(mavenReactor.getCoordinate());
        for (MavenCoordinate mavenCoordinate : mavenReactor.getModules()) {
            if (mavenCoordinate != null) {
                addNexusVersions(mavenCoordinate);
            }
        }
        return;
    }

    void addNexusVersions(MavenCoordinate mavenCoordinate) {
        if (mavenCoordinate == null) {
            return;
        }

        try {
            NexusSearchResult searchResult = nexusClient.searchArtifactVersions(mavenCoordinate.getGroupId(),
                    mavenCoordinate.getArtefactId());
            for (NexusArtifact nexusArtifact : searchResult.getArtifacts()) {
                mavenCoordinate.getVersions().add(nexusArtifact.getVersion());
            }

        } catch (Exception e) {
            LOG.warn(MessageFormat.format("Can''t get Maven version for {0}: {1}", mavenCoordinate, e.getMessage()), e);
        }
    }
}
