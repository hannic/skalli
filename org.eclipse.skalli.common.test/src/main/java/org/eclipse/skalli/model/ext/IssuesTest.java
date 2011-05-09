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

import java.util.Arrays;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

import org.eclipse.skalli.common.util.CollectionUtils;
import org.eclipse.skalli.testutil.AssertUtils;
import org.eclipse.skalli.testutil.PropertyHelper;
import org.eclipse.skalli.testutil.PropertyHelperUtils;

@SuppressWarnings("nls")
public class IssuesTest implements Issuer {

    private static final Class<? extends Issuer> ISSUER = IssuesTest.class;

    private static Issue[] ISSUES = new Issue[] {
            new Issue(Severity.ERROR, ISSUER, PropertyHelperUtils.TEST_UUIDS[1]),
            new Issue(Severity.FATAL, ISSUER, PropertyHelperUtils.TEST_UUIDS[0]),
            new Issue(Severity.WARNING, ISSUER, PropertyHelperUtils.TEST_UUIDS[0]),
            new Issue(Severity.INFO, ISSUER, PropertyHelperUtils.TEST_UUIDS[0]),
            new Issue(Severity.ERROR, ISSUER, PropertyHelperUtils.TEST_UUIDS[0])
    };

    @Test
    public void testPropertyDefinitions() throws Exception {
        Map<String, Object> values = PropertyHelperUtils.getValues();
        Map<Class<?>, String[]> requiredProperties = PropertyHelperUtils.getRequiredProperties();
        values.put(Issues.PROPERTY_ISSUES, CollectionUtils.asSortedSet(ISSUES));
        values.put(Issues.PROPERTY_STALE, true);
        PropertyHelper.checkPropertyDefinitions(Issues.class, requiredProperties, values);
    }

    @Test
    public void testGetMessage() {
        TreeSet<Issue> issues = new TreeSet<Issue>();
        issues.add(new Issue(Severity.WARNING, ISSUER, PropertyHelperUtils.TEST_UUIDS[0], "IssueWarn"));
        issues.add(new Issue(Severity.ERROR, ISSUER, PropertyHelperUtils.TEST_UUIDS[1], "IssueError"));
        issues.add(new Issue(Severity.INFO, ISSUER, PropertyHelperUtils.TEST_UUIDS[2], "IssueInfo"));
        issues.add(new Issue(Severity.ERROR, ISSUER, PropertyHelperUtils.TEST_UUIDS[3], "IssueAnotherError"));
        Assert.assertEquals(
                "Message\n - IssueAnotherError\n - IssueError\n - IssueWarn\n - IssueInfo",
                Issues.getMessage("Message", issues));

        issues = new TreeSet<Issue>();
        issues.add(new Issue(Severity.WARNING, ISSUER, PropertyHelperUtils.TEST_UUIDS[0], ""));
        issues.add(new Issue(Severity.ERROR, ISSUER, PropertyHelperUtils.TEST_UUIDS[1], null));
        issues.add(new Issue(Severity.INFO, ISSUER, PropertyHelperUtils.TEST_UUIDS[2], "IssueInfo"));
        issues.add(new Issue(Severity.ERROR, ISSUER, PropertyHelperUtils.TEST_UUIDS[3], ""));
        Assert.assertEquals("Message\n" +
                " - Entity " + PropertyHelperUtils.TEST_UUIDS[3] + " is invalid\n" +
                " - Entity " + PropertyHelperUtils.TEST_UUIDS[1] + " is invalid\n" +
                " - Entity " + PropertyHelperUtils.TEST_UUIDS[0] + " is invalid\n" +
                " - IssueInfo",
                Issues.getMessage("Message", issues));

        issues = new TreeSet<Issue>();
        issues.add(new Issue(Severity.WARNING, ISSUER, PropertyHelperUtils.TEST_UUIDS[0], null));
        issues.add(new Issue(Severity.ERROR, ISSUER, PropertyHelperUtils.TEST_UUIDS[1], ""));
        Assert.assertEquals(
                " - Entity " + PropertyHelperUtils.TEST_UUIDS[1] + " is invalid\n" +
                        " - Entity " + PropertyHelperUtils.TEST_UUIDS[0] + " is invalid",
                Issues.getMessage("", issues));

        issues = new TreeSet<Issue>();
        issues.add(new Issue(Severity.FATAL, ISSUER, PropertyHelperUtils.TEST_UUIDS[0], null));
        Assert.assertEquals(
                "Message\n" +
                        " - Entity " + PropertyHelperUtils.TEST_UUIDS[0] + " is invalid",
                Issues.getMessage("Message", issues));

        issues = new TreeSet<Issue>();
        issues.add(new Issue(Severity.WARNING, ISSUER, PropertyHelperUtils.TEST_UUIDS[0], null));
        Assert.assertEquals(
                "Entity " + PropertyHelperUtils.TEST_UUIDS[0] + " is invalid",
                Issues.getMessage(null, issues));

        Assert.assertEquals("Message", Issues.getMessage("Message", new TreeSet<Issue>()));
        Assert.assertEquals("Message", Issues.getMessage("Message", null));
        Assert.assertEquals("", Issues.getMessage("", new TreeSet<Issue>()));
        Assert.assertEquals("", Issues.getMessage("", null));
    }

    @Test
    public void testGetIssues() {
        SortedSet<Issue> set = CollectionUtils.asSortedSet(ISSUES);
        Issues issues = new Issues(PropertyHelperUtils.TEST_UUIDS[0], set);
        AssertUtils.assertEquals("getIssues(WARNING)",
                Arrays.asList(ISSUES[1], ISSUES[0], ISSUES[4], ISSUES[2]),
                issues.getIssues(Severity.WARNING));
        AssertUtils.assertEquals("Issues.getIssues(WARNING)",
                Arrays.asList(ISSUES[1], ISSUES[0], ISSUES[4], ISSUES[2]),
                Issues.getIssues(set, Severity.WARNING));
        AssertUtils.assertEquals("getIssues(FATAL)",
                Arrays.asList(ISSUES[1]),
                issues.getIssues(Severity.FATAL));
        AssertUtils.assertEquals("getIssues(FATAL)",
                Arrays.asList(ISSUES[1]),
                issues.getIssues(Severity.FATAL));
        AssertUtils.assertEquals("getIssues(INFO)",
                Arrays.asList(ISSUES[1], ISSUES[0], ISSUES[4], ISSUES[2], ISSUES[3]),
                issues.getIssues(Severity.INFO));
        Assert.assertTrue(issues.getIssues(null).isEmpty());
    }
}
