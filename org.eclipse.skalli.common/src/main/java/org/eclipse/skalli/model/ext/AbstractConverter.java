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

  /** URL prefix for schema access. */
  protected static final String URL_SCHEMAS = "/schemas/"; //$NON-NLS-1$

  /** URL prefix for API access. */
  protected static final String URL_API = "/api/"; //$NON-NLS-1$

  private static final String XMLNS = "xmlns"; //$NON-NLS-1$
  private static final String XMLNS_XSI = "xmlns:xsi"; //$NON-NLS-1$
  private static final String XSI_INSTANCE_NS = "http://www.w3.org/2001/XMLSchema-instance"; //$NON-NLS-1$
  private static final String XSI_SCHEMA_LOCATION = "xsi:schemaLocation"; //$NON-NLS-1$

  private static final String MODIFIED_BY = "modifiedBy"; //$NON-NLS-1$
  private static final String LAST_MODIFIED = "lastModified"; //$NON-NLS-1$
  private static final String API_VERSION = "apiVersion"; //$NON-NLS-1$
  private static final String HREF = "href"; //$NON-NLS-1$
  private static final String REL = "rel"; //$NON-NLS-1$
  private static final String LINK = "link"; //$NON-NLS-1$

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

  protected void marshalNSAttributes(HierarchicalStreamWriter writer) {
    marshalNSAttributes(this, writer);
  }

  protected void marshalNSAttributes(AliasedConverter converter, HierarchicalStreamWriter writer) {
    writer.addAttribute(XMLNS, converter.getNamespace());
    writer.addAttribute(XMLNS_XSI, XSI_INSTANCE_NS);
    writer.addAttribute(XSI_SCHEMA_LOCATION, getSchemaLocationAttribute(converter));
  }

  private String getSchemaLocationAttribute(AliasedConverter converter) {
    return converter.getNamespace() + " " + converter.getHost() + URL_SCHEMAS + converter.getXsdFileName(); //$NON-NLS-1$
  }

  protected void marshalApiVersion(HierarchicalStreamWriter writer) {
    marshalApiVersion(this, writer);
  }

  protected void marshalApiVersion(AliasedConverter converter, HierarchicalStreamWriter writer) {
    writer.addAttribute(API_VERSION, converter.getApiVersion());
  }

  protected void marshalCommonAttributes(EntityBase entity, HierarchicalStreamWriter writer) {
    marshalCommonAttributes(entity, this, writer);
  }

  protected void marshalCommonAttributes(EntityBase entity, AliasedConverter converter, HierarchicalStreamWriter writer) {
    marshalApiVersion(converter, writer);
    String lastModified = entity.getLastModified();
    if (StringUtils.isNotBlank(lastModified)) {
      writer.addAttribute(LAST_MODIFIED, lastModified);
    }
    String modifiedBy = entity.getLastModifiedBy();
    if (StringUtils.isNotBlank(modifiedBy)) {
      writer.addAttribute(MODIFIED_BY, modifiedBy);
    }
  }

  protected void writeLink(HierarchicalStreamWriter writer, String relation, String url) {
    writer.startNode(LINK);
    if (StringUtils.isNotBlank(relation)) {
      writer.addAttribute(REL, relation);
    }
    writer.addAttribute(HREF, url);
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

