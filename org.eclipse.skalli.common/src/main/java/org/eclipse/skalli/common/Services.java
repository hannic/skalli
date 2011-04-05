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
package org.eclipse.skalli.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import org.eclipse.skalli.model.ext.ExtensionEntityBase;
import org.eclipse.skalli.model.ext.ExtensionService;


/**
 * Utiliy class that provides various methods to retrieve,
 * filter and iterate OSGi service instances.
 */
public class Services {

  /**
   * Returns the currently registered instance of the given OSGi
   * <code>service</code> interface, if any.
   *
   * @param <T>
   *          type parameter representing an OSGi service interface.
   * @param service
   *          the OSGi service interface for which an instance is to be
   *          returned.
   * @return the registered instance of <code>service</code>, or
   *         <code>null</code>.
   * @throws IllegalStateException
   *           if there is more than one instance of the service registered.
   */
  public static <T> T getService(Class<T> service) {
    return getService(service, false, null);
  }

  /**
   * Returns the currently registered instance of the given OSGi
   * <code>service</code> interface, if any.
   *
   * @param <T>
   *          type parameter representing an OSGi service interface.
   * @param service
   *          the OSGi service interface for which an instance is to be
   *          returned.
   * @param filter
   *          the OSGi filter to use when looking up the filter.
   *          It must contain the objectClass as well.
   * @return the registered instance of <code>service</code>, or
   *         <code>null</code>.
   * @throws IllegalStateException
   *           if there is more than one instance of the service registered.
   */
  public static <T> T getService(Class<T> service, String filter) {
    return getService(service, false, filter);
  }

  /**
   * Returns the currently registered instance of the given OSGi
   * <code>service</code> interface.
   *
   * @param <T>
   *          type parameter representing an OSGi service interface.
   * @param service
   *          the OSGi service interface for which an instance is to be
   *          returned.
   * @return the registered instance of <code>service</code>.
   * @throws IllegalStateException
   *           if there is none or more than one instance of the service
   *           registered.
   */
  public static <T> T getRequiredService(Class<T> service) {
    return getService(service, true, null);
  }

  /**
   * Returns all instances of a given OSGi <code>service</code>.
   *
   * @param <T>
   *          type parameter representing an OSGi service interface.
   * @param service
   *          the OSGi service interface for which instances are to be returned.
   * @return a set of service instances.
   */
  public static <T> Set<T> getServices(Class<T> service) {
    return getServices(service, null, null);
  }

  /**
   * Returns all instances of a given OSGi <code>service</code>.
   *
   * @param <T>
   *          type parameter representing an OSGi service interface.
   * @param service
   *          the OSGi service interface for which instances are to be returned.
   * @param comparator the comparator that will be used to order this set.
   *        If {@code null}, the {@linkplain Comparable natural
   *        ordering} of the elements will be used.
   * @return a set of service instances.
   */
  public static <T> Set<T> getServices(Class<T> service, Comparator<? super T> comparator) {
    return getServices(service, null, comparator);
  }

  /**
   * Returns instances of a given OSGi <code>service</code> matching
   * a certain filter condition.
   *
   * @param <T>
   *          type parameter representing an OSGi service interface.
   * @param service
   *          the OSGi service interface for which instances are to be returned.
   * @param filter
   *          a filter that determines which instances of the service to
   *          accept, or <code>null</code>. In no filter is specified, all
   *          instances of the service are returned.
   * @return a set of matching service instances.
   */
  public static <T> Set<T> getServices(Class<T> service, ServiceFilter<T> filter) {
    return getServices(service, filter, null);
  }

  /**
   * Returns instances of a given OSGi <code>service</code> matching
   * a certain filter condition.
   *
   * @param <T>
   *          type parameter representing an OSGi service interface.
   * @param serviceClass
   *          the OSGi service interface for which instances are to be returned.
   * @param filter
   *          a filter that determines which instances of the service to
   *          accept, or <code>null</code>. In no filter is specified, all
   *          instances of the service are returned.
   * @param comparator the comparator that will be used to order this set.
   *        If {@code null}, the {@linkplain Comparable natural
   *        ordering} of the elements will be used.
   * @return a set of matching service instances.
   */
  public static <T> Set<T> getServices(Class<T> serviceClass, ServiceFilter<T> filter, Comparator<? super T> comparator) {
    if (comparator == null) {
      comparator = new DefaultComparator<T>();
    }
    BundleContext context = getBundleContext();
    Set<T> serviceInstances = new TreeSet<T>(comparator);
    List<ServiceReference> serviceRefs = getServiceReferences(serviceClass);
    if (serviceRefs != null) {
      for (ServiceReference serviceRef : serviceRefs) {
        T serviceInstance = serviceClass.cast(context.getService(serviceRef));
        if (serviceInstance != null && (filter == null || filter.accept(serviceInstance))) {
          serviceInstances.add(serviceInstance);
        }
      }
    }
    return serviceInstances;
  }

  private static <T> List<ServiceReference> getServiceReferences(Class<T> service) {
    List<ServiceReference> ret = new LinkedList<ServiceReference>();
    try {
      BundleContext context = getBundleContext();
      ServiceReference[] serviceRefs = context.getAllServiceReferences(service.getName(), null);
      if (serviceRefs != null) {
        ret.addAll(Arrays.asList(serviceRefs));
      }
    } catch (InvalidSyntaxException e) {
      throw new IllegalArgumentException(e);
    }
    return ret;
  }

