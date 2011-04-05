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
package org.eclipse.skalli.core.internal.event;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.osgi.service.component.ComponentContext;

import org.eclipse.skalli.api.java.EventListener;
import org.eclipse.skalli.api.java.EventService;
import org.eclipse.skalli.api.java.events.AbstractEvent;
import org.eclipse.skalli.log.Log;

/**
 * Beware: This eventing implementation is just a POC!!!
 *
 * @author d049863
 */
// TODO: just a POC...
public class EventComponent implements EventService {
  private static final Logger LOG = Log.getLogger(EventComponent.class);

  private final Map<String, Set> listeners = new HashMap<String, Set>();

  protected void activate(ComponentContext context){
    LOG.info("Event service activated");
  }

  protected void deactivate(ComponentContext context) {
    LOG.info("Event service deactivated");
  }


  @Override
  public <T extends AbstractEvent> void registerListener(Class<T> event, EventListener<T> listener) {
    Set<EventListener<T>> set = listeners.get(event.getName());
    if (set == null) {
      set = new HashSet<EventListener<T>>(1);
      listeners.put(event.getName(), set);
    }
    set.add(listener);
  }

  @Override
  public <T extends AbstractEvent> void unregisterEventListener(Class<T> event, EventListener<T> listener) {
    Set<EventListener<T>> set = listeners.get(event.getName());
    if (set != null) {
      set.remove(listener);
    }
  }

  @Override
  public <T extends AbstractEvent> void fireEvent(T event) {
    Set<EventListener<T>> set = listeners.get(event.getClass().getName());
    if (set != null) {
      for (EventListener<T> listener : set) {
        listener.onEvent(event);
      }
    }
  }


}

