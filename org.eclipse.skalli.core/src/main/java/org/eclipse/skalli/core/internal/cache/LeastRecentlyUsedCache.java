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

import java.util.Map;
import java.util.Map.Entry;

/**
 * Implementation of the Least Recently Used (LRU) cache strategy.
 * <p>
 * This cache remembers the point in time when each entry was read for the last time.
 * The entry which was not accessed for the longest period of time will be discarded,
 * if there is a need to do so.
 * </p>
 * @author d049863 (simon)
 *
 * @param <T_KEY>
 * @param <T_VALUE>
 */
public class LeastRecentlyUsedCache<T_KEY, T_VALUE> extends AbstractCache<T_KEY, T_VALUE, Long> {

  public LeastRecentlyUsedCache(int cacheSize) {
    super(cacheSize);
  }

  @Override
  protected Long createMetaInfo(T_KEY key) {
    return System.currentTimeMillis();
  }

  @Override
  protected Long onAccess(T_KEY key, Long metaInfo) {
    return System.currentTimeMillis();
  }

  @Override
  protected T_KEY calcEntryToDiscard(Map<T_KEY, Long> metaInfos) {
    long oldest = System.currentTimeMillis();
    T_KEY oldestEntryKey = null;
    for (Entry<T_KEY, Long> entry : metaInfos.entrySet()) {
      if (entry.getValue() < oldest) {
        oldestEntryKey = entry.getKey();
        oldest = entry.getValue();
      }
    }
    return oldestEntryKey;
  }

}

