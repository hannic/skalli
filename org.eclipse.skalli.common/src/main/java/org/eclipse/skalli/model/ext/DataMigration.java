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

import org.w3c.dom.Document;

/**
 * Interface representing the migration of a model DOM to
 * a more recent model version. Note, data migrations should
 * not implement this interface directly, but be derived from
 * {@ link AbstractDataMigration}.
 */
public interface DataMigration extends Comparable<DataMigration> {

    /**
     * Migrates the model entity represented by a model DOM.
     *
     * @throws ValidationException  if the migration failed because of
     * an invalid document model.
     */
    public void migrate(Document doc) throws ValidationException;

    /**
     * Returns the model version of a model entity to which this
     * migrator should be applied.
     */
    public int getFromVersion();

    /**
     * Returns true if this {@link DataMigration} is able to handle an {@link EntityBase} with the given class name.
     * @param className
     * @return
     */
    public boolean handlesType(String className);

}
