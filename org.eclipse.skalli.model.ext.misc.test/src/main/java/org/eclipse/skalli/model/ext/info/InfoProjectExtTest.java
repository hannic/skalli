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
package org.eclipse.skalli.model.ext.info;

import java.util.LinkedHashSet;
import java.util.Map;

import org.junit.Test;

import org.eclipse.skalli.testutil.PropertyHelper;
import org.eclipse.skalli.testutil.PropertyHelperUtils;

public class InfoProjectExtTest {

  private static final String PAGE_URL = "http://example.org/home"; //$NON-NLS-1$

  @Test
  public void testPropertyDefinitions() throws Exception {
    Map<String, Object> values = PropertyHelperUtils.getValues();

    values.put(InfoProjectExt.PROPERTY_PAGE_URL, PAGE_URL);
    LinkedHashSet<String> mailingLists = new LinkedHashSet<String>();
    mailingLists.add("homer@listserv.springfield.net");
    mailingLists.add("marge@listserv.springfield.net");
    values.put(InfoProjectExt.PROPERTY_MAILING_LIST, mailingLists);

    Map<Class<?>,String[]> requiredProperties = PropertyHelperUtils.getRequiredProperties();

    PropertyHelper.checkPropertyDefinitions(InfoProjectExt.class, requiredProperties, values);

  }
}

