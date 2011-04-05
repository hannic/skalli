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

import java.util.Collection;
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

  @PropertyName(position=0)
  public static final String PROPERTY_ISSUES = "issues"; //$NON-NLS-1$

  @PropertyName(position=1)
  public static final String PROPERTY_STALE = "stale"; //$NON-NLS-1$

  /**
   * The issues of the entity specified by {@link #getUuid()}.
   * Sorted with {@link Issue#compareTo(Issue)}
   */
  private TreeSet<Issue> issues = new TreeSet<Issue>();

  /**
   * Specifies whether the set of issues is stale and the corresponding
   * entity needs to validated again.
   */
  private boolean stale;

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
      for (Issue issue: issues) {
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

  public void clear() {
    getIssues().clear();
  }

  public boolean isStale() {
    return stale;
  }

  public void setStale(boolean stale) {
    this.stale = stale;
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
      for (Issue issue: issues) {
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
   *
   * @param message  the message to render as caption of the list, or <code>null</code>.
   * @param issues  the set of issues to render, or <code>null</code>.
   */
  @SuppressWarnings("nls")
  public static String asHTMLList(String message, Set<Issue> issues) {
    StringBuilder sb = new StringBuilder();
    if (StringUtils.isNotBlank(message)) {
      sb.append(message);
    }
    if (issues != null && issues.size() > 0) {
      sb.append("<ul>");
      for (Issue issue: issues) {
        sb.append("<li class=\"").append(issue.getSeverity().name()).append("\">");
        sb.append("<strong>").append(issue.getSeverity().name()).append(":</strong> ");
        sb.append(issue.getMessage());
        sb.append("</li>");
      }
      sb.append("</ul>");
    }
    return sb.toString();
  }
}

