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

import java.util.Collection;

/**
 * Interface to an arbitrary cache implementation.
 * @author d049863 (simon)
 * @see {@link LeastRecentlyUsedCache}
 *
 * @param <T_KEY>
 * @param <T_VALUE>
 */
public interface Cache<T_KEY, T_VALUE> {

    /**
     * Stores a new entry.
     * <p>
     * If the cache is filled already, then there will be discarded one entry according to the cache strategy,
     * before the new entry actually gets added.
     * </p>
     * <p>
     * Please note that the key must implement equals() and hashCode() properly.
     * </p>
     * @param key identifies the entry. Must not be null.
     * @param value
     */
    public void put(T_KEY key, T_VALUE value);

    /**
     * Returns an entry from the cache.
     * <p>
     * Please be aware of the fact that according to the cache strategy there is no guarantee
     * that an entry which was stored once in the cache still is in there at a later point in time.
     * </p>
     * @param key identifies the entry. Must not be null.
     * @return the corresponding entry or null, if the entry is not contained in the cache (anymore).
     */
    public T_VALUE get(T_KEY key);

    public Collection<T_VALUE> getAll();

    public void clear();

}
