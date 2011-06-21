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
package org.eclipse.skalli.persistence.db;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import org.apache.commons.io.IOUtils;
import org.eclipse.skalli.api.java.StorageException;
import org.eclipse.skalli.api.java.StorageService;
import org.eclipse.skalli.persistence.db.entities.HistoryStorageItem;
import org.eclipse.skalli.persistence.db.entities.StorageItem;

public class PersistenceDB implements StorageService {
    private static EntityManagerFactory emf;

    @Override
    public void write(String category, String id, InputStream blob) throws StorageException {
        EntityManager em = getEntityManager();

        StorageItem item = new StorageItem();
        item.setId(id);
        item.setCategory(category);
        item.setDateModified(new Date());
        try {
            item.setContent(IOUtils.toString(blob, "UTF-8"));

            em.getTransaction().begin();
            em.persist(item);
            em.getTransaction().commit();
        } catch (Exception e) {
            throw new StorageException("Failed to store data", e);
        } finally {
            em.close();
        }
    }

    @Override
    public InputStream read(String category, String id) throws StorageException {
        EntityManager em = getEntityManager();

        List<StorageItem> resultList;
        try {
            TypedQuery<StorageItem> query = em.createNamedQuery("getItemById", StorageItem.class);
            query.setParameter("id", id);
            resultList = query.getResultList();
        } catch (Exception e) {
            throw new StorageException("Failed to retrieve data", e);
        } finally {
            em.close();
        }

        if (!resultList.isEmpty()) {
            StorageItem row = resultList.get(0);

            try {
                return new ByteArrayInputStream(row.getContent().getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new StorageException("Failed to convert content to UTF-8", e);
            }
        }

        return null;
    }

    @Override
    public void archive(String category, String id) throws StorageException {
        // read from StorageItem
        InputStream is = read(category, id);

        // write to HistoryStorageItem
        EntityManager em = getEntityManager();
        HistoryStorageItem item = new HistoryStorageItem();
        item.setId(id);
        item.setCategory(category);
        item.setDateCreated(new Date());
        try {
            item.setContent(IOUtils.toString(is, "UTF-8"));

            em.getTransaction().begin();
            em.persist(item);
            em.getTransaction().commit();
        } catch (Exception e) {
            throw new StorageException("Failed to archive data", e);
        } finally {
            IOUtils.closeQuietly(is);
            em.close();
        }
    }

    public List<HistoryStorageItem> getHistory(String category, String id) throws StorageException {
        EntityManager em = getEntityManager();

        List<HistoryStorageItem> resultList;
        try {
            TypedQuery<HistoryStorageItem> query = em.createNamedQuery("getItemsByCompositeKey",
                    HistoryStorageItem.class);
            query.setParameter("category", category);
            query.setParameter("id", id);
            resultList = query.getResultList();
        } catch (Exception e) {
            throw new StorageException("Failed to retrieve historical data", e);
        } finally {
            em.close();
        }

        return resultList;
    }

    @Override
    public List<String> keys(String category) throws StorageException {
        EntityManager em = getEntityManager();

        List<String> resultList;
        try {
            TypedQuery<String> query = em.createNamedQuery("getIdsByCategory", String.class);
            query.setParameter("category", category);
            resultList = query.getResultList();
        } catch (Exception e) {
            throw new StorageException("Failed to retrieve IDs", e);
        } finally {
            em.close();
        }

        return resultList;
    }

    private EntityManager getEntityManager() throws StorageException {
        try {
            return emf.createEntityManager();
        } catch (Exception e) {
            throw new StorageException("EntityManager could not be created", e);
        }
    }

    public synchronized void setService(EntityManagerFactory emFactory) {
        emf = emFactory;
    }

    public synchronized void unsetService(EntityManagerFactory emFactory) {
        emf = null;
    }

}
