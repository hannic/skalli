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
package org.eclipse.skalli.model.ext.maven;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Interface for an utility that provides URLs for resources in an SCM repository
 * specified by the location of the SCM repository and the path to the resource within
 * that repository. For example, for a Git repository with the SCM location
 * <tt>scm:git:git://git.example.org/project.git</tt> a suitable path resolver
 * could return URL pointing to a gitweb server:
 * <tt>http://git.example.org:50000/git/?p=project.git;a=blob_plain;f=pom.xml;hb=HEAD</tt>
 */
public interface MavenPathResolver {

    /**
     * Returns <code>true</code> if this path resolver is applicable to the given SCM location.
     * @param scmLocation  the SCM location to check.
     */
    public boolean canResolve(String scmLocation);

    /**
     * Provides the URL for a resource described by the given SCM location and path.
     * @param scmLocation  the location of the SCM repository where the resource is hosted.
     * @param relativePath  the path relative to the repository root of the reactor POM file
     * (without leading or trailing slashes and without file namne).
     *
     * @throws MalformedURLException  if the URL constructed from the given parameters is malformed.
     */
    public URL resolvePath(String scmLocation, String relativePath) throws MalformedURLException;

}
