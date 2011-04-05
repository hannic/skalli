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
package org.eclipse.skalli.view.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.logging.Logger;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import org.eclipse.skalli.api.java.IconProvider;
import org.eclipse.skalli.log.Log;
import com.vaadin.terminal.StreamResource;

public final class ExtensionStreamSource implements StreamResource.StreamSource {

  private static final long serialVersionUID = -3343186536633039815L;
  private static final Logger LOG = Log.getLogger(ExtensionStreamSource.class);

  private final Class<? extends IconProvider> clazz;
  private final String path;

  public ExtensionStreamSource(Class<? extends IconProvider> clazz, String path) {
    this.clazz = clazz;
    this.path = path;
  }

  @Override
  public InputStream getStream() {
    Bundle bundle = FrameworkUtil.getBundle(clazz);
    if (bundle != null) {
      URL resource = bundle.getResource(path);
      if (resource != null) {
        try {
         return resource.openStream();
        } catch (IOException e) {
          LOG.warning("I/O problems while opening stream."); //$NON-NLS-1$
        }
      }
    }
    LOG.warning(MessageFormat.format("Could not load ''{0}'' from ''{1}'' of bundle ''{2}''", path, clazz.getName(), bundle.getSymbolicName())); //$NON-NLS-1$
    return null;
  }
}
