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
package org.eclipse.skalli.api.rest.internal.resources;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.eclipse.skalli.api.java.ProjectService;
import org.eclipse.skalli.common.Services;
import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.core.ProjectMember;
import org.eclipse.skalli.model.ext.AbstractConverter;
import org.eclipse.skalli.model.ext.AliasedConverter;
import org.eclipse.skalli.model.ext.Derived;
import org.eclipse.skalli.model.ext.ExtensibleEntityBase;
import org.eclipse.skalli.model.ext.ExtensionEntityBase;
import org.eclipse.skalli.model.ext.ExtensionService;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public abstract class CommonProjectConverter extends AbstractConverter<Project> {

    private final static String URL_PROJECTS = URL_API + "projects/"; //$NON-NLS-1$
    private final static String URL_ISSUES = "/issues"; //$NON-NLS-1$
    private final static String URL_BROWSE = "/projects/"; //$NON-NLS-1$

    private final List<String> extensions;
    private boolean allExtensions;
    private boolean omitNSAttributes;

    public CommonProjectConverter(String host, boolean omitNSAttributes) {
        this(host, new String[] {}, omitNSAttributes);
        this.allExtensions = true;
    }

    public CommonProjectConverter(String host, String[] extensions, boolean omitNSAttributes) {
        super(Project.class, "project", host); //$NON-NLS-1$
        if (extensions != null) {
            this.extensions = Arrays.asList(extensions);
        } else {
            this.extensions = Collections.<String> emptyList();
        }
        this.allExtensions = false;
        this.omitNSAttributes = omitNSAttributes;
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        Project project = (Project) source;
        String host = getHost();
        if (!omitNSAttributes) {
            marshalNSAttributes(writer);
        }
        marshalCommonAttributes(project, writer);
        writeNode(writer, "uuid", project.getUuid().toString()); //$NON-NLS-1$
        writeNode(writer, "id", project.getProjectId()); //$NON-NLS-1$
        writeNode(writer, "template", project.getProjectTemplateId()); //$NON-NLS-1$
        writeNode(writer, "name", project.getName()); //$NON-NLS-1$
        writeNode(writer, "shortName", project.getShortName()); //$NON-NLS-1$
        writeLink(writer, "project", host + URL_PROJECTS + project.getUuid().toString()); //$NON-NLS-1$
        writeLink(writer, "browse", host + URL_BROWSE + project.getProjectId()); //$NON-NLS-1$
        writeLink(writer, "issues", host + URL_PROJECTS + project.getUuid().toString() + URL_ISSUES); //$NON-NLS-1$
        writeNode(writer, "phase", project.getPhase()); //$NON-NLS-1$
        writeNode(writer, "description", project.getDescription()); //$NON-NLS-1$
        UUID parent = project.getParentProject();
        if (parent != null) {
            writeLink(writer, "parent", host + URL_PROJECTS + parent.toString()); //$NON-NLS-1$
        }
        ProjectService projectService = Services.getRequiredService(ProjectService.class);
        List<Project> subprojects = projectService.getSubProjects(project.getUuid());
        if (subprojects.size() > 0) {
            writer.startNode("subprojects"); //$NON-NLS-1$
            for (Project subproject : subprojects) {
                writeLink(writer, "subproject", host + URL_PROJECTS + subproject.getUuid().toString()); //$NON-NLS-1$
            }
            writer.endNode();
        }

        marshalMembers(project, projectService, writer);
        marshalExtensions(project, writer, context);
    }

    private void marshalMembers(Project project, ProjectService projectService, HierarchicalStreamWriter writer) {
        if (allExtensions || extensions.contains("members")) { //$NON-NLS-1$
            writer.startNode("members"); //$NON-NLS-1$
            for (ProjectMember member : projectService.getAllPeople(project)) {
                writer.startNode("member"); //$NON-NLS-1$
                writeNode(writer, "userId", member.getUserID()); //$NON-NLS-1$
                writeLink(writer, "user", getHost() + URL_API + "user/" + member.getUserID()); //$NON-NLS-1$ //$NON-NLS-2$
                for (Entry<String, Set<ProjectMember>> entry : projectService.getAllPeopleByRole(project).entrySet()) {
                    if (entry.getValue().contains(member)) {
                        writeNode(writer, "role", entry.getKey()); //$NON-NLS-1$
                    }
                }
                writer.endNode();
            }
            writer.endNode();
        }
    }

    private void marshalExtensions(ExtensibleEntityBase extensibleEntity, HierarchicalStreamWriter writer,
            MarshallingContext context) {
        writer.startNode("extensions"); //$NON-NLS-1$
        Set<ExtensionService> extensionServices = getExtensionServices();
        for (ExtensionService extensionService : extensionServices) {
            if (allExtensions || extensions.contains(extensionService.getShortName())) {
                marshalExtension(extensibleEntity, extensionService, writer, context);
            }
        }
        writer.endNode();
    }

    private void marshalExtension(ExtensibleEntityBase extensibleEntity, ExtensionService<?> extensionService,
            HierarchicalStreamWriter writer, MarshallingContext context) {
        Class<? extends ExtensionEntityBase> extensionClass = extensionService.getExtensionClass();
        if (extensionClass.equals(Project.class)) {
            return;
        }
        ExtensionEntityBase extension = extensibleEntity.getExtension(extensionClass);
        AliasedConverter converter = extensionService.getConverter(getHost());
        if (extension != null && converter != null) {
            writer.startNode(extensionService.getShortName());
            marshalNSAttributes(converter, writer);
            marshalCommonAttributes(extension, converter, writer);
            writer.addAttribute("inherited", Boolean.toString(extensibleEntity.isInherited(extensionClass))); //$NON-NLS-1$
            writer.addAttribute("derived", Boolean.toString(extensionClass.isAnnotationPresent(Derived.class))); //$NON-NLS-1$
            context.convertAnother(extension, converter);
            writer.endNode();
        }
    }

    Set<ExtensionService> getExtensionServices() {
        return Services.getServices(ExtensionService.class);
    }
}
