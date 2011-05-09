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
package org.eclipse.skalli.model.core.internal;

import org.w3c.dom.Document;

import org.eclipse.skalli.common.util.XMLUtils;
import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.ext.AbstractDataMigration;
import org.eclipse.skalli.model.ext.ValidationException;

public class DataMigration13 extends AbstractDataMigration {

    public DataMigration13() {
        super(Project.class, 13);
    }

    /**
     * Changes from model version 13 -> 14:
     * <ol>
     *   <li>Project members/leads now in separate extension PeopleProjectExt</li>
     * </ol>
     */
    @Override
    public void migrate(Document doc) throws ValidationException {
        String extensionClassName = "org.eclipse.skalli.model.ext.people.PeopleProjectExt"; //$NON-NLS-1$
        XMLUtils.getOrCreateExtensionNode(doc, extensionClassName);
        XMLUtils.moveTagToExtension(doc, extensionClassName, "members"); //$NON-NLS-1$
        XMLUtils.moveTagToExtension(doc, extensionClassName, "leads"); //$NON-NLS-1$
    }
}
