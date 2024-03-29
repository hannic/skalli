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
package org.eclipse.skalli.model.core;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.skalli.common.util.CollectionUtils;
import org.eclipse.skalli.model.ext.ExtensionEntityBase;
import org.eclipse.skalli.model.ext.ExtensionService;
import org.eclipse.skalli.model.ext.ExtensionValidator;
import org.eclipse.skalli.model.ext.PropertyValidator;

/**
 * Default implementation of a project template suitable for
 * generic projects. This template for example allows all model
 * extensions and defines a variety of project phases.
 */
public class DefaultProjectTemplate extends ProjectTemplateBase {

    /** Identifier of this template, see {@link #getId()} */
    public static final String ID = "default"; //$NON-NLS-1$

    private static final String TEMPLATE_DISPLAYNAME = "Free-Style Project";
    private static final String TEMPLATE_DESCRIPTION =
            "Compose a project freely from all available project natures and enter exactly the information you need.<br/>"
          + "This kind of project represents a group of people working on a topic, for example a TGiF innovation. "
          + "It can have subprojects allowing to break down the topic into more manageable pieces or represent "
          + "different parallel work streams. Furthermore, you can assign component-like subprojects, for example"
          + "Free-Style Components, to represent the more technical aspects of the topic.";

    protected final Map<String, Float> ranks = new HashMap<String, Float>();
    protected final Set<String> enabledExtensions = new HashSet<String>();

    protected final Map<String, Set<String>> readOnlyItems = new HashMap<String, Set<String>>();
    protected final Map<String, Set<String>> readOnlyAdminItems = new HashMap<String, Set<String>>();
    protected final Map<String, Set<String>> invisibleItems = new HashMap<String, Set<String>>();
    protected final Map<String, Set<String>> invisibleAdminItems = new HashMap<String, Set<String>>();
    protected final Map<String, Set<String>> newValuesAllowedItems = new HashMap<String, Set<String>>();
    protected final Map<String, Map<String, List<String>>> allowedValues = new HashMap<String, Map<String, List<String>>>();
    protected final Map<String, Map<String, String>> captions = new HashMap<String, Map<String, String>>();
    protected final Map<String, Map<String, String>> descriptions = new HashMap<String, Map<String, String>>();

    private final Map<String, ExtensionService<?>> extensions = new HashMap<String, ExtensionService<?>>();

    protected void bindExtensionService(ExtensionService<?> extensionService) {
        String extensionClassName = extensionService.getExtensionClass().getName();
        if (extensions.containsKey(extensionClassName)) {
            throw new RuntimeException(MessageFormat.format(
                    "Extension {0} already has a registered extension service",
                    extensionClassName));
        }
        extensions.put(extensionClassName, extensionService);
    }

    protected void unbindExtensionService(ExtensionService<?> extensionService) {
        extensions.remove(extensionService.getExtensionClass().getName());
    }

    @SuppressWarnings("unchecked")
    protected <T extends ExtensionEntityBase> ExtensionService<T> getExtensionService(Class<T> extension) {
        return (ExtensionService<T>)extensions.get(extension.getClass().getName());
    }

    public DefaultProjectTemplate() {
        Set<String> set = CollectionUtils.asSet(PEOPLE_EXTENSION_CLASSNAME, INFO_EXTENSION_CLASSNAME);

        enabledExtensions.addAll(set);

        newValuesAllowedItems.put(PROJECT_CLASSNAME, CollectionUtils.asSet(Project.PROPERTY_PHASE));

        allowedValues.put(PROJECT_CLASSNAME, CollectionUtils.asMap(Project.PROPERTY_PHASE,
                "Proposal", "Experimental", "Prototype", "Incubation", "Specification",
                "Design", "Implementation", "Alpha", "Beta", "Public Beta", "Release Candidate",
                "Validation", "Ramp Up", "Released", "Stable", "Mature", "Maintenance",
                "Deprecated", "Abandoned", "Closed"));

        readOnlyAdminItems.put(PROJECT_CLASSNAME, CollectionUtils.asSet(
                Project.PROPERTY_TEMPLATEID));
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return TEMPLATE_DISPLAYNAME;
    }

    @Override
    public String getDescription() {
        return TEMPLATE_DESCRIPTION;
    }

    @Override
    public float getRank() {
        return 1000.0f;
    }

    /**
     * Returns the style of projects this template supports,
     * i.e. always {@link ProjectNature#PROJECT}.
     */
    @Override
    public ProjectNature getProjectNature() {
        return ProjectNature.PROJECT;
    }

    @Override
    public boolean isEnabled(String extensionClassName) {
        return enabledExtensions.contains(extensionClassName);
    }

    @Override
    public Collection<?> getAllowedValues(String extensionClassName, Object propertyId) {
        return CollectionUtils.getCollection(allowedValues, extensionClassName, propertyId);
    }

    @Override
    public boolean isReadOnly(String extensionClassName, Object propertyId, boolean isAdmin) {
        if (CollectionUtils.contains(readOnlyAdminItems, extensionClassName, propertyId)) {
            return !isAdmin;
        }
        return CollectionUtils.contains(readOnlyItems, extensionClassName, propertyId);
    }

    @Override
    public boolean isVisible(String extensionClassName, Object propertyId, boolean isAdmin) {
        if (CollectionUtils.contains(invisibleAdminItems, extensionClassName, propertyId)) {
            return isAdmin;
        }
        return !CollectionUtils.contains(invisibleItems, extensionClassName, propertyId);
    }

    @Override
    public boolean isNewItemsAllowed(String extensionClassName, Object propertyId) {
        return CollectionUtils.contains(newValuesAllowedItems, extensionClassName, propertyId);
    }

    @Override
    public Set<ExtensionValidator<?>> getExtensionValidators(String extensionClassName) {
        return Collections.emptySet();
    }

    @Override
    public Set<PropertyValidator> getPropertyValidators(String extensionClassName, Object propertyId) {
        return Collections.emptySet();
    }

    @Override
    public float getRank(String extensionClassName) {
        Float rank = ranks.get(extensionClassName);
        return rank != null ? rank : -1.0f;
    }

    @Override
    public String getCaption(String extensionClassName, Object propertyId) {
        Map<String, String> map = captions.get(extensionClassName);
        return map != null ? map.get(propertyId) : null;
    }

    @Override
    public String getDescription(String extensionClassName, Object propertyId) {
        Map<String, String> map = descriptions.get(extensionClassName);
        return map != null ? map.get(propertyId) : null;
    }
}
