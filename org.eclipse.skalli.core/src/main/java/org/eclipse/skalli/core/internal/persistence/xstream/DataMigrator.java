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
package org.eclipse.skalli.core.internal.persistence.xstream;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.w3c.dom.Document;

import org.eclipse.skalli.log.Log;
import org.eclipse.skalli.model.ext.DataMigration;
import org.eclipse.skalli.model.ext.ValidationException;

public class DataMigrator {
    private static final Logger LOG = Log.getLogger(DataMigrator.class);

    private final List<DataMigration> migrations;

    public DataMigrator(Set<DataMigration> migrations) {
        if (migrations != null) {
            this.migrations = new ArrayList<DataMigration>(migrations);
            Collections.sort(this.migrations);
        } else {
            this.migrations = null;
        }
    }

    public void migrate(Document doc, int fromVersion, int toVersion, String fileName) throws ValidationException {
        if (migrations == null) {
            return;
        }
        if (fromVersion >= toVersion) {
            return;
        }
        String docType = doc.getDocumentElement().getNodeName();
        for (int i = fromVersion; i < toVersion; i++) {
            for (DataMigration migration : migrations) {
                if (migration.getFromVersion() == i && migration.handlesType(docType)) {
                    LOG.info(MessageFormat.format("Migrating {0} with {1}", fileName, migration.getClass().getName()));
                    migration.migrate(doc);
                }
            }
        }
    }

}
