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
package org.eclipse.skalli.core.internal.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Represents a cache for arbitrary data objects.
 * <p>
 * This class needs to be extended by a caching strategy.
 * </p>
 * @author d049863 (simon)
 *
 * @param <T_KEY>
 * @param <T_VALUE>
 * @param <T_META>
 */
public abstract class AbstractCache<T_KEY, T_VALUE, T_META> implements Cache<T_KEY, T_VALUE> {

    private final int cacheSize;

    private final Map<T_KEY, T_META> metaInfos = new HashMap<T_KEY, T_META>();
    private final Map<T_KEY, T_VALUE> cache = new HashMap<T_KEY, T_VALUE>();

    protected abstract T_META createMetaInfo(T_KEY key);

    protected abstract T_KEY calcEntryToDiscard(Map<T_KEY, T_META> metaInfos);

    protected abstract T_META onAccess(T_KEY key, T_META metaInfo);

    public AbstractCache(int cacheSize) {
        super();
        if (cacheSize < 1)
            throw new IllegalArgumentException("cacheSize needs to be bigger than 0");
        this.cacheSize = cacheSize;
    }

    /* (non-Javadoc)
     * @see com.sap.ide.mylyn.internal.jcwb.core.util.Cache#put(T_KEY, T_VALUE)
     */
    public synchronized final void put(T_KEY key, T_VALUE value) {
        if (key == null)
            throw new IllegalArgumentException("Key must not be NULL!");
        beforeAccess(key);
        synchronized (cache) {
            if (cache.size() >= cacheSize) {
                T_KEY keyDiscard = calcEntryToDiscard(metaInfos);
                if (keyDiscard == null || !cache.containsKey(keyDiscard)) {
                    // Strategy did not determine a valid key to be removed, so just decide randomly as a fallback strategy
                    List<T_KEY> keys = new ArrayList<T_KEY>(cache.keySet());
                    int position = new Random(System.currentTimeMillis()).nextInt(keys.size());
                    keyDiscard = keys.get(position);
                }
                cache.remove(keyDiscard);
                metaInfos.remove(keyDiscard);
            }
            cache.put(key, value);
            metaInfos.put(key, createMetaInfo(key));
        }
    }

    /* (non-Javadoc)
     * @see com.sap.ide.mylyn.internal.jcwb.core.util.Cache#get(T_KEY)
     */
    public synchronized final T_VALUE get(T_KEY key) {
        if (key == null)
            throw new IllegalArgumentException("Key must not be NULL!");
        beforeAccess(key);
        synchronized (cache) {
            T_VALUE value = cache.get(key);
            if (value != null) {
                T_META newMetaInfo = onAccess(key, metaInfos.get(key));
                metaInfos.put(key, newMetaInfo);
            }
            return value;
        }
    }

    protected void beforeAccess(T_KEY key) {
    }

    public synchronized final void clear() {
        synchronized (cache) {
            cache.clear();
            metaInfos.clear();
        }
    }

    @Override
    public synchronized final Collection<T_VALUE> getAll() {
        beforeAccess(null);
        synchronized (cache) {
            return cache.values();
        }
    }

}
