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
package org.eclipse.skalli.api.rest.internal.resources;

import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.ext.AbstractConverter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

class ProjectsConverter extends AbstractConverter<Projects> {

  private String[] extensions;

  public ProjectsConverter(String host, String[] extensions) {
    super(Projects.class, "projects", host); //$NON-NLS-1$
    this.extensions = extensions;
  }

  @Override
  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    writer.addAttribute("xmlns", getNamespace()); //$NON-NLS-1$
    writer.addAttribute("xmlns:xsi", XSI_INSTANCE_NS); //$NON-NLS-1$
    writer.addAttribute("xsi:schemaLocation", getNamespace() + " " + getHost() + URL_SCHEMAS + getXsdFileName()); //$NON-NLS-1$ //$NON-NLS-2$
    writer.addAttribute("apiVersion", getApiVersion()); //$NON-NLS-1$
    for (Project project: ((Projects)source).getProjects()) {
      writer.startNode("project"); //$NON-NLS-1$
      new ProjectConverter(getHost(), extensions, true).marshal(project, writer, context);
      writer.endNode();
    }
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader arg0, UnmarshallingContext arg1) {
    // don't support that yet
    return null;
  }

  @Override
  public String getApiVersion() {
    return ProjectConverter.API_VERSION;
  }

  @Override
  public String getNamespace() {
    return ProjectConverter.NAMESPACE;
  }

  @Override
  public String getXsdFileName() {
    return "projects.xsd"; //$NON-NLS-1$
  }
}

