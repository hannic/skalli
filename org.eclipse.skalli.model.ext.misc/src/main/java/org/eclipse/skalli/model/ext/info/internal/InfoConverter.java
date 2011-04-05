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
package org.eclipse.skalli.model.ext.info.internal;

import org.eclipse.skalli.model.ext.AbstractConverter;
import org.eclipse.skalli.model.ext.info.InfoProjectExt;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class InfoConverter extends AbstractConverter<InfoProjectExt> {

  public static final String API_VERSION = "1.0"; //$NON-NLS-1$
  public static final String NAMESPACE = "http://xml.sap.com/2010/08/ProjectPortal/API/Extension-Info"; //$NON-NLS-1$

  public InfoConverter(String host) {
    super(InfoProjectExt.class, "info", host); //$NON-NLS-1$
  }

  @Override
  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    InfoProjectExt info = (InfoProjectExt) source;
    writeNode(writer, "homepage", info.getPageUrl()); //$NON-NLS-1$
    writeNode(writer, "mailingLists", "mailingList", info.getMailingLists()); //$NON-NLS-1$ //$NON-NLS-2$
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    return iterateNodes(null, reader, context);
  }

  private InfoProjectExt iterateNodes(InfoProjectExt ext, HierarchicalStreamReader reader, UnmarshallingContext context) {
    if (ext == null) {
      ext = new InfoProjectExt();
    }

    while (reader.hasMoreChildren()) {
      reader.moveDown();

      String field = reader.getNodeName();
      String value = reader.getValue();

      if ("mailingLists".equals(field) && reader.hasMoreChildren()) { //$NON-NLS-1$
        iterateNodes(ext, reader, context);
      } else if ("mailingList".equals(field)) { //$NON-NLS-1$
        ext.addMailingList(value);
      }else if ("homepage".equals(field)) { //$NON-NLS-1$
        ext.setPageUrl(value);
      }

      reader.moveUp();
    }
    return ext;
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
    return "extension-info.xsd"; //$NON-NLS-1$
  }

}

