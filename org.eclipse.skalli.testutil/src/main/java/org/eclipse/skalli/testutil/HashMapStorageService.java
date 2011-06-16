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
package org.eclipse.skalli.testutil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.eclipse.skalli.api.java.StorageException;
import org.eclipse.skalli.api.java.StorageService;

/**
 * Simple implementation of a storage service based on a hash map
 * for testing purposes.
 */
public class HashMapStorageService implements StorageService {

    private Map<String,byte[]> blobStore = new HashMap<String,byte[]>();

    private String getKey(String category, String key) {
        return category + "/" + key; //$NON-NLS-1$
    }

    @Override
    public void write(String category, String key, InputStream blob) throws StorageException {
        try {
            blobStore.put(getKey(category, key), IOUtils.toByteArray(blob));
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    @Override
    public InputStream read(String category, String key) throws StorageException {
        return new ByteArrayInputStream(blobStore.get(getKey(category, key)));
    }

    @Override
    public void archive(String category, String key) throws StorageException {
        byte[] content = blobStore.get(getKey(category, key));
        if (content != null) {
            blobStore.put(getKey(category, key) + "/" + System.currentTimeMillis(), content); //$NON-NLS-1$
        }
    }

    @Override
    public List<String> keys(String category) throws StorageException {
        return new ArrayList<String>(blobStore.keySet());
    }
}