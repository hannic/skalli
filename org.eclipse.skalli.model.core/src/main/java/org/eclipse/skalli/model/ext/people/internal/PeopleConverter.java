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
package org.eclipse.skalli.model.ext.people.internal;

import org.eclipse.skalli.model.core.ProjectMember;
import org.eclipse.skalli.model.ext.AbstractConverter;
import org.eclipse.skalli.model.ext.people.PeopleProjectExt;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class PeopleConverter extends AbstractConverter<PeopleProjectExt> {

  public static final String API_VERSION = "1.0"; //$NON-NLS-1$
  public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/API/Extension-People"; //$NON-NLS-1$

  public PeopleConverter(String host) {
    super(PeopleProjectExt.class, "people", host); //$NON-NLS-1$
  }

  @Override
  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    PeopleProjectExt ext = (PeopleProjectExt) source;
    writer.startNode("leads"); //$NON-NLS-1$
    for (ProjectMember member : ext.getLeads()) {
      writer.startNode("lead"); //$NON-NLS-1$
      writeNode(writer, "userId", member.getUserID()); //$NON-NLS-1$
      writeLink(writer, "user", getHost() + URL_API + "user/" + member.getUserID()); //$NON-NLS-1$ //$NON-NLS-2$
      writer.endNode();
    }
    writer.endNode();
    writer.startNode("members"); //$NON-NLS-1$
    for (ProjectMember member : ext.getMembers()) {
      writer.startNode("member"); //$NON-NLS-1$
      writeNode(writer, "userId", member.getUserID()); //$NON-NLS-1$
      writeLink(writer, "user", getHost() + URL_API + "user/" + member.getUserID()); //$NON-NLS-1$ //$NON-NLS-2$
      writer.endNode();
    }
    writer.endNode();

  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    return iterateNodes(null, reader, context);
  }

  private PeopleProjectExt iterateNodes(PeopleProjectExt ext, HierarchicalStreamReader reader, UnmarshallingContext context) {
    if (ext == null) {
      ext = new PeopleProjectExt();
    }

    while (reader.hasMoreChildren()) {
      reader.moveDown();

      String field = reader.getNodeName();

      if ("leads".equals(field) && reader.hasMoreChildren()) { //$NON-NLS-1$
        iterateNodes(ext, reader, context);
      }
      else if ("members".equals(field) && reader.hasMoreChildren()) { //$NON-NLS-1$
        iterateNodes(ext, reader, context);
      }
      else if ("lead".equals(field) && reader.hasMoreChildren()) { //$NON-NLS-1$
        ext.addLead(unmarshalProjectMember(reader));
      }
      else if ("member".equals(field) && reader.hasMoreChildren()) { //$NON-NLS-1$
        ext.addMember(unmarshalProjectMember(reader));
      }

      reader.moveUp();
    }

    return ext;
  }

  private ProjectMember unmarshalProjectMember(HierarchicalStreamReader reader) {
    ProjectMember result = null;
    while (reader.hasMoreChildren()) {
      reader.moveDown();
      String field = reader.getNodeName();
      String value = reader.getValue();
      if ("userId".equals(field)) { //$NON-NLS-1$
        result = new ProjectMember(value);
      }
      reader.moveUp();
    }
    return result;
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
    return "extension-people.xsd"; //$NON-NLS-1$
  }

}

