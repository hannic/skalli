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
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

/**
 * Collection of validation issues for a certain entity.
 * Validation issues are treated as separately persistable entities, but an <code>Issues</code>
 * instance is always associated with a certain associated entity.
 */
public class Issues extends EntityBase {

    private static final int NUMBER_LATEST_DURATIONS = 5;

    @PropertyName(position = 0)
    public static final String PROPERTY_ISSUES = "issues"; //$NON-NLS-1$

    @PropertyName(position = 1)
    public static final String PROPERTY_STALE = "stale"; //$NON-NLS-1$

    /**
     * The issues of the entity specified by {@link #getUuid()}.
     * Sorted with {@link Issue#compareTo(Issue)}
     */
    private TreeSet<Issue> issues = new TreeSet<Issue>();

    /**
     * Specifies whether the set of issues is stale and the corresponding
     * entity needs to be validated again.
     */
    private boolean stale;

    /**
     * The latest {@value NUMBER_LATEST_DURATIONS} durations to find the validation issues.
     * For the purpose of showing meaningful progress indicators in the UI and for monitoring.
     */
    private long[] latestDurations;

    /**
     * Creates an empty <code>Issues</code> instance.
     */
    public Issues() {
    }

    /**
     * Creates an <code>Issues</code> instance for the given entity.
     */
    public Issues(UUID entityId) {
        setUuid(entityId);
    }

    /**
     * Creates an <code>Issues</code> instance for the given
     * entity and adds the given issues.
     */
    public Issues(UUID entityId, Collection<Issue> issues) {
        this(entityId);
        setIssues(issues);
    }

    public synchronized SortedSet<Issue> getIssues() {
        if (issues == null) {
            issues = new TreeSet<Issue>();
        }
        return issues;
    }

    /**
     * Returns the issues with severity equal or greater than the given minimal severity.
     * The result set is sorted according to {@link Issue#compareTo(Issue)}.
     *
     * @param minSeverity  the minimal severity of issues to return in the result.
     * @return  a set of issues, or an empty set.
     */
    public SortedSet<Issue> getIssues(Severity minSeverity) {
        return getIssues(issues, minSeverity);
    }

    /**
     * Returns those issues from the given collection that have severity equal
     * or greater than the given minimal severity.
     * The result set is sorted according to {@link Issue#compareTo(Issue)}.
     *
     * @param issues  the collection tp filter.
     * @param minSeverity  the minimal severity of issues to return in the result.
     * @return  a set of issues, or an empty set.
     */
    public static SortedSet<Issue> getIssues(Collection<Issue> issues, Severity minSeverity) {
        TreeSet<Issue> result = new TreeSet<Issue>();
        if (minSeverity != null) {
            for (Issue issue : issues) {
                if (minSeverity.compareTo(issue.getSeverity()) >= 0) {
                    result.add(issue);
                }
            }
        }
        return result;
    }

    public synchronized void setIssues(Collection<Issue> c) {
        issues = new TreeSet<Issue>();
        issues.addAll(c);
    }

    public void addIssue(Issue issue) {
        if (issue != null) {
            getIssues().add(issue);
        }
    }

    public void removeIssue(Issue issue) {
        if (issue != null) {
            getIssues().remove(issue);
        }
    }

    public boolean hasIssue(Issue issue) {
        return getIssues().contains(issue);
    }

    public boolean hasIssues() {
        return getIssues().size() > 0;
    }

    public void clear() {
        getIssues().clear();
    }

    public boolean isStale() {
        return stale;
    }

    public void setStale(boolean stale) {
        this.stale = stale;
    }

    public long[] getLatestDurations() {
        if (latestDurations == null) {
            return new long[0];
        }
        return latestDurations;
    }

    public long getLatestDuration() {
        return latestDurations != null? getLatestDurations()[0] : -1L;
    }

    public void addLatestDuration(long duration) {
        if (latestDurations == null) {
            latestDurations = new long[NUMBER_LATEST_DURATIONS];
            Arrays.fill(latestDurations, duration);
            return;
        }
        for (int i = NUMBER_LATEST_DURATIONS-1; i > 0 ; --i) {
            latestDurations[i] = latestDurations[i-1];
        }
        latestDurations[0] = duration;
    }

