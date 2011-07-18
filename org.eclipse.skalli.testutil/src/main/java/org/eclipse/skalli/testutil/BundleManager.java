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

import java.util.Dictionary;

import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;

public class BundleManager {

    private final Class<?> hintClass;

    public BundleManager(Class<?> hintClass) {
        this.hintClass = hintClass;
    }

    public void startBundles() throws BundleException {
        Bundle bundle = FrameworkUtil.getBundle(hintClass);
        bundle.start();
        for (Bundle b : bundle.getBundleContext().getBundles()) {
            try {
               if (b.getSymbolicName().startsWith("org.eclipse.skalli") //$NON-NLS-1$
                        && !b.getSymbolicName().endsWith(".test") //$NON-NLS-1$
                        && !isFragment(b)
                        && b.getState() != Bundle.ACTIVE) {
                    b.start();
                }
            } catch (BundleException e) {
                // ignore that: if a bundle required by a test cannot be started
                // the test should fail anyway
            }
        }
    }

    private boolean isFragment(Bundle b) {
        Dictionary<String,String> headers = b.getHeaders();
        return StringUtils.isNotBlank(headers.get("Fragment-Host")); //$NON-NLS-1$
    }
}
