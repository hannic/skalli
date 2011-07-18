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
package org.eclipse.skalli.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

import org.eclipse.skalli.model.ext.EntityBase;
import org.eclipse.skalli.testutil.PropertyHelper;

@SuppressWarnings("nls")
public class UserTest {

    @Test
    public void testPropertyDefinitions() throws Exception {
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put(User.PROPERTY_USERID, "homer");
        values.put(User.PROPERTY_FIRSTNAME, "Homer");
        values.put(User.PROPERTY_LASTNAME, "Simpson");
        values.put(User.PROPERTY_EMAIL, "homer@springfield.net");
        values.put(User.PROPERTY_FULL_NAME, "Homer Simpson");
        values.put(User.PROPERTY_DISPLAY_NAME, "Homer Simpson (homer)");
        values.put(EntityBase.PROPERTY_UUID, UUID.fromString("e4d78581-08da-4f04-8a90-a7dac41f6247"));
        values.put(EntityBase.PROPERTY_DELETED, Boolean.FALSE);

        Map<Class<?>, String[]> requiredProperties = Collections.emptyMap();

        PropertyHelper.checkPropertyDefinitions(User.class, requiredProperties, values);
    }

}