    public long getAverageDuration() {
        if (latestDurations == null) {
            return -1L;
        }
        long sum = 0;
        for (int i = 0; i < NUMBER_LATEST_DURATIONS; ++i) {
            sum += latestDurations[i];
        }
        return sum / NUMBER_LATEST_DURATIONS;
    }

    /**
     * Composes a message from a given message and the {@link Issue#getMessage() detail messages
     * of the given issues. The messages of the issues are appended in form of a bulleted list
     * (using <tt>"-"</tt> as bullet) in the order defined by {@link Issue#compareTo(Issue)}.
     * If no explicit <code>message</code> is specified, then only the list of issue messages
     * is returned. If there is only a single issue given, then {@link Issue#getMessage()} is
     * returned without leading bullet.
     */
    @SuppressWarnings("nls")
    public static String getMessage(String message, SortedSet<Issue> issues) {
        StringBuilder sb = new StringBuilder();
        boolean hasMessage = StringUtils.isNotBlank(message);
        if (hasMessage) {
            sb.append(message);
        }
        if (issues != null) {
            int n = issues.size();
            int i = 0;
            for (Issue issue : issues) {
                message = issue.getMessage();
                if (StringUtils.isNotBlank(message)) {
                    if (hasMessage && i == 0 || i > 0) {
                        sb.append("\n");
                    }
                    if (hasMessage || n > 1) {
                        sb.append(" - ");
                    }
                    sb.append(message);
                    ++i;
                }
            }
        }
        return sb.toString();
    }

    /**
     * Renders the given message and set of issues as HTML bulleted list (&lt;ut&gt;) with
     * the message as caption. If not message is specified, only the bulleted list is rendered.
     * Example:
     * <ul>
     * <li style="color:#8a1f11;background-color:#fbe3e4;border-color:#fbc2c4;border-style:solid;border-width:1px;
     * margin: 2px 0px 2px 0px;list-style-type:none;padding:2px 2px 2px 15px;width:300px;min-height:20px;">
     * <b>ERROR</b>&nbsp;&nbsp;Description must not be empty</li>
     * </ul>
     *
     * @param message  the message to render as caption of the list, or <code>null</code>.
     * @param issues  the set of issues to render, or <code>null</code>.
     */
    public static String asHTMLList(String message, Set<Issue> issues) {
        return asHTMLList(message, issues, null);
    }

    /**
     * Renders the given message and set of issues as HTML bulleted list (&lt;ut&gt;) with
     * the message as caption. If not message is specified, only the bulleted list is rendered.
     * Example:
     * <ul>
     * <li style="color:#8a1f11;background-color:#fbe3e4;border-color:#fbc2c4;border-style:solid;border-width:1px;
     * margin: 2px 0px 2px 0px;list-style-type:none;padding:2px 2px 2px 15px;width:600px;min-height:20px;">
     * <b>ERROR</b>&nbsp;&nbsp;Development Infrastructure:&nbsp;SCM location is invalid</li>
     * </ul>
     * @param message  the message to render as caption of the list, or <code>null</code>.
     * @param issues  the set of issues to render, or <code>null</code>.
     * @param displayNames  the display names of extensions that are referenced by the given issues. If for
     * a given issue the display name of the extension it belongs to is known, the display name is
     * rendered (as link with <tt>href="#&lt;extensionName&gt;"</tt>) between the severity label an the
     * message of the issue.
     */
    @SuppressWarnings("nls")
    public static String asHTMLList(String message, Set<Issue> issues, Map<String,String> displayNames) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(message)) {
            sb.append(message);
        }
        if (issues != null && issues.size() > 0) {
            sb.append("<ul>");
            for (Issue issue : issues) {
                sb.append("<li class=\"").append(issue.getSeverity().name()).append("\">");
                sb.append("<strong>").append(issue.getSeverity().name()).append("</strong>&nbsp;&nbsp;");
                if (displayNames != null) {
                    Class<? extends ExtensionEntityBase> extension = issue.getExtension();
                    if (extension != null && displayNames.containsKey(extension.getName())) {
                        sb.append("<a href=\"#" + extension.getName()  +"\">");
                        sb.append(displayNames.get(extension.getName())).append("</a>:&nbsp;");
                    }
                }
                sb.append(issue.getMessage());
                sb.append("</li>");
            }
            sb.append("</ul>");
        }
        return sb.toString();
    }
}