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
package org.eclipse.skalli.api.java.feeds;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface FeedService {

    List<Entry> findEntries(UUID projectId, int maxResults) throws FeedServiceException;

    List<Entry> findEntries(UUID projectId, Collection<String> categories, int maxResults) throws FeedServiceException;

    List<String> findSources(UUID projectId) throws FeedServiceException;
}
