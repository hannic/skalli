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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.eclipse.skalli.api.java.feeds.Entry;
import org.eclipse.skalli.api.java.feeds.FeedPersistenceService;
import org.eclipse.skalli.api.java.feeds.FeedServiceException;
import org.eclipse.skalli.feed.db.entities.EntryJPA;

public class JPAFeedPersistenceService implements FeedPersistenceService {

    private static EntityManagerFactory emf;

    @Override
    public void merge(Collection<Entry> entries) throws FeedServiceException {
        for (Entry entry : entries) {
            EntityManager em = getEntityManager();
            EntityTransaction tx = null;
            try {
                tx = em.getTransaction();
                tx.begin();
                em.merge(entry);
                tx.commit();
            } catch (RuntimeException e) {
                if (tx != null && tx.isActive()) {
                    tx.rollback();
                }
                throw new FeedServiceException("Failed to persist " + EntryJPA.class.getSimpleName() + " ("
                        + entry.getProjectId().toString() + ")", e);
            } finally {
                em.close();
            }
        }
    }

    private EntityManager getEntityManager() throws FeedServiceException {
        try {
            return emf.createEntityManager();
        } catch (RuntimeException e) {
            throw new FeedServiceException("EntityManager could not be created", e);
        }
    }

    public synchronized void setService(EntityManagerFactory emFactory) {
        emf = emFactory;
    }

    public synchronized void unsetService(EntityManagerFactory emFactory) {
        emf = null;
    }

    @Override
    public EntryJPA createEntry() {
        return new EntryJPA();
    }

}
