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
package org.eclipse.skalli.common.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

@SuppressWarnings("nls")
public class ComparatorUtilsTest {

    @Test
    public void testEquals() {
        assertTrue(ComparatorUtils.equals(null, null));
        assertFalse(ComparatorUtils.equals(null, ""));
        assertFalse(ComparatorUtils.equals("", null));
        assertTrue(ComparatorUtils.equals("hugo", "hugo"));
        assertFalse(ComparatorUtils.equals("hugo", "foobar"));
        assertFalse(ComparatorUtils.equals("hugo", new Integer(1)));
        assertFalse(ComparatorUtils.equals(new Integer(1), "hugo"));
        assertTrue(ComparatorUtils.equals(Arrays.asList("a", "b"), Arrays.asList("a", "b")));
    }

    @Test
    public void testCompareComparables() {
        assertTrue(ComparatorUtils.compare(null, null) == 0);
        assertTrue(ComparatorUtils.compare(null, "hugo") < 0);
        assertTrue(ComparatorUtils.compare("hugo", null) > 0);
        assertTrue(ComparatorUtils.compare("hugo", "hugo") == 0);
        assertTrue(ComparatorUtils.compare("a", "b") < 0);
        assertTrue(ComparatorUtils.compare("b", "a") > 0);
    }

    @Test
    public void testCompareAsStrings() throws Exception {
        Class<?> c = getClass();
        assertTrue(ComparatorUtils.compareAsStrings(null, null) == 0);
        assertTrue(ComparatorUtils.compareAsStrings(null, c) < 0);
        assertTrue(ComparatorUtils.compareAsStrings(c, null) > 0);
        assertTrue(ComparatorUtils.compareAsStrings(c, c) == 0);
        assertTrue(ComparatorUtils.compareAsStrings(c, "hugo") < 0);
        assertTrue(ComparatorUtils.compareAsStrings("hugo", c) > 0);
    }

}
