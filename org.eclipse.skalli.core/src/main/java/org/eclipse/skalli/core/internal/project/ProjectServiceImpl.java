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
package org.eclipse.skalli.core.internal.project;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.api.java.EntityServiceImpl;
import org.eclipse.skalli.api.java.InvalidParentChainException;
import org.eclipse.skalli.api.java.NoSuchTemplateException;
import org.eclipse.skalli.api.java.PersistenceService;
import org.eclipse.skalli.api.java.ProjectNode;
import org.eclipse.skalli.api.java.ProjectService;
import org.eclipse.skalli.api.java.ProjectTemplateService;
import org.eclipse.skalli.api.java.SearchService;
import org.eclipse.skalli.api.java.authentication.UserUtil;
import org.eclipse.skalli.common.Services;
import org.eclipse.skalli.common.util.ProjectDescriptionValidator;
import org.eclipse.skalli.model.core.PeopleProvider;
import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.core.ProjectMember;
import org.eclipse.skalli.model.core.ProjectNature;
import org.eclipse.skalli.model.core.ProjectTemplate;
import org.eclipse.skalli.model.ext.ExtensibleEntityBase;
import org.eclipse.skalli.model.ext.ExtensionEntityBase;
import org.eclipse.skalli.model.ext.ExtensionService;
import org.eclipse.skalli.model.ext.ExtensionValidator;
import org.eclipse.skalli.model.ext.Issue;
import org.eclipse.skalli.model.ext.PropertyValidator;
import org.eclipse.skalli.model.ext.Severity;
import org.eclipse.skalli.model.ext.ValidationException;
import org.eclipse.skalli.model.ext.people.PeopleProjectExt;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectServiceImpl extends EntityServiceImpl<Project> implements ProjectService {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectServiceImpl.class);
    private ProjectTemplateService projectTemplateService;

    protected SearchService getSearchService() {
        return Services.getService(SearchService.class);
    }

    protected void activate(ComponentContext context) {
        LOG.info("Project Service activated"); //$NON-NLS-1$
    }

    protected void deactivate(ComponentContext context) {
        LOG.info("Project Service deactivated"); //$NON-NLS-1$
    }

    /**
     * Binds the required project template service.
     */
    protected void bindProjectTemplateService(ProjectTemplateService srvc) {
        this.projectTemplateService = srvc;
    }

    protected void unbindProjectTemplateService(ProjectTemplateService srvc) {
        this.projectTemplateService = null;
    }

    @Override
    public Class<Project> getEntityClass() {
        return Project.class;
    }

    @Override
    public Project getProjectByProjectId(String projectId) {
        for (Project p : getAll()) {
            if (p.getProjectId().equalsIgnoreCase(projectId)) {
                return p;
            }
        }
        return null;
    }

    @Override
    public List<Project> getProjects(Comparator<Project> c) {
        List<Project> projects = getAll();
        Collections.sort(projects, c);
        return projects;
    }

    @Override
    public List<Project> getProjects(List<UUID> uuids) {
        List<Project> result = new ArrayList<Project>();
        PersistenceService persistence = getPersistenceService();
        for (UUID uuid : uuids) {
            Project project = persistence.getEntity(Project.class, uuid);
            if (project != null) {
                result.add(project);
            }
        }
        return result;
    }

    @Override
    public List<Project> getSubProjects(UUID uuid) {
        List<Project> result = new ArrayList<Project>();
        for (Project p : getAll()) {
            if (uuid.equals(p.getParentProject())) {
                result.add(p);
            }
        }
        return result;
    }

    @Override
    public List<Project> getSubProjects(UUID uuid, Comparator<Project> c) {
        List<Project> result = getSubProjects(uuid);
        Collections.sort(result, c);
        return result;
    }

    @Override
    public List<Project> getParentChain(UUID uuid) {
        List<Project> result = new LinkedList<Project>();
        Project project = getByUUID(uuid);
        if (project != null) {
            result.add(project);
            UUID parentUUID = project.getParentProject();
            while (parentUUID != null) {
                Project parent = getByUUID(parentUUID);
                if (parent == null) {
                    throw new InvalidParentChainException(uuid, parentUUID);
                }
                result.add(parent);
                parentUUID = parent.getParentProject();
            }
        }
        return result;
    }

    @Override
    public Project getNearestParent(UUID uuid, ProjectNature nature) {
        UUID parentUUID = uuid;
        while (parentUUID != null) {
            Project parent = getByUUID(parentUUID);
            if (parent == null) {
                throw new InvalidParentChainException(uuid, parentUUID);
            }
            String templateId = parent.getProjectTemplateId();
            ProjectTemplate template = projectTemplateService.getProjectTemplateById(templateId);
            if (template == null) {
                throw new NoSuchTemplateException(parentUUID, templateId);
            }
            if (nature.equals(template.getProjectNature())) {
                return parent;
            }
            parentUUID = getByUUID(parentUUID).getParentProject();
        }
        return null;
    }

    @Override
    public List<Project> getProjectsForTag(String tag) {
        List<Project> result = new ArrayList<Project>();
        for (Project p : getAll()) {
            if (p.hasTag(tag)) {
                result.add(p);
            }
        }
        return result;
    }

    @Override
    public List<Project> getDeletedProjects() {
        return getPersistenceService().getDeletedEntities(Project.class);
    }

    @Override
    public List<Project> getDeletedProjects(Comparator<Project> c) {
        List<Project> projects = getDeletedProjects();
        Collections.sort(projects, c);
        return projects;
    }

    @Override
    public Project getDeletedProject(UUID uuid) {
        return getPersistenceService().getDeletedEntity(Project.class, uuid);
    }

    @Override
    public List<ProjectNode> getRootProjectNodes(Comparator<Project> c) {
        List<ProjectNode> rootNodes = new LinkedList<ProjectNode>();
        List<Project> projects = getProjects(c);
        for (Project project : projects) {
            if (project.getParentProject() == null) {
                ProjectNode projectNode = new ProjectNodeImpl(this, project, c);
                rootNodes.add(projectNode);
            }
        }
        return rootNodes;
    }

    @Override
    public ProjectNode getProjectNode(UUID uuid, Comparator<Project> c) {
        return new ProjectNodeImpl(this, uuid, c);
    }

    @Override
    protected void validateEntity(Project entity) throws ValidationException {
        SortedSet<Issue> issues = validate(entity, Severity.FATAL);
        if (issues.size() > 0) {
            throw new ValidationException("Project could not be saved due to the following reasons:", issues);
        }
    }

    /**
    * Validates the given project.
    * Checks basic data like project ID, template, project lead etc.
    * Furthermore, validates the project with the default property/extension validators provided by
    * <code>ExtensionServiceCore</code> and the extension services of the assigned extensions and
    * with the custom validators provided by the
    * {@link org.eclipse.skalli.model.core.ProjectTemplate project template}.
     */
    @Override
    protected SortedSet<Issue> validateEntity(Project project, Severity minSeverity) {
        SortedSet<Issue> issues = new TreeSet<Issue>();

        issues.addAll(validateProjectId(project));
        issues.addAll(validateProjectName(project));

        UUID projectUUID = project.getUuid();
        // soft description validation
        issues.addAll(new ProjectDescriptionValidator(Project.class, Project.PROPERTY_DESCRIPTION).validate(
                projectUUID, project.getDescription(), minSeverity));

        issues.addAll(validatePeopleExtension(project));

        // ensure that the entity service exists
        ExtensionService<?> extensionService = validateExtensionService(projectUUID, project, issues);

        // ensure that the project template exists
        ProjectTemplate projectTemplate = validateProjectTemplate(project, issues);

        if (extensionService != null && projectTemplate != null) {
            // use the validators provided by the project template/extension services to validate the project
            validateExtension(projectUUID, project, extensionService, projectTemplate, issues, minSeverity);

            // check that all extensions are compatible with the template and vice versa
            if (minSeverity.compareTo(Severity.ERROR) >= 0) {
                validateCompatibility(project, projectTemplate, extensionService, issues);
            }
        }
        return issues;
    }

    private SortedSet<Issue> validatePeopleExtension(Project project) {
        // ensure that the project has a PeopleProjectExt and a project lead
        SortedSet<Issue> issues = new TreeSet<Issue>();
        PeopleProjectExt peopleExtension = project.getExtension(PeopleProjectExt.class);
        if (peopleExtension == null) {
            issues.add(new Issue(Severity.FATAL, ProjectService.class, project.getUuid(),
                    PeopleProjectExt.class, null,
                    "Project must have a Project Members extension or inherit it from a parent"));
        } else if (peopleExtension.getLeads().isEmpty()) {
            issues.add(new Issue(Severity.FATAL, ProjectService.class, project.getUuid(),
                    PeopleProjectExt.class, PeopleProjectExt.PROPERTY_LEADS,
                    "Project must have a least one Project Lead"));
        }
        return issues;
    }

    private SortedSet<Issue> validateProjectId(Project project) {
        SortedSet<Issue> issues = new TreeSet<Issue>();
        String projectId = project.getProjectId();
        if (StringUtils.isBlank(projectId)) {
            issues.add(new Issue(Severity.FATAL, ProjectService.class, project.getUuid(),
                    Project.class, Project.PROPERTY_PROJECTID, 1, "Project must have a Project ID"));
        }
        else {
            if (projectId.trim().length() != projectId.length()) {
                issues.add(new Issue(Severity.FATAL, ProjectService.class, project.getUuid(),
                        Project.class, Project.PROPERTY_PROJECTID, 2, "Project ID must not have leading or trailing whitespaces"));
            }
            else {
                for (Project anotherProject : getAll()) {
                    String anotherProjectId = anotherProject.getProjectId();
                    if (projectId.equals(anotherProjectId) && !anotherProject.getUuid().equals(project.getUuid())) {
                        issues.add(new Issue(Severity.FATAL, ProjectService.class, project.getUuid(),
                                Project.class, Project.PROPERTY_PROJECTID, 3,
                                MessageFormat.format("Project with Project ID ''{0}'' already exists", projectId)));
                        break;
                    }
                }
            }
        }
        return issues;
    }

    private SortedSet<Issue> validateProjectName(Project project) {
        SortedSet<Issue> issues = new TreeSet<Issue>();
        String name = project.getName();
        if (StringUtils.isBlank(name)) {
            issues.add(new Issue(Severity.FATAL, ProjectService.class, project.getUuid(),
                    Project.class, Project.PROPERTY_NAME, "Projects must have a Display Name"));
        }
        return issues;
    }

    private ProjectTemplate validateProjectTemplate(Project project, Set<Issue> issues) {
        ProjectTemplate projectTemplate = projectTemplateService.getProjectTemplateById(project.getProjectTemplateId());
        if (projectTemplate == null) {
            issues.add(new Issue(Severity.FATAL, ProjectService.class, project.getUuid(),
                    MessageFormat.format(
                            "Project references project template ''{0}'' but such a template is not registered",
                            Project.class, Project.PROPERTY_TEMPLATEID,
                            project.getProjectTemplateId())));
        }
        return projectTemplate;
    }

    private ExtensionService<?> validateExtensionService(UUID projectUUID, ExtensionEntityBase ext, Set<Issue> issues) {
        Class<? extends ExtensionEntityBase> extensionClass = ext.getClass();
        ExtensionService<?> extensionService = Services.getExtensionService(extensionClass);
        if (extensionService == null) {
            issues.add(new Issue(Severity.FATAL, ProjectService.class, projectUUID,
                    MessageFormat.format("Project references model extension ''{0}'' but there is no " +
                            "corresponding extension service registered", extensionClass.getName())));
        }
        return extensionService;
    }

    private void validateCompatibility(Project project, ProjectTemplate projectTemplate,
            ExtensionService<?> extensionService, Set<Issue> issues) {
        Set<String> allowed = extensionService.getProjectTemplateIds();
        Set<String> included = projectTemplate.getIncludedExtensions();
        Set<String> excluded = projectTemplate.getExcludedExtensions();
        if (allowed != null || included != null || excluded != null) {
            UUID projectUUID = project.getUuid();
            for (ExtensionEntityBase extension : project.getAllExtensions()) {
                String extensionClassName = extension.getClass().getName();
                if (allowed != null && !allowed.contains(projectTemplate.getId())) {
                    issues.add(new Issue(
                            Severity.ERROR, ProjectTemplate.class, projectUUID, extension.getClass(), null,
                            MessageFormat.format("{0} projects are not compatible with ''{1}'' extensions. " +
                                    "Disable the extension or select another project template.",
                                            projectTemplate.getDisplayName(), extensionClassName)));
                }
                if (excluded != null && excluded.contains(extensionClassName) ||
                        included != null && !included.contains(extensionClassName)) {
                    issues.add(new Issue(
                            Severity.ERROR, ProjectTemplate.class, projectUUID, extension.getClass(), null,
                            MessageFormat.format("''{0}'' extensions are not appropriate for {1} projects. " +
                                    "Disable the extension or select another project template.",
                                            extensionClassName, projectTemplate.getDisplayName())));
                }
            }
        }
    }

    private void validateExtension(UUID projectUUID, ExtensionEntityBase ext, ProjectTemplate projectTemplate,
            Set<Issue> issues, Severity minSeverity) {
        ExtensionService<?> extensionService = validateExtensionService(projectUUID, ext, issues);
        if (extensionService != null) {
            validateExtension(projectUUID, ext, extensionService, projectTemplate, issues, minSeverity);
        }
    }

    private void validateExtension(UUID projectUUID, ExtensionEntityBase ext, ExtensionService<?> extensionService,
            ProjectTemplate projectTemplate, Set<Issue> issues, Severity minSeverity) {
        String extensionClassName = extensionService.getExtensionClass().getName();

        // first run all property validators and determine the captions of the UI fields
        Map<String, String> captions = new HashMap<String, String>();
        Set<String> propertyNames = ext.getPropertyNames();
        for (String propertyName : propertyNames) {
            String caption = projectTemplate.getCaption(extensionClassName, propertyName);
            if (caption != null) {
                captions.put(propertyName, caption);
            }
            Set<PropertyValidator> propertyValidators = new HashSet<PropertyValidator>();
            Set<PropertyValidator> defaultValidators = extensionService.getPropertyValidators(propertyName, caption);
            if (defaultValidators == null) {
                LOG.warn(MessageFormat.format(
                        "{0}#getPropertyValidators({1}) returned null, but is expected to return an empty set",
                        extensionService.getClass().getName(), propertyName));
            } else {
                propertyValidators.addAll(defaultValidators);
            }
            Set<PropertyValidator> customValidators = projectTemplate.getPropertyValidators(extensionClassName,
                    propertyName);
            if (customValidators == null) {
                LOG.warn(MessageFormat.format(
                        "{0}#getPropertyValidators({1}, {2}) returned null, but is expected to return an empty set",
                        projectTemplate.getClass().getName(), extensionClassName, propertyName));
            } else {
                propertyValidators.addAll(customValidators);
            }
            for (PropertyValidator propertyValidator : propertyValidators) {
                issues.addAll(propertyValidator.validate(projectUUID, ext.getProperty(propertyName), minSeverity));
            }
        }

        issues.addAll(validateExtensionServiceExtensionValidators(projectUUID, ext, extensionService, projectTemplate,
                minSeverity, captions));
        issues.addAll(validateProjectTemplateExtensionValidators(projectUUID, ext, projectTemplate, minSeverity));

        // if this extension is extensible, recursively validate all extensions
        if (ext instanceof ExtensibleEntityBase) {
            for (ExtensionEntityBase extension : ((ExtensibleEntityBase) ext).getAllExtensions()) {
                validateExtension(projectUUID, extension, projectTemplate, issues, minSeverity);
            }
        }
    }

    private Set<Issue> validateExtensionServiceExtensionValidators(UUID projectUUID, ExtensionEntityBase ext,
            ExtensionService<?> extensionService, ProjectTemplate projectTemplate, Severity minSeverity,
            Map<String, String> captions) {
        Set<Issue> issues = new TreeSet<Issue>();

        Set<? extends ExtensionValidator<?>> extensionValidators = extensionService.getExtensionValidators(captions);
        if (extensionValidators == null) {
            LOG.warn(MessageFormat.format(
                    "{0}#getExtensionValidators() returned null, but is expected to return an empty set",
                    projectTemplate.getClass().getName()));
        } else {
            for (ExtensionValidator<?> extensionValidator : extensionValidators) {
                issues.addAll(extensionValidator.validate(projectUUID, ext, minSeverity));
            }
        }
        return issues;
    }

    private Set<Issue> validateProjectTemplateExtensionValidators(UUID uuid, ExtensionEntityBase ext,
            ProjectTemplate projectTemplate, Severity minSeverity) {
        Set<Issue> issues = new TreeSet<Issue>();
        Set<ExtensionValidator<?>> extentionValidators = projectTemplate.getExtensionValidators(ext.getClass()
                .getName());
        if (extentionValidators == null)
        {
            LOG.warn(MessageFormat.format(
                    "{0}#getExtensionValidators({1}) returned null, but is expected to return an empty set",
                    projectTemplate.getClass().getName(), ext.getClass().getName()));
        }
        else {
            for (ExtensionValidator<?> extensionValidator : extentionValidators) {
                issues.addAll(extensionValidator.validate(uuid, ext, minSeverity));
            }
        }
        return issues;
    }

    @Override
    protected void updateSearchIndex(Project project) {
        SearchService searchService = getSearchService();
        if (searchService != null) {
            searchService.update(project);
            LOG.debug(MessageFormat.format("project {0} updated in search index", project.getProjectId()));
        } else {
            LOG.warn(MessageFormat.format("Failed to update search index - no instance of {0} available",
                    SearchService.class.getName()));
        }
    }

    @Override
    public Set<ProjectMember> getAllPeople(Project project) {
        Set<ProjectMember> ret = new TreeSet<ProjectMember>();
        for (PeopleProvider pp : Services.getServices(PeopleProvider.class)) {
            for (Set<ProjectMember> people : pp.getPeople(project).values()) {
                ret.addAll(people);
            }
        }
        return ret;
    }

    @Override
    public Map<String, Set<ProjectMember>> getAllPeopleByRole(Project project) {
        Map<String, Set<ProjectMember>> ret = new HashMap<String, Set<ProjectMember>>();
        for (PeopleProvider pp : Services.getServices(PeopleProvider.class)) {
            ret.putAll(pp.getPeople(project));
        }
        return ret;
    }

    @Override
    public Project createProject(final String templateId, final String userId) {
        final Project project = (StringUtils.isNotBlank(templateId)) ? new Project(templateId) : new Project();
        project.setUuid(UUID.randomUUID());

        if (StringUtils.isNotBlank(userId) && UserUtil.getUser(userId) != null) {
            final PeopleProjectExt peopleExt = new PeopleProjectExt();
            peopleExt.getLeads().add(new ProjectMember(userId));
            project.addExtension(peopleExt);
        }

        final ProjectTemplate template = projectTemplateService.getProjectTemplateById(templateId);

        if (template != null) {
            for (Class<? extends ExtensionEntityBase> extensionClass : projectTemplateService.getSelectableExtensions(
                    template, null)) {
                String extensionClassName = extensionClass.getName();
                if (template.isEnabled(extensionClassName)) {
                    try {
                        if (project.getExtension(extensionClass) == null) {
                            project.addExtension(extensionClass.cast(extensionClass.newInstance()));
                        }
                    } catch (InstantiationException e) {
                        LOG.warn(MessageFormat.format("Extension ''{0}'' could not be instantiated: {1}",
                                extensionClassName, e.getMessage()));
                    } catch (IllegalAccessException e) {
                        LOG.warn(MessageFormat.format("Extension ''{0}'' could not be instantiated: {1}",
                                extensionClassName, e.getMessage()));
                    }
                }
            }
        }

        return project;
    }
}