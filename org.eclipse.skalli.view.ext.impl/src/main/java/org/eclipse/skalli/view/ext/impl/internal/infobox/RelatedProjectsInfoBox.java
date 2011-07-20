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
package org.eclipse.skalli.view.ext.impl.internal.infobox;

import java.util.UUID;

import org.eclipse.skalli.api.java.ProjectService;
import org.eclipse.skalli.api.java.SearchHit;
import org.eclipse.skalli.api.java.SearchResult;
import org.eclipse.skalli.api.java.SearchService;
import org.eclipse.skalli.common.Services;
import org.eclipse.skalli.common.util.UUIDList;
import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.ext.misc.RelatedProjectsExt;
import org.eclipse.skalli.view.ext.ExtensionUtil;
import org.eclipse.skalli.view.ext.InfoBox;
import org.eclipse.skalli.view.ext.ProjectInfoBox;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;

public class RelatedProjectsInfoBox extends InfoBox implements ProjectInfoBox {

    @Override
    public String getIconPath() {
        return "res/icons/relProjects.png"; //$NON-NLS-1$
    }

    @Override
    public String getCaption() {
        return "Related Projects";
    }

    @Override
    public Component getContent(Project project, ExtensionUtil util) {
        Layout layout = new CssLayout();
        layout.setSizeFull();

        RelatedProjectsExt ext = project.getExtension(RelatedProjectsExt.class);
        if (ext != null) {
            Label label = new Label("The following projects might also be of interest to you:",
                    Label.CONTENT_XHTML);
            layout.addComponent(label);
            boolean calculated = ext.getCalculated();
            if (calculated) {
                addCalculatedContent(project, layout);
            } else {
                UUIDList ids = ext.getRelatedProjects();
                ProjectService projectService = Services.getRequiredService(ProjectService.class);
                for (UUID uuid : ids) {
                    Project relatedProject = projectService.getByUUID(uuid);
                    ExternalResource externalResource = new ExternalResource("/projects/" + relatedProject.getProjectId());
                    String content = HSPACE + "<a href=" + externalResource.getURL() + ">" + relatedProject.getName()
                            + "</a>";
                    Label l = new Label(content, Label.CONTENT_XHTML);
                    layout.addComponent(l);
                }
            }
        }
        return layout;
    }

    protected void addCalculatedContent(Project project, Layout layout) {
        SearchService searchService = Services.getService(SearchService.class);
        if (searchService != null) {
            SearchResult<Project> relatedProjects = searchService.getRelatedProjects(project, 3);
            for (SearchHit<Project> hit : relatedProjects.getResult()) {
                ExternalResource externalResource = new ExternalResource("/projects/" + hit.getEntity().getProjectId());
                String content = HSPACE + "<a href=" + externalResource.getURL() + ">" + hit.getEntity().getName()
                        + "*</a>";
                Label label = new Label(content, Label.CONTENT_XHTML);
                layout.addComponent(label);
            }
            Label label = new Label(HSPACE + "*calculated based on similarities between the projects",
                    Label.CONTENT_XHTML);
            label.setStyleName("light");//$NON-NLS-1$
            layout.addComponent(label);

        }
    }

    @Override
    public float getPositionWeight() {
        return 1.8f;
    }

    @Override
    public int getPreferredColumn() {
        return COLUMN_EAST;
    }

    @Override
    public boolean isVisible(Project project, String loggedInUserId) {
        RelatedProjectsExt ext = project.getExtension(RelatedProjectsExt.class);
        if (ext == null || ext.getRelatedProjects().isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

}
