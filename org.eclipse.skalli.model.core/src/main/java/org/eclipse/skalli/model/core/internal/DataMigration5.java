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

public class DataMigration5 extends AbstractDataMigration {

    public DataMigration5() {
        super(Project.class, 5);
    }

    @Override
    public void migrate(Document doc) throws ValidationException {
        XMLUtils.migrateStringToStringSet(doc, "mailingList", "mailingLists", false);
        XMLUtils.migrateStringToStringSet(doc, "scmLocation", "scmLocations", false);
    }

}
