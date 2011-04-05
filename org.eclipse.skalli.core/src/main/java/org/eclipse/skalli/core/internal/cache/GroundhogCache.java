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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Implementation of the "Groundhog" cache strategy.
 * <p>
 * This cache remembers the point in time when each entry was read for the last time.
 * The entry which was not accessed for the longest period of time will be discarded,
 * if there is a need to do so.
 * </p>
 * <p>
 * Furthermore, this cache remembers its data only for the current day.
 * The next day it starts over empty again.
 * For more details ask Phil Connors (a.k.a. Bill Murray).
 * </p>
 * @author d049863 (simon)
 *
 * @param <T_KEY>
 * @param <T_VALUE>
 */
public class GroundhogCache<T_KEY, T_VALUE> extends AbstractCache<T_KEY, T_VALUE, Long> {

  private int activeDayOfYear;
  private int activeYear;

  public GroundhogCache(int maxSize) {
    super(maxSize);
    initializeIfNeeded();
  }

  Calendar getCalendar() {
    return new GregorianCalendar();
  }

  private void initializeIfNeeded() {
    Calendar cal = getCalendar();
    int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
    int year = cal.get(Calendar.YEAR);
    if (dayOfYear != activeDayOfYear || year != activeYear) {
      activeDayOfYear = dayOfYear;
      activeYear = year;
      clear();
    }
  }

  @Override
  protected Long createMetaInfo(T_KEY key) {
    return System.nanoTime();
  }

  @Override
  protected void beforeAccess(T_KEY key) {
    initializeIfNeeded();
  };

  @Override
  protected Long onAccess(T_KEY key, Long metaInfo) {
    return System.nanoTime();
  }

  @Override
  protected T_KEY calcEntryToDiscard(Map<T_KEY, Long> metaInfos) {
    long oldest = System.nanoTime();
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

