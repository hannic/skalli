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
package org.eclipse.skalli.nexus.internal;

import java.net.URL;

import org.eclipse.skalli.nexus.NexusArtifact;
import org.w3c.dom.Element;

public class NexusArtifactImpl implements NexusArtifact {

    private URL resourceURI;
    private String groupId;
    private String artifactId;
    private String version;
    private String classifier;
    private String packaging;
    private String extension;
    private String repoId;
    private String contextId;
    private URL pomLink;
    private URL artifactLink;

    public NexusArtifactImpl(Element rootElement) {

    }

    @Override
    public URL getResourceURI() {
        return resourceURI;
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    @Override
    public String getArtifactId() {
        return artifactId;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getClassifier() {
        return classifier;
    }

    @Override
    public String getPackaging() {
        return packaging;
    }

    @Override
    public String getExtension() {
        return extension;
    }

    @Override
    public String getRepoId() {
        return repoId;
    }

    @Override
    public String getContextId() {
        return contextId;
    }

    @Override
    public URL getPomLink() {
        return pomLink;
    }

    @Override
    public URL getArtifactLink() {
        return artifactLink;
    }
}
