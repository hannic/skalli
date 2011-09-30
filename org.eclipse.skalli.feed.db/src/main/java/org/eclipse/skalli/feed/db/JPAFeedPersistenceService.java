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
package org.eclipse.skalli.feed.db;

import java.util.Collection;

import org.eclipse.skalli.api.java.feeds.Entry;
import org.eclipse.skalli.api.java.feeds.FeedPersistenceService;
import org.eclipse.skalli.api.java.feeds.FeedServiceException;
import org.eclipse.skalli.feed.db.entities.EntryJPA;

public class JPAFeedPersistenceService implements FeedPersistenceService {

    @Override
    public void merge(Collection<Entry> entries) throws FeedServiceException {
        // TODO Auto-generated method stub
    }

    @Override
    public Entry createEntry() {
        return new EntryJPA();
    }

}
