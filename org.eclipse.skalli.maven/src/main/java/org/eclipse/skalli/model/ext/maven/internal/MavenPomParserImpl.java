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

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.eclipse.skalli.common.util.XMLUtils;
import org.eclipse.skalli.model.ext.maven.MavenCoordinate;

public class MavenPomParserImpl implements MavenPomParser {

  private static final String MODULE = "module"; //$NON-NLS-1$
  private static final String RELATIVE_PATH = "relativePath"; //$NON-NLS-1$
  private static final String PARENT = "parent"; //$NON-NLS-1$
  private static final String PROJECT = "project"; //$NON-NLS-1$
  private static final String PACKAGING = "packaging"; //$NON-NLS-1$
  private static final String ARTIFACT_ID = "artifactId"; //$NON-NLS-1$
  private static final String GROUP_ID = "groupId"; //$NON-NLS-1$

  @Override
  public MavenPom parse(InputStream in) throws IOException, MavenValidationException {
    Document document;
    try {
      document = XMLUtils.documentFromStream(in);
    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e);
    } catch (SAXException e) {
      throw new MavenValidationException(e);
    }
    return parse(document);
  }

  private MavenPom parse(Document document) {
    MavenPom result = new MavenPom();

    MavenCoordinate self = parseMavenCoordinate(document, PROJECT);
    result.setSelf(self);

    if (elementExists(document, PARENT)) {
      MavenCoordinate parent = parseMavenCoordinate(document, PARENT);
      result.setParent(parent);

      String relativePath= extractTextContent(document, PARENT, RELATIVE_PATH);
      result.setParentRelativePath(relativePath);
    }

    NodeList modules = document.getElementsByTagName(MODULE);
    for (int i=0; i< modules.getLength(); ++i) {
      result.getModuleTags().add(modules.item(i).getTextContent());
    }

    return result;
  }

  private MavenCoordinate parseMavenCoordinate(Document document, String parentTagName) {
    MavenCoordinate coordinate = new MavenCoordinate();
    String groupId = extractTextContent(document, parentTagName, GROUP_ID);
    coordinate.setGroupId(groupId);

    String artifactId = extractTextContent(document, parentTagName, ARTIFACT_ID);
    coordinate.setArtefactId(artifactId);

    String packaging = extractTextContent(document, parentTagName, PACKAGING);
    coordinate.setPackaging(packaging);
    return coordinate;
  }

  private boolean elementExists(Document document, String tagName) {
    return document.getElementsByTagName(tagName).getLength() > 0;
  }

  private String extractTextContent(Document document, String parentTagName, String tagName) {
    String textContent = null;
    NodeList parentNodes = document.getElementsByTagName(parentTagName);
    if (parentNodes.getLength() > 0) {
      Node parent = parentNodes.item(0);
      NodeList children = parent.getChildNodes();
      for (int i = 0; i < children.getLength(); i++) {
        Node node = children.item(i);
        if (tagName.equals(node.getNodeName())) {
          textContent = node.getTextContent();
          break;
        }
      }
    }
    return textContent;
  }
}

