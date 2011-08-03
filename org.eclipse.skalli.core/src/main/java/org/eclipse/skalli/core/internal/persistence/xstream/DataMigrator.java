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
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.eclipse.skalli.common.util.XMLUtils;
import org.eclipse.skalli.log.Log;
import org.eclipse.skalli.model.ext.DataMigration;
import org.eclipse.skalli.model.ext.ValidationException;
import org.w3c.dom.Document;

public class DataMigrator {
    private static final Logger LOG = Log.getLogger(DataMigrator.class);

    private final List<DataMigration> migrations;
    private final Map<String, Class<?>> aliases;

    public DataMigrator(Set<DataMigration> migrations, Map<String, Class<?>> aliases) {
        if (migrations != null) {
            this.migrations = new ArrayList<DataMigration>(migrations);
            Collections.sort(this.migrations);
        } else {
            this.migrations = null;
        }
        if (aliases != null) {
            this.aliases = aliases;
        } else {
            this.aliases = Collections.emptyMap();
        }
    }

    public void migrate(Document doc, int fromVersion, int toVersion) throws ValidationException {
        if (migrations == null) {
            return;
        }
        if (fromVersion >= toVersion) {
            return;
        }
        if (doc == null) {
            return;
        }
        String nodeName = doc.getDocumentElement().getNodeName();
        String className = aliases.containsKey(nodeName)? aliases.get(nodeName).getName() : nodeName;
        for (int i = fromVersion; i < toVersion; i++) {
            for (DataMigration migration : migrations) {
                if (migration.getFromVersion() == i && migration.handlesType(className)) {
                    LOG.info(MessageFormat.format("Migrating {0} with {1}", XMLUtils.getUuid(doc), migration
                            .getClass().getName()));
                    migration.migrate(doc);
                }
            }
        }
    }

}
