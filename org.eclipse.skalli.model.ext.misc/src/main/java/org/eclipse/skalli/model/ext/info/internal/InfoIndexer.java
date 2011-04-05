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
package org.eclipse.skalli.model.ext.info.internal;

import org.eclipse.skalli.model.ext.AbstractIndexer;
import org.eclipse.skalli.model.ext.info.InfoProjectExt;

public class InfoIndexer extends AbstractIndexer<InfoProjectExt> {

  @Override
  protected void indexFields(InfoProjectExt infoProjectExt) {
    addField(InfoProjectExt.PROPERTY_PAGE_URL, infoProjectExt.getPageUrl(), true, false);
  }

}

