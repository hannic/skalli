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
package org.eclipse.skalli.model.ext.maven.internal;

import java.util.TreeSet;

import org.eclipse.skalli.model.ext.AbstractConverter;
import org.eclipse.skalli.model.ext.maven.MavenCoordinate;
import org.eclipse.skalli.model.ext.maven.MavenReactor;
import org.eclipse.skalli.model.ext.maven.MavenReactorProjectExt;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;


class MavenReactorConverter extends AbstractConverter<MavenReactorProjectExt> {

  public static final String API_VERSION = "1.0"; //$NON-NLS-1$
  public static final String NAMESPACE = "http://xml.sap.com/2010/08/ProjectPortal/API/Extension-MavenReactor"; //$NON-NLS-1$

  private static final String TAG_MODULE = "module"; //$NON-NLS-1$
  private static final String TAG_MODULES = "modules"; //$NON-NLS-1$
  private static final String TAG_PACKAGING = "packaging"; //$NON-NLS-1$
  private static final String TAG_ARTIFACTID = "artifactId"; //$NON-NLS-1$
  private static final String TAG_GROUPID = "groupId"; //$NON-NLS-1$
  private static final String TAG_COORDINATE = "coordinate"; //$NON-NLS-1$

  public MavenReactorConverter(String host) {
    super(MavenReactorProjectExt.class, "mavenReactor", host); //$NON-NLS-1$
  }

  @Override
  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    MavenReactorProjectExt ext = (MavenReactorProjectExt)source;
    MavenReactor reactor = ext.getMavenReactor();
    if (reactor != null) {
      MavenCoordinate reactorCoordinate = reactor.getCoordinate();
      if (reactorCoordinate != null) {
        writer.startNode(TAG_COORDINATE); // <mavenReactor>
        writeNode(writer, TAG_GROUPID, reactorCoordinate.getGroupId());
        writeNode(writer, TAG_ARTIFACTID, reactorCoordinate.getArtefactId());
        writeNode(writer, TAG_PACKAGING, reactorCoordinate.getPackaging());
        writer.endNode(); // </coordinate>
      }

      TreeSet<MavenCoordinate> modules = reactor.getModules();
      if (modules.size() > 0) {
        writer.startNode(TAG_MODULES); // <modules>
        for (MavenCoordinate moduleCoordinate : modules) {
          writer.startNode(TAG_MODULE); // <module>
          writeNode(writer, TAG_GROUPID, moduleCoordinate.getGroupId());
          writeNode(writer, TAG_ARTIFACTID, moduleCoordinate.getArtefactId());
          writeNode(writer, TAG_PACKAGING, moduleCoordinate.getPackaging());
          writer.endNode(); // </module>
        }
        writer.endNode(); // </modules>
      }
    }
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    // don't support that yet
    return null;
  }


  @Override
  public String getApiVersion() {
    return API_VERSION;
  }

  @Override
  public String getNamespace() {
    return NAMESPACE;
  }

  @Override
  public String getXsdFileName() {
    return "extension-maven-reactor.xsd"; //$NON-NLS-1$
  }
}

