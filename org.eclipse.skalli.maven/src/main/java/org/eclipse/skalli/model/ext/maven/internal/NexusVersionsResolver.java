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

import org.eclipse.skalli.common.util.ComparatorUtils;
import org.eclipse.skalli.model.ext.maven.MavenCoordinate;
import org.eclipse.skalli.model.ext.maven.MavenReactor;
import org.eclipse.skalli.nexus.NexusArtifact;
import org.eclipse.skalli.nexus.NexusClient;
import org.eclipse.skalli.nexus.NexusSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NexusVersionsResolver {
    private static final Logger LOG = LoggerFactory.getLogger(NexusVersionsResolver.class);
    private NexusClient nexusClient;

    public NexusVersionsResolver(NexusClient nexusClient) {
        this.nexusClient = nexusClient;
        if (this.nexusClient == null) {
            LOG.warn("Can't calculate versions: No Nexus client available");
        }
    }

    public void setNexusVersions(MavenReactor mavenReactor) {
        if (nexusClient == null || mavenReactor == null) {
            return;
        }
        setNexusVersion(mavenReactor.getCoordinate());
        for (MavenCoordinate mavenCoordinate : mavenReactor.getModules()) {
            if (mavenCoordinate != null) {
                setNexusVersion(mavenCoordinate);
            }
        }
        return;
    }

    void setNexusVersion(MavenCoordinate mavenCoordinate) {
        if (mavenCoordinate == null) {
            return;
        }

        try {
            NexusSearchResult searchResult = nexusClient.searchArtifactVersions(mavenCoordinate.getGroupId(),
                    mavenCoordinate.getArtefactId());
            //we got a search Result, hence we can delete the old versions.
            mavenCoordinate.getVersions().clear();
            for (NexusArtifact nexusArtifact : searchResult.getArtifacts()) {
                mavenCoordinate.getVersions().add(nexusArtifact.getVersion());
            }

        } catch (Exception e) {
            LOG.warn(MessageFormat.format("Can''t get Maven version for {0}: {1}", mavenCoordinate, e.getMessage()), e);
        }
    }

    public void addVersions(MavenReactor newReactor, MavenReactor oldReactor) {
        if (newReactor == null || oldReactor == null) {
            return;
        }

        if (newReactor.getCoordinate() != null
                && haveSameGroupArtifact(newReactor.getCoordinate(), oldReactor.getCoordinate())) {
            newReactor.getCoordinate().getVersions().addAll(oldReactor.getCoordinate().getVersions());
        }

        for (MavenCoordinate mavenCoordinate : newReactor.getModules()) {
            if (mavenCoordinate != null) {
                MavenCoordinate oldCoordinate = findModuleCoordinate(oldReactor, mavenCoordinate);
                if (oldCoordinate != null) {
                    mavenCoordinate.getVersions().addAll(oldCoordinate.getVersions());
                }
            }
        }
        return;

    }

    private MavenCoordinate findModuleCoordinate(MavenReactor reactor, MavenCoordinate mavenCoordinate) {
        if (reactor == null || mavenCoordinate == null) {
            return null;
        }
        for (MavenCoordinate reactorCoordinate : reactor.getModules()) {
            if (haveSameGroupArtifact(mavenCoordinate, reactorCoordinate)) {
                return reactorCoordinate;
            }

        }

        return null;
    }

    private boolean haveSameGroupArtifact(MavenCoordinate c1, MavenCoordinate c2) {
        if (c1 == null && c2 == null) {
            return true;
        }
        if (c1 == null) {
            return false;
        }
        if (c2 == null) {
            return false;
        }

        if (ComparatorUtils.compare(c1.getGroupId(), c2.getGroupId()) != 0) {
            return false;
        }

        if (ComparatorUtils.compare(c1.getArtefactId(), c2.getArtefactId()) != 0) {
            return false;
        }

        return true;
    }
}
