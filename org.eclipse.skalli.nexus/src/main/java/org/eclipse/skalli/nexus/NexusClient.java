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
package org.eclipse.skalli.nexus;

import org.eclipse.skalli.nexus.internal.config.NexusConfig;

/**
 * Interface of the Nexus client.
 */
public interface NexusClient {

    /**
     * Retrieves information about the versions of a given artifact.
     * This method issues a call to the following Nexus path:
     * <tt>/service/local/data_index/{domain}/{target}/content?g={groupId}&a={artifactId}&from=0&count=10000</tt>,
     * where <tt>domain</tt> and <tt>target</tt> are taken from the {@link NexusConfig} Nexus configuration.
     *
     * @param groupId  the group id of the artifact.
     * @param artifactId  the artifact id of the artifact.
     * @return  the search result retrieved from Nexus.
     *
     * @see https://repository.sonatype.org/nexus-indexer-lucene-plugin/default/docs/rest.data_index.domain.target.content.html
     */
    public NexusSearchResult searchArtifactVersions(String groupId, String artifactId);

}
