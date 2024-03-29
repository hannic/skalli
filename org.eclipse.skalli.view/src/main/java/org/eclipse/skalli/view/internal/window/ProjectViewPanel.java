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
package org.eclipse.skalli.view.internal.window;

import java.util.Comparator;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.api.java.IconProvider;
import org.eclipse.skalli.api.java.ProjectService;
import org.eclipse.skalli.api.java.ProjectTemplateService;
import org.eclipse.skalli.api.java.authentication.UserUtil;
import org.eclipse.skalli.common.ServiceFilter;
import org.eclipse.skalli.common.Services;
import org.eclipse.skalli.common.User;
import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.core.ProjectTemplate;
import org.eclipse.skalli.model.ext.ValidationException;
import org.eclipse.skalli.view.component.InformationBox;
import org.eclipse.skalli.view.ext.ExtensionStreamSource;
import org.eclipse.skalli.view.ext.ExtensionUtil;
import org.eclipse.skalli.view.ext.Navigator;
import org.eclipse.skalli.view.ext.ProjectInfoBox;
import org.eclipse.skalli.view.internal.application.ProjectApplication;

import com.vaadin.terminal.Resource;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;

public class ProjectViewPanel extends CssLayout {

    private static final long serialVersionUID = -2756706292280384313L;

    private static final String STYLE_EAST_COLUMN = "east-column"; //$NON-NLS-1$
    private static final String STYLE_WEST_COLUMN = "west-column"; //$NON-NLS-1$

    private final ProjectApplication application;
    private final Navigator navigator;
    private final Project project;

    private final CssLayout leftLayout;
    private final CssLayout rightLayout;


    public ProjectViewPanel(ProjectApplication application, Navigator navigator, Project project) {
        super();

        this.application = application;
        this.project = project;
        this.navigator = navigator;

        this.setSizeFull();

        leftLayout = new CssLayout();
        leftLayout.addStyleName(STYLE_EAST_COLUMN);
        leftLayout.setWidth("50%"); //$NON-NLS-1$
        addComponent(leftLayout);

        rightLayout = new CssLayout();
        rightLayout.addStyleName(STYLE_WEST_COLUMN);
        rightLayout.setWidth("50%"); //$NON-NLS-1$
        addComponent(rightLayout);

        renderContent();
    }

    private void renderContent() {
        int leftCounter = 0;
        int rightCounter = 0;

        Set<ProjectInfoBox> infoBoxes = getOrderedVisibleInfoBoxList();
        for (ProjectInfoBox projectInfoBox : infoBoxes) {
            ExtensionUtil context = new ProjectViewContextImpl(projectInfoBox.getClass());
            Component content = projectInfoBox.getContent(project, context);
            if (content != null) {
                InformationBox infoBox = InformationBox.getInformationBox("&nbsp;" + projectInfoBox.getCaption()); //$NON-NLS-1$
                infoBox.getContent().addComponent(content);

                String icon = projectInfoBox.getIconPath();
                if (StringUtils.isNotBlank(icon)) {
                    infoBox.setIcon(new StreamResource(new ExtensionStreamSource(projectInfoBox.getClass(), icon),
                            FilenameUtils.getName(icon), application));
                }

                if (projectInfoBox.getPreferredColumn() == ProjectInfoBox.COLUMN_WEST) {
                    leftLayout.addComponent(infoBox);
                    leftCounter++;
                } else if (projectInfoBox.getPreferredColumn() == ProjectInfoBox.COLUMN_EAST) {
                    rightLayout.addComponent(infoBox);
                    rightCounter++;
                } else {
                    if (leftCounter <= rightCounter) {
                        leftLayout.addComponent(infoBox);
                        leftCounter++;
                    } else {
                        rightLayout.addComponent(infoBox);
                        rightCounter++;
                    }
                }
            }
        }
    }

    private Set<ProjectInfoBox> getOrderedVisibleInfoBoxList() {
        Set<ProjectInfoBox> set = Services.getServices(ProjectInfoBox.class,
                new ServiceFilter<ProjectInfoBox>() {
                    @Override
                    public boolean accept(ProjectInfoBox infoBox) {
                        return infoBox.isVisible(project, application.getLoggedInUser());
                    }
                },
                new Comparator<ProjectInfoBox>() {
                    @Override
                    public int compare(ProjectInfoBox o1, ProjectInfoBox o2) {
                        if (o1.getPositionWeight() != o2.getPositionWeight()) {
                            return new Float(o1.getPositionWeight()).compareTo(o2.getPositionWeight());
                        } else {
                            // in case the position weight is equal, compare by class name to prevent that
                            // one of both info boxes is sorted out of the result set
                            return (o1.getClass().toString().compareTo(o2.getClass().toString()));
                        }
                    }
                });
        return set;
    }

    @Override
    protected String getCss(Component c) {
        if (c instanceof CssLayout) {
            return "float: left"; //$NON-NLS-1$
        } else {
            return ""; //$NON-NLS-1$
        }
    }

    private class ProjectViewContextImpl implements ExtensionUtil {

        private Class<? extends IconProvider> iconProvider;

        public ProjectViewContextImpl(Class<? extends IconProvider> iconProvider) {
            this.iconProvider = iconProvider;
        }

        @Override
        public void persist(Project project) {
            ProjectService projectService = Services.getRequiredService(ProjectService.class);
            try {
                projectService.persist(project, getLoggedInUser().getUserId());
            } catch (ValidationException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean isUserAdmin() {
            return UserUtil.isAdministrator(getLoggedInUser());
        }

        @Override
        public boolean isUserProjectAdmin(Project project) {
            return UserUtil.isProjectAdmin(getLoggedInUser(), project)
                    || UserUtil.isAdministrator(getLoggedInUser());
        }

        @Override
        public User getLoggedInUser() {
            return UserUtil.getUser(application.getLoggedInUser());
        }

        @Override
        public Resource getBundleResource(String path) {
            return new StreamResource(new ExtensionStreamSource(iconProvider, path),
                    FilenameUtils.getName(path), application);
        }

        @Override
        public Navigator getNavigator() {
            return navigator;
        }

        @Override
        public ProjectTemplate getProjectTemplate() {
            ProjectTemplateService templateService = Services.getRequiredService(ProjectTemplateService.class);
            return templateService.getProjectTemplateById(project.getProjectTemplateId());
        }
    }
}
