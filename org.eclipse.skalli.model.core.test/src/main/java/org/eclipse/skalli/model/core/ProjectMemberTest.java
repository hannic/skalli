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
package org.eclipse.skalli.model.core;

import java.util.Map;

import org.junit.Test;

import org.eclipse.skalli.testutil.PropertyHelper;
import org.eclipse.skalli.testutil.PropertyHelperUtils;

public class ProjectMemberTest {

    @Test
    public void testPropertyDefinitions() throws Exception {
        Map<String, Object> values = PropertyHelperUtils.getValues();
        values.put(ProjectMember.PROPERTY_USERID, "homer");

        Map<Class<?>, String[]> requiredProperties = PropertyHelperUtils.getRequiredProperties();
        requiredProperties.put(ProjectMember.class, new String[] { ProjectMember.PROPERTY_USERID });

        PropertyHelper.checkPropertyDefinitions(ProjectMember.class, requiredProperties, values);
    }

}
