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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.logging.Logger;

import org.eclipse.skalli.log.Log;
import org.eclipse.skalli.model.ext.maven.MavenCoordinate;
import org.eclipse.skalli.model.ext.maven.MavenReactor;
import org.eclipse.skalli.nexus.NexusArtifact;
import org.eclipse.skalli.nexus.NexusClient;
import org.eclipse.skalli.nexus.NexusClientException;
import org.eclipse.skalli.nexus.NexusSearchResult;

/**
 *
 */
public class NexusVersionsResolver {
    private static final Logger LOG = Log.getLogger(NexusVersionsResolver.class);
    private NexusClient nexusClient;

    public NexusVersionsResolver(NexusClient nexusClient) {
        this.nexusClient = nexusClient;
    }

    /**
     * @param mavenReactor
     */
    public void addNexusVersions(MavenReactor mavenReactor) {
        try {
            addNexusVersions(mavenReactor.getCoordinate());
            for (MavenCoordinate mavenCoordinate : mavenReactor.getModules()) {
                if (mavenCoordinate != null) {
                    addNexusVersions(mavenCoordinate);
                }
            }
        } catch (IOException e) {
            LOG.info(MessageFormat.format("Can't get Maven version for {0}: {1}",
                    mavenReactor.getCoordinate(), e.getMessage()));
        } catch (NexusClientException e) {
            LOG.info(MessageFormat.format("Can't get Maven version for {0}: {1}",
                    mavenReactor.getCoordinate(), e.getMessage()));
        }
    }

    void addNexusVersions(MavenCoordinate mavenCoordinate) throws NexusClientException, IOException {
        NexusSearchResult searchResult = nexusClient.searchArtifactVersions(mavenCoordinate.getGroupId(),
                mavenCoordinate.getArtefactId());
        for (NexusArtifact nexusArtifact : searchResult.getArtifacts()) {
            mavenCoordinate.getVersions().add(nexusArtifact.getVersion());
        }
    }

}
