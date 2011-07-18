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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.ext.AbstractDataMigration;
import org.eclipse.skalli.model.ext.maven.MavenProjectExt;

public class DataMigration1 extends AbstractDataMigration {

    public DataMigration1() {
        super(Project.class, 1);
    }

    private void remove(NodeList elements) {
        for (int i = 0; i < elements.getLength(); i++) {
            Node item = elements.item(i);
            item.getParentNode().removeChild(item);
        }
    }

    private void removeGAVs(NodeList elements) {
        for (int i = 0; i < elements.getLength(); i++) {
            Element node = (Element) elements.item(i);
            remove(node.getElementsByTagName("artifactID")); //$NON-NLS-1$
            remove(node.getElementsByTagName("groupID")); //$NON-NLS-1$
            remove(node.getElementsByTagName("version")); //$NON-NLS-1$
            remove(node.getElementsByTagName("id")); //$NON-NLS-1$
        }
    }

    @Override
    public void migrate(Document doc) {
        removeGAVs(doc.getElementsByTagName(MavenProjectExt.class.getName()));
    }

}