  /**
   * Returns the {@link ExtensionService} instance matching a given model extension class.
   *
   * @param extensionClass  the model extension class.
   * @return  the extension service instance, or <code>null</code> if there is no instance
   * for the given model extension class available.
   */
  @SuppressWarnings("unchecked")
  public static <T extends ExtensionEntityBase> ExtensionService<T> getExtensionService(Class<T> extensionClass) {
    BundleContext context = getBundleContext();
    String extensionClassName = extensionClass.getName();
    TreeSet<ExtensionService<T>> extensionServices = new TreeSet<ExtensionService<T>>();
    List<ServiceReference> serviceRefs = getServiceReferences(ExtensionService.class);
    if (serviceRefs != null) {
      for (ServiceReference serviceRef : serviceRefs) {
        ExtensionService<?> serviceInstance = (ExtensionService<?>)context.getService(serviceRef);
        if (serviceInstance != null && extensionClassName.equals(serviceInstance.getExtensionClass().getName())) {
          extensionServices.add((ExtensionService<T>)serviceInstance);
        }
      }
    }
    if (extensionServices.size() > 1) {
      throw new IllegalStateException("More than one extension service registered for model extension " + extensionClassName);
    }
    return extensionServices.isEmpty()? null : extensionServices.first();
  }

  /**
   * Returns the {@link Bundle}s that are currently providing the given OSGi <code>service</code>.
   *
   * @param <T>
   *          type parameter representing an OSGi service interface.
   * @param service
   *          the OSGi service interface for which an instance is to be
   *          returned.
   * @return the bundles or an empty set if there is none registered.
   */
  public static <T> Set<Bundle> getBundlesProvidingService(Class<T> service) {
    Set<Bundle> ret = new HashSet<Bundle>();
    List<ServiceReference> serviceRefs = getServiceReferences(service);
    if (serviceRefs != null) {
      for (ServiceReference serviceRef : serviceRefs) {
        ret.add(serviceRef.getBundle());
      }
    }
    return ret;
  }


  /**
   * Comparator that sorts OSGi service instances according to their class name.
   */
  private static class DefaultComparator<T> implements Comparator<T> {
    @Override
    public int compare(T o1, T o2) {
      return o1.getClass().getName().compareTo(o2.getClass().getName());
    }
  }

  /**
   * Returns an interator for all implementations of the given OSGi <code>service</code>.
   * @param <T> type parameter representing an OSGi service interface.
   * @param serviceClass  the OSGi service interface for which implementations are
   * to be returned.
   */
  public static <T> Iterator<T> getServiceIterator(Class<T> serviceClass) {
    BundleContext context = getBundleContext();
    try {
      ServiceReference[] serviceRefs = context.getAllServiceReferences(serviceClass.getName(), null);
      if (serviceRefs != null) {
        return new ServiceIterator<T>(serviceClass, serviceRefs, context);
      }
    } catch (InvalidSyntaxException e) {
      throw new IllegalArgumentException(e);
    }
    return new ServiceIterator<T>(serviceClass, null, context);
  }

  /**
   *
   * @return
   */
  public static List<Bundle> getBundles() {
    BundleContext context = getBundleContext();
    List<Bundle> bundles = Arrays.asList(context.getBundles());
    Collections.sort(bundles, new Comparator<Bundle>() {
      @Override
      public int compare(Bundle o1, Bundle o2) {
        return o1.getSymbolicName().compareTo(o2.getSymbolicName());
      }
    });
    return bundles;
  }


  private static <T> T getService(Class<T> serviceClass, boolean required, String filter) {
    T serviceInstance = null;
    try {
      BundleContext context = getBundleContext();
      ServiceReference[] serviceRefs = context.getAllServiceReferences(serviceClass.getName(), filter);
      if (serviceRefs != null) {
        if (serviceRefs.length > 1) {
          throw new IllegalStateException("ERROR: Multiple implementations for service " + serviceClass.getName() + " registered");
        }
        serviceInstance = serviceClass.cast(context.getService(serviceRefs[0]));
      }
    } catch (InvalidSyntaxException e) {
      throw new IllegalArgumentException(e);
    }
    if (required && serviceInstance == null) {
      throw new IllegalStateException("ERROR: No implementation for required service " + serviceClass.getName() + " registered");
    }
    return serviceInstance;
  }

  private static final BundleContext getBundleContext() {
    return FrameworkUtil.getBundle(Services.class).getBundleContext();
  }

  private static class ServiceIterator<T> implements Iterator<T> {
    private int i = 0;
    private final Class<T> serviceClass;
    private final BundleContext context;
    private final ServiceReference[] serviceRefs;

    public ServiceIterator(Class<T> serviceClass, ServiceReference[] serviceRefs, BundleContext context) {
      this.serviceClass = serviceClass;
      this.context = context;
      this.serviceRefs = serviceRefs;
    }

    @Override
    public boolean hasNext() {
      return serviceRefs != null? i < serviceRefs.length : false;
    }

    @Override
    public T next() {
      if (!hasNext()) {
        throw new NoSuchElementException("ERROR: Iteration has no more elements");
      }
      return serviceClass.cast(context.getService(serviceRefs[i++]));
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("ERROR: Remove operation is not supported by this Iterator");
    }
  }

}

