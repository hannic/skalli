/*******************************************************************************
 * Copyright (c) 2010 - 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.nexus.internal;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.nexus.NexusClientException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class NexusResponseParser {

    static Node findNode(Element rootElement, String nodeName) throws NexusClientException {
        List<Node> nodes = new ArrayList<Node>();
        NodeList children = rootElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (nodeName.equals(node.getNodeName())) {
                nodes.add(node);
            }
        }

        if (nodes.size() == 0) {
            return null;
        } else if (nodes.size() > 1) {
            throw new NexusClientException("Root element '" + rootElement.getNodeName() + "' has " + nodes.size()
                    + " nodes with the name '" + nodeName + "'");
        }
        return nodes.get(0);
    }

    static String getNodeTextContent(Element rootElement, String nodeName)
            throws NexusClientException
    {
        Node versionNode = findNode(rootElement, nodeName);
        if (versionNode == null) {
            return null;
        }
        return versionNode.getTextContent();
    }

    static int getNodeTextContentAsInt(Element rootElement, String nodeName, int defaultValue) throws NexusClientException {
        int result;
        String fromStr = getNodeTextContent(rootElement, nodeName);
        if (StringUtils.isNotBlank(fromStr))
        {
            try {
                result = Integer.parseInt(fromStr);
            } catch (NumberFormatException e) {
                throw new NexusClientException("NodeContet '"+fromStr+ "' of node '"+nodeName+"' from Root element '" + rootElement.getNodeName() + "' is not an Integer");
            }
        }
        else {
            result = defaultValue;
        }
        return result;
    }

    static boolean getNodeTextContentAsBoolean(Element rootElement, String nodeName) throws NexusClientException {
        return BooleanUtils.toBoolean(getNodeTextContent(rootElement, nodeName));
    }

    static URL getNodeTextContentAsURL(Element rootElement, String nodeName)
            throws NexusClientException {
        String textContent = getNodeTextContent(rootElement, nodeName);
        if (StringUtils.isBlank(textContent)) {
            return null;
        }
        try {
            return new URL(textContent);
        } catch (MalformedURLException e) {
            throw new NexusClientException("The textContent = '" + textContent + "' for the node with name '"
                    + nodeName + "' is not a valid URL.", e);
        }
    }

}
