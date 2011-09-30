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
package org.eclipse.skalli.feed.db;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.eclipse.skalli.api.java.feeds.Entry;
import org.eclipse.skalli.api.java.feeds.FeedService;
import org.eclipse.skalli.api.java.feeds.FeedServiceException;

public class JPAFeedService implements FeedService {

    @Override
    public List<Entry> findEntries(UUID projectId, int maxResults) throws FeedServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Entry> findEntries(UUID projectId, Collection<String> categories, int maxResults)
            throws FeedServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> findSources(UUID projectId) throws FeedServiceException {
        // TODO Auto-generated method stub
        return null;
    }
}
