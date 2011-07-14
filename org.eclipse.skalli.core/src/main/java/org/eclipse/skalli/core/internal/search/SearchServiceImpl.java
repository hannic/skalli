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
package org.eclipse.skalli.core.internal.search;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.osgi.service.component.ComponentContext;

import org.eclipse.skalli.api.java.PagingInfo;
import org.eclipse.skalli.api.java.ProjectService;
import org.eclipse.skalli.api.java.QueryParseException;
import org.eclipse.skalli.api.java.SearchHit;
import org.eclipse.skalli.api.java.SearchResult;
import org.eclipse.skalli.api.java.SearchService;
import org.eclipse.skalli.common.Services;
import org.eclipse.skalli.log.Log;
import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.ext.AbstractIndexer;
import org.eclipse.skalli.model.ext.ExtensionService;

public class SearchServiceImpl implements SearchService {

    private static final Logger LOG = Log.getLogger(SearchServiceImpl.class);
    private LuceneIndex<Project> luceneIndex;

    protected void bindProjectService(ProjectService srvc) {
        LOG.info("Project service injected into search service"); //$NON-NLS-1$
        try {
            luceneIndex = new LuceneIndex<Project>(srvc);
            luceneIndex.initialize();
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    protected void unbindProjectService(ProjectService srvc) {
        LOG.info("Project service removed from search service"); //$NON-NLS-1$
        luceneIndex = null;
    }

    protected void activate(ComponentContext context) {
        LOG.info("Search service activated"); //$NON-NLS-1$
    }

    protected void deactivate(ComponentContext context) {
        LOG.info("Search service deactivated"); //$NON-NLS-1$
    }

    @Override
    public void reindex(Collection<Project> projects) {
        luceneIndex.reindex(projects);
    }

    @Override
    public void update(final Project project) {
        update(Collections.singleton(project));
    }

    @Override
    public void update(Collection<Project> projects) {
        luceneIndex.update(projects);
    }

    @Override
    public SearchResult<Project> findProjectsByQuery(String queryString, PagingInfo pagingInfo)
            throws QueryParseException {
        Set<String> fieldSet = new HashSet<String>();
        for (ExtensionService<?> ext : Services.getServices(ExtensionService.class)) {
            AbstractIndexer<?> indexer = ext.getIndexer();
            if (indexer != null) {
                Set<String> fields = indexer.getDefaultSearchFields();
                if (fields != null) {
                    fieldSet.addAll(fields);
                }
            }
        }
        return luceneIndex.search(fieldSet.toArray(new String[fieldSet.size()]), queryString, pagingInfo);
    }

    @Override
    public SearchResult<Project> findProjectsByUser(String queryString, PagingInfo pagingInfo)
            throws QueryParseException {
        String[] fields = new String[] { "allMembers" }; //$NON-NLS-1$
        return luceneIndex.search(fields, queryString, pagingInfo);
    }

    @Override
    public SearchResult<Project> findProjectsByTag(String queryString, PagingInfo pagingInfo)
            throws QueryParseException {
        String[] fields = new String[] { Project.PROPERTY_TAGS };
        return luceneIndex.search(fields, queryString, pagingInfo);
    }

    @Override
    public SearchResult<Project> getRelatedProjects(Project project, int count) {
        String[] fields = new String[] { Project.PROPERTY_NAME, Project.PROPERTY_DESCRIPTION, Project.PROPERTY_TAGS };
        return luceneIndex.moreLikeThis(project, fields, count);
    }

    @Override
    public List<SearchHit<Project>> asSearchHits(List<Project> projects) {
        return luceneIndex.entitiesToHit(projects);
    }

}
