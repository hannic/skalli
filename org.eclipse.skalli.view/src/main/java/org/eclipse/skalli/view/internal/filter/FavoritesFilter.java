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
package org.eclipse.skalli.view.internal.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.eclipse.skalli.api.java.ProjectService;
import org.eclipse.skalli.api.java.SearchHit;
import org.eclipse.skalli.api.java.SearchResult;
import org.eclipse.skalli.api.java.SearchService;
import org.eclipse.skalli.common.Services;
import org.eclipse.skalli.common.User;
import org.eclipse.skalli.model.core.Project;

public class FavoritesFilter extends AbstractSearchFilter {

    @Override
    protected boolean showNearestProjects(User user, ServletRequest request, ServletResponse response) {
        return false;
    }

    @Override
    protected SearchResult<Project> getSearchHits(User user, ServletRequest request, ServletResponse response,
            int start, int viewSize)
            throws IOException, ServletException {
        List<Project> projects = new ArrayList<Project>();
        if (user != null) {
            ProjectService projectService = Services.getRequiredService(ProjectService.class);
            List<UUID> uuids = getFavorites(user).getProjects();
            if (start < uuids.size()) {
                int end = Math.min(start + viewSize, uuids.size());
                for (int i = start; i < end; ++i) {
                    Project project = projectService.getByUUID(uuids.get(i));
                    if (project != null) {
                        projects.add(project);
                    }
                }
            }
        }
        SearchService searchService = Services.getRequiredService(SearchService.class);
        List<SearchHit<Project>> searchHits = searchService.asSearchHits(projects);
        SearchResult<Project> searchResult = new SearchResult<Project>();
        searchResult.setResult(searchHits);
        searchResult.setResultCount(searchHits.size());
        searchResult.setDuration(0);
        return searchResult;
    }

    @Override
    protected String getTitle(User user) {
        return user != null ? "Favorites for " + user.getDisplayName() : "Favorites";
    }

}
