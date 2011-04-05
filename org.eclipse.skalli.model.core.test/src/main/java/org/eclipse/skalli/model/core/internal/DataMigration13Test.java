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

import org.junit.Test;

import org.eclipse.skalli.testutil.MigrationTestUtil;

@SuppressWarnings("nls")
public class DataMigration13Test {

  @Test
  public void testMigrate() throws Exception {
    DataMigration13 migration = new DataMigration13();
    MigrationTestUtil.testMigration(migration, "projectportal");

    migration = new DataMigration13();
    MigrationTestUtil.testMigration(migration, "noextensions");
  }


}

