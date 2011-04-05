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

import org.eclipse.skalli.common.util.XMLUtils;
import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.ext.AbstractDataMigration;
import org.eclipse.skalli.model.ext.ValidationException;
import org.eclipse.skalli.model.ext.maven.MavenProjectExt;
import org.eclipse.skalli.model.ext.maven.MavenReactorProjectExt;

public class DataMigration2 extends AbstractDataMigration {

  private static final String TAG_NAME = "mavenReactor"; //$NON-NLS-1$

  public DataMigration2() {
    super(Project.class, 1);
  }

  @Override
  public void migrate(Document doc) throws ValidationException {
    String sourceExtClassName = MavenProjectExt.class.getName();
    String targetExtClassName = MavenReactorProjectExt.class.getName();
    XMLUtils.moveTagToExtension(doc, sourceExtClassName, targetExtClassName, TAG_NAME, TAG_NAME);
  }

}

