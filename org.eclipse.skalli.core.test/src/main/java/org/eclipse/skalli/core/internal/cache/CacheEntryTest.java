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
package org.eclipse.skalli.core.internal.cache;

import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class CacheEntryTest {

    @Test
    public void testAll() {
        CacheEntry<String, String> ce = new CacheEntry<String, String>("info", "value");

        Assert.assertEquals("getMetaInfo()", "info", ce.getMetaInfo());
        Assert.assertEquals("getValue()", "value", ce.getValue());

        ce.setMetaInfo("info2");
        Assert.assertEquals("getMetaInfo()", "info2", ce.getMetaInfo());
    }

}
