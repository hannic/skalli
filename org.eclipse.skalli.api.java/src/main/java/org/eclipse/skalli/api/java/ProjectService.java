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
package org.eclipse.skalli.api.java;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.eclipse.skalli.model.core.PeopleProvider;
import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.core.ProjectMember;
import org.eclipse.skalli.model.core.ProjectNature;
import org.eclipse.skalli.model.ext.Issuer;

/**
 * Service that allows to retrieve projects, for example based on a project's
 * UUID, project identifier or by a part of its display name.
 */
public interface ProjectService extends EntityService<Project>, Issuer {

    /**
     * Returns a new project with all extensions defined in its template.
     *
     * @param templateId
     *          the template (ID) to use
     * @param userId
     *          the user that shall be added as project lead, might be null
     *
     * @return a project, never null
     */
    public Project createProject(String templateId, String userId);

    /**
     * Returns a sorted list of all currently existing projects.
     *
     * @param c
     *          the comparator to use to sort the result.
     *
     * @return  a list of projects, or an empty list.
     */
    public List<Project> getProjects(Comparator<Project> c);

    /**
     * Returns projects specified by a list of unique identifiers.
     * Note that the method ignores unique identifiers for which
     * no project entity is available. The result may therefore contain
     * less entries than the <code>uuids</code> list.
     *
     * @param uuids
     *          a list of project UUIDs (see
     *          {@link org.eclipse.skalli.model.core.Project#getUuid()}).
     *
     * @return  a list of projects matching the given unique identifiers.
     */
    public List<Project> getProjects(List<UUID> uuids);

    /**
     * returns a sorted list of root project nodes that can be used to
     * traverse the project hierarchy.
     *
     * @param c the comparator to use to sort the result list
     * @return sorted list of project nodes (see
     *          {@link org.eclipse.skalli.api.java.ProjectNode})
     */
    public List<ProjectNode> getRootProjectNodes(Comparator<Project> c);

    /**
     * returns a project node for a certain project UUID that can be used to
     * traverse the project hierarchy starting from this project
     *
     * @param uuid project UUID (see
     *          {@link org.eclipse.skalli.model.core.Project#getUuid()})
     * @param c the comparator to use to sort the child nodes of the returned
     *          <code>ProjectNode</code>
     * @return project node (see
     *          {@link org.eclipse.skalli.api.java.ProjectNode})
     */
    public ProjectNode getProjectNode(UUID uuid, Comparator<Project> c);

    /**
     * Returns all subprojects of the given project.
     * The order of the subprojects is not specified.
     *
     * @param uuid
     *          a project's UUID (see
     *          {@link org.eclipse.skalli.model.core.Project#getUuid()}).
     *
     * @return  projects that reference the given project as parent project, or an empty set.
     */
    public List<Project> getSubProjects(UUID uuid);

    /**
     * Returns a sorted list of subprojects of the given project.
     * This method is equivalent to calling {@link #getSubProjects(UUID, Comparator, int)} with
     * <code>depth</code> 1.
     *
     * @param uuid
     *          a project's UUID (see
     *          {@link org.eclipse.skalli.model.core.Project#getUuid()}).
     * @param c the comparator to use to sort the returned projects, or <code>null</code>
     *          if the order is not specified.
     *
     * @return  projects that reference the given project as parent project, or an empty set.
     */
    public List<Project> getSubProjects(UUID uuid, Comparator<Project> c);

    /**
     * Returns a sorted list of subprojects of the given project and depth of search.
     *
     * @param uuid
     *          a project's UUID (see
     *          {@link org.eclipse.skalli.model.core.Project#getUuid()}).
     * @param c the comparator to use to sort the returned projects, or <code>null</code>
     *          if the order is not specified.
     *  @param depth
     *          depth of subprojects tree. A <code>depth</code> of -1 is equivalent
     *          to {@link Integer#MAX_VALUE}.
     *
     * @return  projects that reference the given project as parent project (but not necessary direct parent),
     *          or an empty set.
     */
    public List<Project> getSubProjects(UUID uuid, Comparator<Project> c, int depth);

    /**
     * Returns the chain of parent projects of the given project.
     *
     * @param uuid
     *          a project's UUID (see
     *          {@link org.eclipse.skalli.model.core.Project#getUuid()}).
     *
     * @return  the parent projects of the project including the project itself
     *          (as first list entry).
     */
    public List<Project> getParentChain(UUID uuid);

    /**
     * Returns the first ("nearest") parent project in the project's parent chain
     * that matches the given project nature. Since the project is member of its own
     * parent chain (see {@link #getParentChain(UUID)}), this method will return
     * the project itself, if it matches the given project nature.
     *
     * @param uuid
     *          a project's UUID (see
     *          {@link org.eclipse.skalli.model.core.Project#getUuid()}).
     * @param nature
     *          the project nature to match.
     *
     * @return  the nearest parent project matching the given project nature,
     *          or the project itself, if it matches the given project nature.
     */
    public Project getNearestParent(UUID uuid, ProjectNature nature);

    /**
     * Returns the project with the given project identifier.
     *
     * @param projectId
     *          a project's project identifier
     *          (see {@link org.eclipse.skalli.model.core.Project#getProjectId()}).
     *
     * @return  the project with the given project identifier, or
     *          <code>null</code> if no such project exists.
     */
    public Project getProjectByProjectId(String projectId);

    /**
     * Returns all projects that are tagged with the given tag.
     * The order of the result is not specified.
     *
     * @param tag
     *          the tag to search for.
     *
     * @return  projects tagged with the given tag, or an empty list.
     */
    public List<Project> getProjectsForTag(String tag);

    /**
     * Returns all deleted projects.
     * The order of the result is not specified.
     *
     * @return  a list of deleted projects, or an empty list.
     */
    public List<Project> getDeletedProjects();

    /**
     * Returns a sorted list of all currently existing deleted projects.
     *
     * @param c
     *          the comparator to use to sort the result.
     *
     * @return  a list of deleted projects, or an empty list.
     */
    public List<Project> getDeletedProjects(Comparator<Project> c);

    /**
     * Returns the deleted project with the given unique identifier.
     *
     * @param uuid
     *          a project's UUID (see
     *          {@link org.eclipse.skalli.model.core.Project#getUuid()}).
     *
     * @return  the project with the given unique identifier, or <code>null</code>
     *          if no project with the given unique identifier exists at all or
     *          the project matching the given unique identifier is not marked as
     *          deleted.
     */
    public Project getDeletedProject(UUID uuid);

    /**
     * Returns all people involved in a {@link Project}.
     * <p>
     * This normally includes project leads and project members,
     * but can be extended to model extensions by providing a {@link PeopleProvider} implementation.
     * </p>
     * @param project
     * @return
     */
    public Set<ProjectMember> getAllPeople(Project project);

    /**
     * Returns all people involved in a {@link Project}.
     * <p>
     * This normally includes projectd leads and project members,
     * but can be extended to model extensions by providing a {@link PeopleProvider} implementation.
     * </p>
     * <p>
     * The people returned are structured by string representations of their roles in the project.
     * Therefore, if a person has multiple roles in a project, there will be several entries in the different sets accordingly.
     * </p>
     * @param project
     * @return
     */
    public Map<String, Set<ProjectMember>> getAllPeopleByRole(Project project);
}
