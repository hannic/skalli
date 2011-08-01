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

import java.util.List;

import org.eclipse.skalli.nexus.NexusArtifact;
import org.eclipse.skalli.nexus.NexusSearchResult;
import org.w3c.dom.Element;

public class NexusSearchResponseImpl implements NexusSearchResult {

    private int totalCount;
    private int from;
    private int count;
    private boolean toManyResults;
    private List<NexusArtifact> artifacts;

    public NexusSearchResponseImpl(Element rootElement) {

    }

    @Override
    public int getTotalCount() {
        return totalCount;
    }

    @Override
    public int getFrom() {
        return from;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public boolean isToManyResults() {
        return toManyResults;
    }

    @Override
    public List<NexusArtifact> getArtifacts() {
        return artifacts;
    }
}
