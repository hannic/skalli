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
package org.eclipse.skalli.core.internal.tagging;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.skalli.api.java.PersistenceService;
import org.eclipse.skalli.api.java.TaggingService;
import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.ext.Taggable;

public class TaggingServiceImpl implements TaggingService {

    private PersistenceService persistenceService;

    protected void bindPersistenceService(PersistenceService srvc) {
        this.persistenceService = srvc;
    }

    protected void unbindPersistenceService(PersistenceService srvc) {
        this.persistenceService = null;
    }

    @Override
    public Set<String> getAvailableTags() {
        Set<String> result = new HashSet<String>();
        List<Project> entities = persistenceService.getEntities(Project.class);
        for (Project entityBase : entities) {
            if (entityBase instanceof Taggable) {
                Taggable taggable = (Taggable) entityBase;
                Set<String> tags = taggable.getTags();
                if (tags != null) {
                    for (String tag : tags) {
                        if (!result.contains(tag)) {
                            result.add(tag);
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Set<Taggable> getTaggables(String tag) {
        Set<Taggable> result = new HashSet<Taggable>();
        List<Project> entities = persistenceService.getEntities(Project.class);
        for (Project entityBase : entities) {
            if (entityBase instanceof Taggable) {
                Taggable taggable = (Taggable) entityBase;
                if (taggable.hasTag(tag)) {
                    result.add(taggable);
                }
            }
        }
        return result;
    }

    @Override
    public Map<String, Set<Taggable>> getTaggables() {
        Map<String, Set<Taggable>> result = new HashMap<String, Set<Taggable>>();
        for (String tag : getAvailableTags()) {
            Set<Taggable> taggables = getTaggables(tag);
            result.put(tag, taggables);
        }
        return result;
    }

}
