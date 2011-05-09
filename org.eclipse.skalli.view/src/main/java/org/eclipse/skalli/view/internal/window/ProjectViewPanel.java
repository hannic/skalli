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
import org.eclipse.skalli.view.ext.ExtensionUtil;
import org.eclipse.skalli.view.ext.Navigator;
import org.eclipse.skalli.view.ext.ProjectInfoBox;
import org.eclipse.skalli.view.internal.ExtensionStreamSource;
import org.eclipse.skalli.view.internal.application.ProjectApplication;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;

public class ProjectViewPanel extends CssLayout {

    private static final long serialVersionUID = -2756706292280384313L;

    protected static final String STYLE_PANEL_LAYOUT = "panel-layout"; //$NON-NLS-1$

    private final ProjectApplication application;
    private final Navigator navigator;
    private final Project project;

    public ProjectViewPanel(ProjectApplication application, Navigator navigator, Project project) {
        super();

        this.application = application;
        this.project = project;
        this.navigator = navigator;

        CssLayout leftLayout = new CssLayout() {
            private static final long serialVersionUID = -4251374136128934343L;

            @Override
            protected String getCss(Component c) {
                if (c instanceof InformationBox) {
                    return "margin-bottom: 10px;"; //$NON-NLS-1$
                } else {
                    return ""; //$NON-NLS-1$
                }
            }
        };
        CssLayout rightLayout = new CssLayout() {
            private static final long serialVersionUID = 4993626671504156383L;

            @Override
            protected String getCss(Component c) {
                if (c instanceof InformationBox) {
                    return "margin-bottom: 10px;"; //$NON-NLS-1$
                } else {
                    return ""; //$NON-NLS-1$
                }
            }
        };

        leftLayout.setWidth("50%"); //$NON-NLS-1$
        leftLayout.setMargin(false, true, false, false);
        rightLayout.setWidth("50%"); //$NON-NLS-1$
        setSizeFull();

        int leftCounter = 0;
        int rightCounter = 0;

        addComponent(leftLayout);
        addComponent(rightLayout);

        Set<ProjectInfoBox> infoBoxes = getOrderedVisibleInfoBoxList();
        for (final ProjectInfoBox projectInfoBox : infoBoxes) {
            InformationBox infoBox = InformationBox.getInformationBox("&nbsp;" + projectInfoBox.getCaption()); //$NON-NLS-1$
            Component content = projectInfoBox.getContent(project, new ExtensionUtil() {
                private ProjectTemplate projectTemplate;

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
                    return UserUtil.getUser(ProjectViewPanel.this.application.getLoggedInUser());
                }

                @Override
                public Navigator getNavigator() {
                    return ProjectViewPanel.this.navigator;
                }

                @Override
                public ProjectTemplate getProjectTemplate() {
                    if (projectTemplate == null) {
                        ProjectTemplateService templateService = Services
                                .getRequiredService(ProjectTemplateService.class);
                        projectTemplate = templateService.getProjectTemplateById(ProjectViewPanel.this.project
                                .getProjectTemplateId());
                    }
                    return projectTemplate;
                }

            });

            content.setStyleName(STYLE_PANEL_LAYOUT);
            infoBox.getContent().addComponent(content);

            final String icon = projectInfoBox.getIconPath();
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

    private Set<ProjectInfoBox> getOrderedVisibleInfoBoxList() {
        Set<ProjectInfoBox> set = Services.getServices(ProjectInfoBox.class,
                new ServiceFilter<ProjectInfoBox>() {
                    @Override
                    public boolean accept(ProjectInfoBox infoBox) {
                        return infoBox.isVisible(project, application.getLoggedInUser());
                    }
                }, new Comparator<ProjectInfoBox>() {
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
}
