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
package org.eclipse.skalli.testutil;

import java.util.SortedSet;

import org.junit.Assert;

import org.eclipse.skalli.model.ext.Issue;
import org.eclipse.skalli.model.ext.PropertyValidator;
import org.eclipse.skalli.model.ext.Severity;

public class ValidatorUtils {
    public static void assertIsValid(PropertyValidator validator, Object value) {
        SortedSet<Issue> issues = validator.validate(PropertyHelperUtils.TEST_UUIDS[0], value, Severity.ERROR);
        Assert.assertNotNull(issues);
        Assert.assertTrue(issues.isEmpty());
    }

    public static void assertNotValid(PropertyValidator validator, Object value, Severity expectedSeverity) {
        SortedSet<Issue> issues = validator.validate(PropertyHelperUtils.TEST_UUIDS[0], value, expectedSeverity);
        Assert.assertNotNull(issues);
        Assert.assertEquals(1, issues.size());
        Assert.assertEquals(expectedSeverity, issues.first().getSeverity());
    }
}
