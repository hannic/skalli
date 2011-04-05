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
package org.eclipse.skalli.common.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class CollectionUtils {
  public static <T> Set<T> asSet(T... args) {
    Set<T> result = new HashSet<T>();
    if (args != null) {
      for (T arg: args) {
        result.add(arg);
      }
    }
    return result;
  }

  public static <T> SortedSet<T> asSortedSet(T... args) {
    SortedSet<T> result = new TreeSet<T>();
    if (args != null) {
      for (T arg: args) {
        result.add(arg);
      }
    }
    return result;
  }

  public static Map<String,String> asMap(String[][] args) {
    HashMap<String,String> result = new HashMap<String,String>();
    if (args != null) {
      for (int i=0; i<args.length; ++i) {
        String key = args[i][0];
        String value = args[i][1];
        result.put(key, value);
      }
    }
    return result;
  }

  public static <K,V> Map<K,V> asMap(K key, V value) {
    HashMap<K,V> result = new HashMap<K,V>(1);
    result.put(key, value);
    return result;
  }

  /**
   * Similar to Collection.add() but only allows non-null elements.
   *
   * @return <code>true</code> if this collection changed as a result of the call
   */
  public static <T> boolean addSafe(final Collection<T> collection, T element) {
    if (element != null) {
      return collection.add(element);
    } else {
      return false;
    }
  }

  public static Map<String,String> addAll(Map<String,String> map, String[][] args) {
    map.putAll(asMap(args));
    return map;
  }

  public static Map<String,List<String>> asMap(String key, String... args) {
    HashMap<String,List<String>> result = new HashMap<String,List<String>>();
    if (args != null) {
      result.put(key, Arrays.asList(args));
    }
    return result;
  }

  public static class MapBuilder<T> {
    private Map<String,T> map;
    public MapBuilder() {
      map = new HashMap<String,T>();
    }
    public MapBuilder(Map<String,T> map) {
      this.map = map;
    }
    public MapBuilder<T> put(String key, T value) {
      map.put(key, value);
      return this;
    }
    public Map<String,T> toMap() {
      return map;
    }
  }

  public static boolean contains(Map<String,Set<String>> itemSet, String extensionClassName, Object propertyId) {
    Set<String> items = itemSet.get(extensionClassName);
    return items != null? items.contains(propertyId) : false;
  }

  public static <T> T get(Map<String,Map<String,T>> itemSet, String extensionClassName, Object propertyId) {
    Map<String,T> items = itemSet.get(extensionClassName);
    return items != null? items.get(propertyId) : null;
  }

  public static Collection<?> getCollection(Map<String,Map<String,List<String>>> itemSet, String extensionClassName, Object propertyId) {
    Map<String,List<String>> items = itemSet.get(extensionClassName);
    if (items == null) {
      return Collections.EMPTY_LIST;
    }
    List<String> list = items.get(propertyId);
    return list != null?  list: Collections.EMPTY_LIST;
  }

  public static boolean isNotBlank(Collection<?> c) {
    return c != null && !c.isEmpty();
  }

  public static int[] asSortedArray(int[] a) {
    Arrays.sort(a);
    return a;
  }
}

