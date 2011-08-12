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

import static org.easymock.EasyMock.*;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.skalli.model.ext.maven.MavenCoordinate;
import org.eclipse.skalli.model.ext.maven.MavenReactor;
import org.eclipse.skalli.nexus.NexusArtifact;
import org.eclipse.skalli.nexus.NexusClient;
import org.eclipse.skalli.nexus.NexusClientException;
import org.eclipse.skalli.nexus.NexusSearchResult;
import org.junit.Test;

public class NexusVersionsResolverTest {

    private NexusSearchResult createNexusSearchResultStub(String groupId, String artifactId, String versions[])
    {
        NexusSearchResult mock = createMock(NexusSearchResult.class);

        List<NexusArtifact> artifacts = new ArrayList<NexusArtifact>();
        for (int i = 0; i < versions.length; i++) {
            NexusArtifact createNexusArtifactSub = createNexusArtifactSub(groupId, artifactId, versions[i]);
            artifacts.add(createNexusArtifactSub);
            replay(createNexusArtifactSub);
        }

        expect(mock.getArtifacts()).andReturn(artifacts);
        return mock;
    }

    private NexusArtifact createNexusArtifactSub(String groupId, String artifactId, String version) {
        NexusArtifact naMock = createMock(NexusArtifact.class);
        expect(naMock.getGroupId()).andReturn(groupId);
        expect(naMock.getArtifactId()).andReturn(artifactId);
        expect(naMock.getVersion()).andReturn(version);
        return naMock;
    }

    private void expectSearchArtifactVersions(NexusClient nexusClientMock, MavenCoordinate coordinate,
            String[] versions) throws NexusClientException, IOException {
        NexusSearchResult createNexusSearchResultStub = createNexusSearchResultStub(coordinate.getGroupId(),
                coordinate.getArtefactId(),
                versions);
        expect(
                nexusClientMock.searchArtifactVersions(coordinate.getGroupId(),
                        coordinate.getArtefactId())).andReturn(
                createNexusSearchResultStub);
        replay(createNexusSearchResultStub);
    }

    @Test
    public void testAddNexusVersionsMavenReactor() throws NexusClientException, IOException {

        MavenReactor mavenReactor = new MavenReactor();
        MavenCoordinate mavenCoordinateParent = new MavenCoordinate("org.example.helloworld",
                "org.example.helloworld", null);
        MavenCoordinate moduleA = new MavenCoordinate("org.example.helloworld",
                "org.example.helloworld.modulA", null);
        MavenCoordinate moduleB = new MavenCoordinate("org.example.helloworld",
                "org.example.helloworld.modulB", null);

        mavenReactor.setCoordinate(mavenCoordinateParent);
        mavenReactor.addModule(moduleA);
        mavenReactor.addModule(moduleB);

        NexusClient nexusClientMock = createMock(NexusClient.class);

        expectSearchArtifactVersions(nexusClientMock, mavenCoordinateParent, new String[] { "0.0.1", "0.0.2" });
        expectSearchArtifactVersions(nexusClientMock, moduleA, new String[] { "0.0.4", "0.0.5" });
        expectSearchArtifactVersions(nexusClientMock, moduleB, new String[] { "0.0.1", "0.0.2", "0.0.3" });
        replay(nexusClientMock);

        NexusVersionsResolver resolver = new NexusVersionsResolver(nexusClientMock);
        resolver.addNexusVersions(mavenReactor);

        assertThat(mavenReactor.getCoordinate().getVersions().size(), is(2));
        assertThat(mavenReactor.getCoordinate().getVersions(), hasItem("0.0.1"));
        assertThat(mavenReactor.getCoordinate().getVersions(), hasItem("0.0.2"));

        assertThat(moduleA.getVersions().size(), is(2));
        assertThat(moduleA.getVersions(), hasItem("0.0.4"));
        assertThat(moduleA.getVersions(), hasItem("0.0.5"));

        assertThat(moduleB.getVersions().size(), is(3));
        assertThat(moduleB.getVersions(), hasItem("0.0.1"));
        assertThat(moduleB.getVersions(), hasItem("0.0.2"));
        assertThat(moduleB.getVersions(), hasItem("0.0.3"));
    }

    @Test
    public void testAddNexusVersionsMavenCoordinate() throws NexusClientException, IOException {
        MavenCoordinate mavenCoordinate = new MavenCoordinate("org.example.helloworld",
                "org.example.helloworld.updatesite", "zip");
        NexusClient nexusClientMock = createMock(NexusClient.class);
        expectSearchArtifactVersions(nexusClientMock, mavenCoordinate, new String[] { "0.0.1", "0.0.2" });
        replay(nexusClientMock);

        NexusVersionsResolver resolver = new NexusVersionsResolver(nexusClientMock);

        resolver.addNexusVersions(mavenCoordinate);

        assertThat(mavenCoordinate.getVersions().size(), is(2));
        assertThat(mavenCoordinate.getVersions(), hasItem("0.0.1"));
        assertThat(mavenCoordinate.getVersions(), hasItem("0.0.2"));
    }

}
