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

import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;

import org.eclipse.skalli.common.Services;
import org.eclipse.skalli.model.ext.ExtensionService;
import org.eclipse.skalli.view.ext.ProjectInfoBox;

public class ViewBundleUtil {

    private static final String BUNDLE_VAADIN = "com.vaadin"; //$NON-NLS-1$

    public static ViewBundleUtil getDefault() {
        return new ViewBundleUtil();
    }

    /**
     * Scans all bundles providing an {@link ExtensionService} for resources
     * matching the given <code>path</code> and <code>pattern</code>.
     * @return a list of matching resources (which may be empty).
     * @see Bundle#findEntries(String, String, boolean)
     */
    @SuppressWarnings("unchecked")
    public static List<URL> findExtensionResources(String path, String pattern, boolean recursive) {
        List<URL> ret = new LinkedList<URL>();
        Set<Bundle> bundles = Services.getBundlesProvidingService(ExtensionService.class);
        for (Bundle bundle : bundles) {
            ret.addAll(getURLs(bundle.findEntries(path, pattern, recursive)));
        }
        return ret;
    }

    /**
     * Scans the Vaadin bundle, the view core bundle and all extensions
     * providing a {@link ProjectInfoBox} for theme resources
     * matching the given <code>path</code> and <code>pattern</code>.
     * @see Bundle#findEntries(String, String, boolean)
     */
    @SuppressWarnings("unchecked")
    public static List<URL> findThemeResources(String path, String pattern, boolean recursive) {
        BundleContext context = FrameworkUtil.getBundle(ViewBundleUtil.class).getBundleContext();
        Bundle vaadinBundle = null;
        if (vaadinBundle == null) {
            for (Bundle bundle : context.getBundles()) {
                // TODO hack - find better way to recognize the vaadin bundle (if not dump it at all)
                if (bundle.getSymbolicName().endsWith(BUNDLE_VAADIN)) {
                    vaadinBundle = bundle;
                }
            }
        }

        List<URL> ret = new LinkedList<URL>();

        // try vaadin bundle first
        ret.addAll(getURLs(vaadinBundle.findEntries(path, pattern, recursive)));

        // try view core bundle
        ret.addAll(getURLs(context.getBundle().findEntries(path, pattern, recursive)));

        // try view extension bundle(s)
        for (Bundle viewExtBundle : Services.getBundlesProvidingService(ProjectInfoBox.class)) {
            if (viewExtBundle != null) {
                ret.addAll(getURLs(viewExtBundle.findEntries(path, pattern, recursive)));
            }
        }

        return ret;
    }

    public static Version getVersion() {
        return FrameworkUtil.getBundle(ViewBundleUtil.class).getVersion();
    }

    private static List<URL> getURLs(Enumeration<URL> u) {
        List<URL> ret = new LinkedList<URL>();
        if (u != null) {
            while (u.hasMoreElements()) {
                URL url = u.nextElement();
                ret.add(url);
            }
        }
        return ret;
    }

}
