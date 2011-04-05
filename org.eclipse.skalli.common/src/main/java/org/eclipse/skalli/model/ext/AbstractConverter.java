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
package org.eclipse.skalli.model.ext;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public abstract class AbstractConverter<T> implements AliasedConverter {

  public static final String XSI_INSTANCE_NS = "http://www.w3.org/2001/XMLSchema-instance"; //$NON-NLS-1$
  public static final String URL_SCHEMAS = "/schemas/"; //$NON-NLS-1$
  public static final String URL_API = "/api/"; //$NON-NLS-1$

  private final String host;
  private final String alias;
  private final Class<T> clazz;

  public AbstractConverter(Class<T> clazz, String alias, String host) {
    this.clazz = clazz;
    this.alias = alias;
    this.host = host;
  }

  @Override
  public String getAlias() {
    return alias;
  }

  @Override
  public Class<?> getConversionClass() {
    return clazz;
  }

  @Override
  public String getHost() {
    return host;
  }

  @Override
  public boolean canConvert(Class type) {
    return type.equals(clazz);
  }

  protected void writeLink(HierarchicalStreamWriter writer, String relation, String url) {
    writer.startNode("link");
    if (relation != null && !"".equals(relation)) {
      writer.addAttribute("rel", relation);
    }
    writer.addAttribute("href", url);
    writer.endNode();
  }

  protected void writeNode(HierarchicalStreamWriter writer, String nodeName) {
    writeNode(writer, nodeName, (String)null);
  }

  protected void writeNode(HierarchicalStreamWriter writer, String nodeName, long value) {
    writeNode(writer, nodeName, Long.toString(value));
  }

  protected void writeNode(HierarchicalStreamWriter writer, String nodeName, String value) {
    if (StringUtils.isNotBlank(value)) {
      writer.startNode(nodeName);
      writer.setValue(value);
      writer.endNode();
    }
  }

  protected void writeNode(HierarchicalStreamWriter writer, String nodeName, String itemName, Collection<String> values) {
    if (values != null && values.size() > 0) {
      writer.startNode(nodeName);
      for (String value: values) {
        writeNode(writer, itemName, value);
      }
      writer.endNode();
    }
  }
}

