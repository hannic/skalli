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
package org.eclipse.skalli.core.internal.persistence;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.eclipse.skalli.api.java.EntityFilter;
import org.eclipse.skalli.api.java.EntityServiceImpl;
import org.eclipse.skalli.api.java.FavoritesService;
import org.eclipse.skalli.model.core.Favorites;
import org.eclipse.skalli.model.ext.Issue;
import org.eclipse.skalli.model.ext.Severity;
import org.eclipse.skalli.model.ext.ValidationException;

public class FavoritesServiceImpl extends EntityServiceImpl<Favorites> implements FavoritesService {

  @Override
  public Class<Favorites> getEntityClass() {
    return Favorites.class;
  }

  @Override
  public Favorites getFavorites(String userId) {
    Favorites favorites = getPersistenceService().getEntity(Favorites.class, new FavoritesFilter(userId));
    return favorites != null? favorites : new Favorites(userId);
  }

  @Override
  public void addFavorite(String userId, UUID project) throws ValidationException {
    Favorites favorites = getFavorites(userId);
    favorites.addProject(project);
    persist(favorites, userId);
  }

  @Override
  public void removeFavorite(String userId, UUID project) throws ValidationException {
    Favorites favorites = getFavorites(userId);
    favorites.removeProject(project);
    persist(favorites, userId);
  }

  protected static class FavoritesFilter implements EntityFilter<Favorites> {
    private String userId;
    public FavoritesFilter(String userId) {
      this.userId = userId;
    }

    public String getUserId() {
      return userId;
    }

    @Override
    public boolean accept(Class<Favorites> entityClass, Favorites entity) {
      return entity.getUserId().equals(userId);
    }
  }

  @Override
  protected void validateEntity(Favorites entity) throws ValidationException {
    SortedSet<Issue> issues = validate(entity, Severity.FATAL);
    if (issues.size() > 0) {
      throw new ValidationException("Favorites could not be saved due to the following reasons:", issues);
    }
  }

  @Override
  protected SortedSet<Issue> validateEntity(Favorites entity, Severity minSeverity) {
    return new TreeSet<Issue>();
  }
}

