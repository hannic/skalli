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

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;

public class BundleManager {

  private final Class<?> hintClass;

  public BundleManager(Class<?> hintClass) {
    this.hintClass = hintClass;
  }

  public void startProjectPortalBundles() throws BundleException {
    Bundle bundle = FrameworkUtil.getBundle(hintClass);
    bundle.start();
    for (Bundle b : bundle.getBundleContext().getBundles()) {
      if (b.getSymbolicName().startsWith("com.sap.ldi") && !b.getSymbolicName().endsWith(".test") && b.getState() != Bundle.ACTIVE) { //$NON-NLS-1$ //$NON-NLS-2$
        b.start();
      }
    }
  }

}

